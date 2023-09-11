package com.sailvan.dispatchcenter.db.service;

import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.db.config.TokenConfig;
import com.sailvan.dispatchcenter.common.util.HttpClientUtils;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.github.pagehelper.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class TokenService implements com.sailvan.dispatchcenter.common.pipe.TokenService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    HttpClientUtils httpClient;
    @Resource
    TokenConfig tokenConfig;
    @Resource
    RedisUtils redisUtils;

    /**
     * 请求token系统拉取token信息
     * @param code 回调返回的code
     * @return token信息
     */
    @Override
    public String requestToken(String code){
        try {
            String url = tokenConfig.getTokenUrl() + "/api/token";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
            LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
            paramMap.add("grant_type", "authorization_code");
            paramMap.add("client_id", tokenConfig.getClientId());
            paramMap.add("client_secret",tokenConfig.getClientSecret());
            paramMap.add("redirect_uri",tokenConfig.getCallbackUrl());
            paramMap.add("code",code);
            HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<>(paramMap, headers);
            return httpClient.post(url, httpEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 将token信息放入缓存
     * @param data
     */
    @Override
    public void setCache(String data){

        Map map = JSONObject.parseObject(data);
        Object token = map.get("access_token");
        long expiresIn = Long.valueOf((int)map.get("expires_in"));
        Object refreshToken = map.get("refresh_token");

        redisUtils.put("token_access_token",token.toString(),expiresIn);
        redisUtils.put("token_refresh_token",refreshToken.toString(),expiresIn*3);
    }

    /**
     * 获取token系统的access token
     * @return access token
     */
    @Override
    public String getAccessToken(){
        if (redisUtils.exists("token_access_token")){
            return redisUtils.get("token_access_token").toString();
        }else {
            Object refreshToken = redisUtils.get("token_refresh_token");

            if (StringUtils.isEmpty(refreshToken)){
                return "";
            }
            for(int i =0 ; i<3; i++){//重试三次
                String data = refreshAccessToken(refreshToken);
                if(StringUtil.isNotEmpty(data)){
                    setCache(data);
                    Map map = JSONObject.parseObject(data);
                    return map.get("access_token").toString();
                }
            }
        }
        return "";
    }

    /**
     * 刷新token系统的token信息
     * @param refreshToken refresh token
     * @return 刷新后token信息
     */
    @Override
    public String refreshAccessToken(Object refreshToken){
        try {
            String url = tokenConfig.getTokenUrl() + "/api/token";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
            LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
            paramMap.add("grant_type", "refresh_token");
            paramMap.add("client_id", tokenConfig.getClientId());
            paramMap.add("client_secret",tokenConfig.getClientSecret());
            paramMap.add("refresh_token",refreshToken);
            paramMap.add("scope","");
            HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<>(paramMap, headers);
            return httpClient.post(url, httpEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    @Override
    public String getTokenInfo(String account, String platform, String devName){
        String accessToken = getAccessToken();
        if (StringUtil.isEmpty(accessToken)){
            logger.error("无法获取token系统的access token");
        }else {
            try {
                String url = tokenConfig.getTokenUrl() + "/api/get_token";
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
                headers.add("Authorization","Bearer " + accessToken);
                LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
                paramMap.add("platform_name", platform);
                paramMap.add("account_name", account);
//                paramMap.add("dev_name", devName);
                HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<>(paramMap, headers);
                return httpClient.post(url, httpEntity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 获取店铺信息详情
     * @param account 账号
     * @param platform 平台
     * @param area 地区
     * @param site 站点
     * @return 店铺信息
     */
    @Override
    public String getAccountDetail(String account, String platform, String area, String site){
        String accessToken = getAccessToken();
        if (StringUtil.isEmpty(accessToken)){
            logger.error("无法获取token系统的access token");
        }else {
            try {
                String url = tokenConfig.getTokenUrl() + "/api/account/detail";
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
                headers.add("Authorization","Bearer " + accessToken);
                LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
                paramMap.add("platform_name", platform);
                paramMap.add("account_name", account);
                paramMap.add("area", area);
                paramMap.add("site", site);
                HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<>(paramMap, headers);
                return httpClient.post(url, httpEntity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}
