package com.sailvan.dispatchcenter.db.service;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;
import com.sailvan.dispatchcenter.common.domain.BusinessSystem;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.common.util.WebTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;

/**
 * @author menghui
 * @date 21-08
 */
@Service
public class BusinessService implements com.sailvan.dispatchcenter.common.pipe.BusinessService {

    private static Logger logger = LoggerFactory.getLogger(BusinessService.class);

    @Autowired
    private BusinessSystemService businessSystemService;

    @Autowired
    RedisUtils redisUtils;

    @Override
    public ApiResponseDomain registerBusiness(JSONObject businessSystem, HttpServletResponse response){
        ApiResponse apiResponse = new ApiResponse();
        String systemName = businessSystem.getString("system_name");
        if (StringUtils.isEmpty(systemName)) {
            //请求参数系统名system_name是空的
            return apiResponse.error(ResponseCode.ERROR_CODE,"system_name cannot empty","{}");
        }
        if(!businessSystem.containsKey("dispatch_center_app_key") || !businessSystem.containsKey("dispatch_center_app_secret")){
            return apiResponse.error(ResponseCode.ERROR_CODE,"请求参数不包含 dispatch_center_app_key or dispatch_center_app_secret","{}");
        }

        systemName = systemName.toUpperCase().trim();
        //请求系统不存在或无效
        BusinessSystem checkBusiness = businessSystemService.checkBusiness(systemName);
        if(checkBusiness == null){
            return apiResponse.error(ResponseCode.ERROR_CODE,"system_name:"+systemName+" not exist","{}");
        }
//        if(businessSystem.containsKey("dispatch_center_app_key") && businessSystem.containsKey("dispatch_center_app_secret")){
        if (!checkBusiness.getAppKey().equals(businessSystem.getString("dispatch_center_app_key")) || !checkBusiness.getAppSecret().equals(businessSystem.getString("dispatch_center_app_secret"))) {
            return apiResponse.error(ResponseCode.ERROR_CODE,"dispatch_center_app_key or dispatch_center_app_secret:"+systemName+" is error,请联系管理员确认数据是否正确","{}");
        }
//        }
        String token;
        String format;
        synchronized (systemName.intern()){
            String redisKey = Constant.BUSINESS_REGISTER_PREFIX+systemName;
            String redisValidityKey = Constant.BUSINESS_REGISTER_VALID_PREFIX+systemName;
            Object redisValue = redisUtils.get(redisKey);
            if(redisValue == null){
                //  生成Token，拿到Token设置在响应Headers里返回
                token = businessSystemService.createBusinessToken(checkBusiness);
                DecodedJWT jwtToken = WebTokenUtil.decode(token);
                jwtToken.getSubject();
                response.addHeader("Authorization", "Bearer " + token);
                format = DateUtils.getTokenValidityTime();
                redisUtils.put(redisKey, token, Long.valueOf(Constant.TOKEN_VALIDITY_TIME * 60));
                redisUtils.put(redisValidityKey, format, Long.valueOf(Constant.TOKEN_VALIDITY_TIME * 60));
            }else {
                token = String.valueOf(redisValue);
                format = String.valueOf(redisUtils.get(redisValidityKey));
            }
        }
        JSONObject content = new JSONObject();
        content.put("access_token", token);
        content.put("system_name", checkBusiness.getSystemName());
        content.put("expiration", format);
        logger.info("注册 register-----system_name:{}, content:{}", systemName, content);
        return apiResponse.success("success",content);
    }
}
