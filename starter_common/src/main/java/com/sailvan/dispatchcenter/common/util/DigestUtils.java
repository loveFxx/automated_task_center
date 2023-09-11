package com.sailvan.dispatchcenter.common.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.util.ByteSource;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.util.*;

import org.apache.commons.codec.binary.Base64;

/**
 * @Title: DigestUtils
 * @Description:
 */
public class DigestUtils {

    public static final String ENCODE_TYPE_HMAC_SHA_256 ="HmacSHA256";
    public static final String ENCODE_UTF_8_UPPER ="UTF-8";
    /**
     *
     * 功能描述: MD5加密账号密码
     *
     */
    public static String Md5(String userName,String password){
        Md5Hash hash = new Md5Hash(password, ByteSource.Util.bytes(userName), 2);
        return hash.toString();
    }

    /**
     * 转化为所需的内容体
     * @param params 原先的参数Map
     * @return
     */
    public static String createMessage(Map<String, Object> params) {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, Object> param = ksort(params);
        Iterator var3 = param.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<String, Object> paramEntry = (Map.Entry)var3.next();
            String key = String.valueOf(paramEntry.getKey()).trim();
            String value = String.valueOf(paramEntry.getValue()).trim();
            if (isNotEmpty(key) && isNotEmpty(value)) {
                stringBuilder.append(key).append(value);
            }
        }

        return stringBuilder.toString().toUpperCase();
    }

    /**
     * 按键值排序
     * @param params
     * @return
     */
    public static TreeMap<String, Object> ksort(Map<String, Object> params) {
        if (params instanceof TreeMap) {
            return (TreeMap)params;
        } else {
            TreeMap<String, Object> sorted = new TreeMap();
            sorted.putAll(params);
            return sorted;
        }
    }

    /**
     * 判断非空字符
     * @param str
     * @return
     */
    private static boolean isNotEmpty(String str) {
        return (str.equals("null") || str.equals("")) ? false : str.trim().length() > 0;
    }

    /**
     * 创建签名
     * @param appKey 秘钥ID
     * @param appSecret 秘钥
     * @param message 内容体
     * @return 签名
     * @throws Exception
     */
    public static String createDispatchCenterSign(String appKey,String appSecret, String message) throws Exception {
        if (StringUtils.isEmpty(appKey) || StringUtils.isEmpty(appSecret)){
            return null;
        }

        String secret = org.springframework.util.DigestUtils.md5DigestAsHex((appKey+appSecret).getBytes());
        String encodeStr;
        try{
            //HMAC_SHA256 加密
            Mac HMAC_SHA256 = Mac.getInstance(ENCODE_TYPE_HMAC_SHA_256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(ENCODE_UTF_8_UPPER),ENCODE_TYPE_HMAC_SHA_256);
            HMAC_SHA256.init(secretKeySpec);
            byte[] bytes = HMAC_SHA256.doFinal(message.getBytes(ENCODE_UTF_8_UPPER));
            if (bytes == null && bytes.length<1){
                return null;
            }
            String BASE64 = Base64.encodeBase64String(bytes);
            if (StringUtils.isEmpty(BASE64)){
                return null;
            }

            //url encode
            encodeStr = URLEncoder.encode(BASE64,ENCODE_UTF_8_UPPER);
        }catch (Exception e){
            throw new Exception("HmacSHA256加密异常");
        }
        return encodeStr;
    }

    public static void main(String[] args) throws Exception {
    }
}


