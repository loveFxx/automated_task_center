package com.sailvan.dispatchcenter.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.sailvan.dispatchcenter.common.constant.Constant;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;


import static com.sailvan.dispatchcenter.common.util.SailvanBus.newBusInstance;

@Component
public class BusSystemUtils {

    @Autowired
    HttpClientUtils httpClient;

    private String buildUrl(String type) {
        return Constant.MAP_BUS_SYSTEM.get(type).get("router");
    }



    /**
     *
     * @param api 接口名
     * @param headers 头部信息
     * @param params 参数
     * @param version 接口版本号
     * @param format 返回格式，支持json/xml
     * @param type 类型 inner:[内网->内网] sz:[国内外网->内网] hk:[海外外网->内网]
     * @param method get/post
     * @return 内容
     */
    public String request(String api, HttpHeaders headers, HashMap<String, Object> params, String version, String format, String type, String method){
        headers.add("Content-Type", "application/json; charset=UTF-8");
        String appKey = Constant.MAP_BUS_SYSTEM.get(type).get("appKey");
        String appSecret = Constant.MAP_BUS_SYSTEM.get(type).get("appSecret");
        SailvanBus busApi = newBusInstance(appKey, appSecret, buildUrl(type));
        String json = JSON.toJSONString(params);
        HashMap hashMap = JSON.parseObject(json, HashMap.class, Feature.OrderedField);

        if ("post".equals(method)){
            HttpEntity<HashMap<String, Object>> httpEntity = new HttpEntity<>(hashMap, headers);
            return busApi.post(json, httpEntity, api,version,format);
        }else {
            HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
            return busApi.get(json,httpEntity,api,version,format);
        }
    }
}
