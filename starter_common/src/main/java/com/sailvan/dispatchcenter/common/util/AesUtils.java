package com.sailvan.dispatchcenter.common.util;



import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Aes加密解密工具类
 * @date 2021-04
 * @author menghui
 */
public class AesUtils {


    public static String encrypt(String sSrc, String sKey, String ivKey, String cipherValue) throws Exception {
        if (sKey == null || sSrc == null) {
            return null;
        }

        // 判断Key是否为16位
        int length = 16;
        if (sKey.length() != length) {
            return null;
        }
        String charsetName = "utf-8";
        String aes = "AES";
        byte[] raw = sKey.getBytes(charsetName);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, aes);
        Cipher cipher = Cipher.getInstance(cipherValue);

        //使用CBC模式，需要一个向量iv，可增加加密算法的强度
        IvParameterSpec iv = new IvParameterSpec(ivKey.getBytes());

        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes());
        //此处使用BASE64做转码功能，同时能起到2次加密的作用。
        String s = Base64.getEncoder().encodeToString(encrypted);
        return s;
    }

    public static String decrypt(String sSrc, String sKey, String ivKey, String cipherValue) throws Exception {
        try {
            // 判断Key是否正确
            if (sKey == null) {
                System.out.print("Key为空null");
                return null;
            }
            int length = 16;
            // 判断Key是否为16位
            if (sKey.length() != length) {
                System.out.print("Key长度不是16位");
                return null;
            }
            String aes = "AES";
            String charsetName = "utf-8";
            byte[] raw = sKey.getBytes(charsetName);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, aes);
            Cipher cipher = Cipher.getInstance(cipherValue);

            IvParameterSpec iv = new IvParameterSpec(ivKey.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            //先用base64解密
            byte[] encrypted1 = Base64.getDecoder().decode(sSrc);
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original);
                return originalString;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        String cSrc = "{\"beijing_time\":\"2022/01/05\",\"mem_used\":23.35653791767496,\"user_num\":9,\"cpu_used\":2,\"timezone\":-8,\"dist_info\":{\"F:\":{\"size\":\"\",\"free_space\":\"\"},\"E:\":{\"size\":\"190862360576\",\"free_space\":\"190743539712\"},\"D:\":{\"size\":\"191127089152\",\"free_space\":\"190953922560\"},\"C:\":{\"size\":\"118112645120\",\"free_space\":\"98549166080\"}},\"worker_type\":{\"AM01785_Europe\":\"mt_daily,mt_monthly,mt_b2b_monthly,returns_daily,returns_monthly,mt_summary_monthly,deal,deal_result,inventory_age,vat_monthly,business_daily,bank_card,box_upload,box_download,order_shoot,balance_daily,balance_quarterly,account_status,feedback_daily,storage_free_monthly,storage_mon_monthly,referenceid,balance_monthly,cpc_sp_daily,cpc_sd_daily,cpc_sbv_daily,cpc_sb_daily,cpc_sp_monthly,cpc_sd_monthly,cpc_sbv_monthly,cpc_sb_monthly,demand_compensation,vat_download_daily,brand_registry_daily\"},\"user_names\":\",admin1,,admin2,admin3,admin4,admin5,Administrator,Guest,sw3046,xinrui\",\"net_work\":false,\"client_file_version\":\"2.3.1\",\"client_version\":\"2.0.4\",\"machine_local_time\":\"2022-01-05 10:40:04\"}";

        System.out.println("size:"+cSrc.length());
        //密码
        String cKey = "RLnfYYroRUPeXqYP";
        String B_KEY= "a$H#tsiu6ZXsz78N";
        // 偏移量
        String ivKey = "4754158104636809";
        //"算法/模式/补码方式"
        String cipherValue = "AES/CBC/PKCS5Padding";
        cipherValue.hashCode();

        System.out.println(cSrc);
        // 加密
        String enString  = AesUtils.encrypt(cSrc, cKey, ivKey, cipherValue);
        System.out.println("加密后的字串是：" + enString);

        // 解密
//        enString = "xcnJIqGr/YYKBuY0C2FZNzOqTL6nPK5OPSQu4LsfQXk=";
        String deString = AesUtils.decrypt(enString, cKey, ivKey, cipherValue);
        System.out.println("解密后的字串是：" + deString);

        String s = ":222";
//        System.out.println("---"+s.split(":").length+"---");
//        System.out.println("---"+s.split(":")[0]+"---");
//        System.out.println("---"+s.split(":")[1]+"---");

        List<String> removePlatformList = new ArrayList<String>();
        removePlatformList.add("1");
        removePlatformList.add("2");
        removePlatformList.add("3");


        List<String> add = new ArrayList<String>();
        add.add("2");
        add.add("4");

        // 差集 (list1 - list2)
        List<String> reduce1 = removePlatformList.stream().filter(item -> !add.contains(item)).collect(toList());
//        System.out.println("---移除 差集 reduce1 (list1 - list2)---");
        reduce1.parallelStream().forEach(System.out :: println);

        // 差集 (list2 - list1)
        List<String> reduce2 = add.stream().filter(item -> !removePlatformList.contains(item)).collect(toList());
//        System.out.println("---添加 差集 reduce2 (list2 - list1)---");
        reduce2.parallelStream().forEach(System.out :: println);



    }

}