package com.sailvan.dispatchcenter.common.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class HttpClientUtils {

    private RestTemplate restTemplate;

    public HttpClientUtils() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        // 设置连接超时，单位毫秒
        requestFactory.setConnectTimeout(8000);
        //设置读取超时
        requestFactory.setReadTimeout(5000);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(requestFactory);
        this.restTemplate = restTemplate;
    }

    public String post(String url, HttpEntity httpEntity){
        ResponseEntity<String> exchange = restTemplate.postForEntity(url, httpEntity, String.class);
        return exchange.getBody();
    }

    public String generateRequestParameters(String protocol, String uri, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder(protocol).append("://").append(uri);
        if (params != null && !params.isEmpty()) {
            sb.append("?");
            for (Map.Entry map : params.entrySet()) {
                sb.append(map.getKey())
                        .append("=")
                        .append(map.getValue())
                        .append("&");
            }
            uri = sb.substring(0, sb.length() - 1);
            return uri;
        }
        return sb.toString();
    }


    public String get(String url, HttpEntity httpEntity) {
        ResponseEntity<String> resEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
        return resEntity.getBody();
    }
}
