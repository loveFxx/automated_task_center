package com.sailvan.dispatchcenter.common.util;

import org.springframework.http.HttpEntity;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;

public class SailvanBus {

    private String appKey = "";
    private String appSecret = "";
    private String busUrl = null;

    public static SailvanBus newBusInstance(String appKey, String appSecret, String busUrl) {
        return new SailvanBus(appKey, appSecret, busUrl);
    }

    private SailvanBus(String appKey, String appSecret, String busUrl) {
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.busUrl = busUrl;
    }

    public String createSignV2(String params) {

        return this.getMD5(params + this.appSecret).toUpperCase();
    }

    public String createQueryString(Map<String, Object> params, boolean encode) {
        StringBuilder stringBuilder = new StringBuilder();
        Iterator var4 = params.entrySet().iterator();
        while(var4.hasNext()) {
            Entry paramEntry = (Entry)var4.next();
            try {
                String key = String.valueOf(paramEntry.getKey());
                String value = String.valueOf(paramEntry.getValue());
                if (encode) {
                    stringBuilder.append(key).append("=").append(URLEncoder.encode(value, "UTF-8")).append("&");
                } else {
                    stringBuilder.append(key).append("=").append(value).append("&");
                }
            } catch (UnsupportedEncodingException var8) {
                var8.printStackTrace();
            }
        }

        return stringBuilder.toString();
    }

    public String getUrl(String queryString) {
        if (this.busUrl.contains("?") && this.busUrl.endsWith("?")) {
            return this.busUrl + queryString;
        } else {
            return this.busUrl.contains("?") && !this.busUrl.endsWith("?") ? this.busUrl + "&" + queryString : this.busUrl + "?" + queryString;
        }
    }

    /**
     * 构建请求参数
     * @param api  接口名
     * @param version 版本号
     * @param format  返回格式，支持json/xml
     */
    private HashMap<String, Object> buildQueryParams(String api, String version, String format) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("method", api);
        params.put("format", format);
        params.put("v", version);
        params.put("protocol", "param5");
        params.put("app_key", this.appKey);
        return params;
    }

    public String get(String params, HttpEntity httpEntity, String api, String version, String format){
        HashMap<String, Object> queryParams = buildQueryParams(api, version, format);
        String queryString = this.createQueryString(queryParams, true);
        String sign = this.createSignV2(params);
        String url = this.getUrl("sign=" + sign + "&" + queryString);
        HttpClientUtils httpClientUtils = new HttpClientUtils();
        return httpClientUtils.get(url,httpEntity);
    }

    public String post(String params, HttpEntity httpEntity, String api, String version, String format){
        HashMap<String, Object> queryParams = buildQueryParams(api, version, format);
        String queryString = this.createQueryString(queryParams, true);
        String sign = this.createSignV2(params);
        String url = this.getUrl("sign=" + sign + "&" + queryString);
        HttpClientUtils httpClientUtils = new HttpClientUtils();
        return httpClientUtils.post(url,httpEntity);
    }

    private static String getMD5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            String md5 = (new BigInteger(1, md.digest())).toString(16);
            if (md5.length() == 32) {
                return md5;
            } else {
                int addZeros = 32 - md5.length();
                char[] zeros = new char[addZeros];

                for(int i = 0; i < addZeros; ++i) {
                    zeros[i] = '0';
                }

                return new String(zeros) + md5;
            }
        } catch (NoSuchAlgorithmException var7) {
            var7.printStackTrace();
            return null;
        }
    }
}
