package com.sailvan.dispatchcenter.core.filter;

import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.cache.InitMachineCache;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.constant.RequestCountCode;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.Machine;
import com.sailvan.dispatchcenter.common.util.AesUtils;
import com.sailvan.dispatchcenter.core.async.AsyncPushTask;
import com.sailvan.dispatchcenter.core.util.RequestCountUtils;
import com.sailvan.dispatchcenter.db.util.UserSecurityUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.sailvan.dispatchcenter.common.constant.Constant.*;


/**
 * 登录拦截
 * @author mh
 * @date 2021
 */
@Component
public class UserSecurityHandlerInterceptor implements HandlerInterceptor {

    private static Logger logger = LoggerFactory.getLogger(UserSecurityHandlerInterceptor.class);

    @Autowired
    private UserSecurityUtil userSecurityUtil;

    @Autowired
    InitMachineCache initMachineCache;

    @Autowired
    RequestCountUtils requestCountUtils;
    /**
     * 进行token验证和权限验证
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断是否是跨域请求，并且是options的请求，直接返回true
        String uri = request.getRequestURI();
        String clientResister = "/client/register";
        String businessResister = "/business/register";
        if(uri.startsWith(BUSINESS_PREFIX)){
            StringBuffer requestURL = request.getRequestURL();
            if(requestURL.toString().contains("192.168.201.29")){
                logger.info("UserSecurityHandlerInterceptor: business url:{}",requestURL);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code", ResponseCode.ERROR_CODE);
                jsonObject.put("mes","不能使用 IP:192.168.201.29 访问");
                writeResponse(response, HttpStatus.UNAUTHORIZED,jsonObject.toJSONString());
                return false;
            }
            System.err.println("UserSecurityHandlerInterceptor preHandle ...");

            //校验的方法封装在了UserSecurityUtil这个类中，后面有这个类的代码
            request.setAttribute(Constant.AUTHORIZATION_REQUEST_URL_NAME,uri.replaceAll(BUSINESS_PREFIX+"/",""));
            requestCountUtils.recordRequest(RequestCountCode.REQUEST_NUM , request.getAttribute(Constant.AUTHORIZATION_BUSINESS_NAME), uri.replaceAll(BUSINESS_PREFIX+"/",""));
            if(businessResister.equals(uri)){
                return true;
            }
            boolean check = userSecurityUtil.verifyWebToken(request, response);
            if (!check) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code", ResponseCode.BUSINESS_UNAUTHORIZED_TOKEN_ERROR_CODE);
                jsonObject.put("mes","token无效");
                writeResponse(response, HttpStatus.UNAUTHORIZED, jsonObject.toJSONString());
                return false;
            }
            return check;
        }else if(uri.startsWith(CLIENT_PREFIX)){
            String url = uri.replaceAll(CLIENT_PREFIX+"/","");
            request.setAttribute(Constant.AUTHORIZATION_REQUEST_URL_NAME,url);
            requestCountUtils.recordRequest(RequestCountCode.REQUEST_NUM,"client", url);
            if(clientResister.equals(uri)){
                return true;
            }
            String token = request.getHeader("Authorization");
            JSONObject jsonObject = new JSONObject();
            if (StringUtils.isEmpty(token)) {
                jsonObject.put("code", ResponseCode.CLIENT_NO_TOKEN_ERROR_CODE);
                jsonObject.put("mes","没有携带token");
                writeResponse(response, HttpStatus.NON_AUTHORITATIVE_INFORMATION, jsonObject.toJSONString());
                return false;
            }
            String decrypt = AesUtils.decrypt(token, C_KEY, IV_KEY, CIPHER_VALUE);
            if(!StringUtils.isEmpty(decrypt)){
                request.setAttribute(AUTHORIZATION_DECRYPT,decrypt);
                request.setAttribute(AUTHORIZATION_ENCRYPT,token);
                request.setAttribute(AUTHORIZATION_CLIENT_URL_NAME,uri);
                Machine select = initMachineCache.getMachineCacheMapCacheByIp(decrypt);
                if (select == null || StringUtils.isEmpty(select.getToken())){
                    jsonObject.put("code", ResponseCode.ERROR_CODE);
                    jsonObject.put("mes","当前机器没有注册,请先进行注册");
                    writeResponse(response, HttpStatus.UNAUTHORIZED, jsonObject.toJSONString());
                    return false;
                }
                return true;
            }
            jsonObject.put("code", ResponseCode.CLIENT_UNAUTHORIZED_TOKEN_ERROR_CODE);
            jsonObject.put("mes","token验证失败");
            writeResponse(response, HttpStatus.UNAUTHORIZED, jsonObject.toJSONString());
        }
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

    private void writeResponse(HttpServletResponse resp, HttpStatus status, String restResult) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        resp.setStatus(status.value());
        resp.setContentType("application/json; charset=utf-8");
        resp.getWriter().write(restResult);
    }
}
