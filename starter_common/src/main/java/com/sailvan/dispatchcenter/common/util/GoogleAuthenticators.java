package com.sailvan.dispatchcenter.common.util;


import java.net.URI;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.HOTPGenerator;
import com.bastiaanjansen.otp.helpers.URIHelper;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.StringUtils;

/**
 * <p>Google 双因子验证</p>
 *
 * @author
 * @version 1.0
 * @since
 */
public class GoogleAuthenticators {
    // 来自谷歌文档，不用修改
    public static final int SECRET_SIZE = 10;
    // 产生密钥的种子
    public static final String SEED = "N%U%cls36e7Ab!@#asd34nB4%9%Nmo2ai1IC9@54n06aY";
    // 安全哈希算法（Secure Hash Algorithm）
    public static final String RANDOM_NUMBER_ALGORITHM = "SHA1PRNG";
    //可偏移的时间 -- 3*30秒的验证时间（客户端30秒生成一次验证码）
    private static Integer window_size = 0;

    /**
     * 生成密钥
     *
     * @return
     */
    public static String generateSecretKey() {
        SecureRandom sr;
        try {
            sr = SecureRandom.getInstance(RANDOM_NUMBER_ALGORITHM);
            sr.setSeed(Base64.decodeBase64(SEED));
            byte[] buffer = sr.generateSeed(SECRET_SIZE);
            Base32 codec = new Base32();
            byte[] bEncodedKey = codec.encode(buffer);
            return new String(bEncodedKey);
        } catch (NoSuchAlgorithmException e) {
            // should never occur... configuration error
        }
        return null;
    }

    /**
     * 校验验证码
     *
     * @param secret
     * @param code
     * @param timeMsec
     * @return
     */
    public static Boolean check_code(String secret, long code, long timeMsec) {
        Base32 codec = new Base32();
        byte[] decodedKey = codec.decode(secret);
        // convert unix msec time into a 30 second "window"
        // this is per the TOTP spec (see the RFC for details)
        long t = (timeMsec / 1000L) / 30L;
        // Window is used to check codes generated in the near past.
        // You can use this value to tune how far you're willing to go.
        for (int i = -window_size; i <= window_size; ++i) {
            long hash;
            try {
                hash = verify_code(decodedKey, t + i);
            } catch (Exception e) {
                // Yes, this is bad form - but
                // the exceptions thrown would be rare and a static configuration problem
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
            if (hash == code) {
                return true;
            }
        }
        // The validation code is invalid.
        return false;
    }

    /**
     * 生成验证码
     *
     * @param key
     * @param t
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private static int verify_code(byte[] key, long t) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] data = new byte[8];
        long value = t;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }
        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);
        int offset = hash[20 - 1] & 0xF;
        // We're using a long because Java hasn't got unsigned int.
        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            // We are dealing with signed bytes:
            // we just keep the first byte.
            truncatedHash |= (hash[offset + i] & 0xFF);
        }
        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;
        return (int) truncatedHash;
    }

    public static String createCode(String secret, long timeMsec) {
        Base32 codec = new Base32();
        byte[] decodedKey = codec.decode(secret);
        // convert unix msec time into a 30 second "window"
        // this is per the TOTP spec (see the RFC for details)
        long t = (timeMsec / 1000L) / 30L;
        // Window is used to check codes generated in the near past.
        // You can use this value to tune how far you're willing to go.
        LinkedList<Long> hashList = new LinkedList<>();
        for (int i = -window_size; i <= window_size; ++i) {
            long hash;
            try {
                hash = verify_code(decodedKey, t + i);
            } catch (Exception e) {
                // Yes, this is bad form - but
                // the exceptions thrown would be rare and a static configuration problem
                throw new RuntimeException(e.getMessage());
            }
            hashList.add(hash);
        }
        return hashList.getLast().toString();
    }

    public static String getToTpCode1(String qrContent) throws Exception{
        if(StringUtils.isEmpty(qrContent)){
            return null;
        }
        URI uri = new URI(qrContent);
        HOTPGenerator toTp = HOTPGenerator.Builder.fromOTPAuthURI(uri);
        byte[] secret = toTp.getSecret();
        // 6
        int passwordLength = toTp.getPasswordLength();
        // HMACAlgorithm.SHA1
        HMACAlgorithm algorithm = toTp.getAlgorithm();
        String codes = toTp.generate(passwordLength);
        if (toTp.verify(codes, passwordLength)){
            return codes;
        }
        return null;
    }

    public static Map<String,Object> getToTpCode(String qrContent) throws Exception{
        Map<String,Object> result = new HashMap();
        if(StringUtils.isEmpty(qrContent)){
            result.put("code",null);
            result.put("time",0);
            return null;
        }
        long time = System.currentTimeMillis();
        URI uri = new URI(qrContent);
        HOTPGenerator toTp = HOTPGenerator.Builder.fromOTPAuthURI(uri);
        byte[] secret = toTp.getSecret();
        String res = new String(secret);
        String code1 = createCode(res, time);
        int timeEffective = 0;
        for (int i = 30; i >0; i--) {
            if (check_code(res, Integer.parseInt(code1), time+i*1000)) {
                timeEffective = i;
                break;
            }
        }
        result.put("code",code1);
        result.put("time",timeEffective);
        return result;
    }



    public static void main(String[] args) throws Exception {
        // otpauth://totp/Amazon%3Amichaely41%40yeah.net?secret=XDPY2L4BEIDK4B25T27B75ZI5L5ZHIZZ6G233XKVKMDC3BMGN5BQ&issuer=Amazon
        String ss = "FINFZZJ6YLCKXFW3CLFDEV3U3D2EFM5BTMSYJ4IRJX4XC7L6C2MA";
        long time = System.currentTimeMillis();
        String code = createCode(ss, time);
        System.out.println("      ==========" + code);
        String url = "otpauth://totp/Amazon%3Afelixnc264%40gmail.com?secret=FINFZZJ6YLCKXFW3CLFDEV3U3D2EFM5BTMSYJ4IRJX4XC7L6C2MA&issuer=Amazon";
        System.out.println("code:"+getToTpCode(url));
        System.out.println(check_code(url, Integer.parseInt(code), time));
        URI uri = new URI(url);
        String path = uri.getPath();
        URLDecoder.decode(path);

        long start = System.currentTimeMillis();
        System.out.println();
        for (int i = 30; i >0; i--) {
            if (check_code(url, Integer.parseInt(code), time+i*1000)) {
                System.out.println("time:"+(i-1));
                break;
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("time:"+(end-start)+"ms");

        String t = "";
        List<String> taskIdLists = Arrays.asList(t.split(","));



    }
}

