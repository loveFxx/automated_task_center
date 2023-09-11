package com.sailvan.dispatchcenter.core.controller;

import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.constant.RequestCountCode;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.core.util.RequestCountUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.sailvan.dispatchcenter.common.constant.Constant.AUTHORIZATION_CLIENT_URL_NAME;
import static com.sailvan.dispatchcenter.common.constant.Constant.AUTHORIZATION_DECRYPT;

/**
 *  超时和异常熔断
 *  @author mh
 *  @date 2021-12
 */
@RestController
public class FallBackCommand extends BaseController{

    @Autowired
    RequestCountUtils requestCountUtils;

    private static Logger logger = LoggerFactory.getLogger(FallBackCommand.class);

    public ApiResponseDomain clientResponseFallback(Map jsonMap, HttpServletRequest httpServletRequest) {
        String ip = String.valueOf(httpServletRequest.getAttribute(AUTHORIZATION_DECRYPT));
        String method = String.valueOf(httpServletRequest.getAttribute(AUTHORIZATION_CLIENT_URL_NAME));
        ApiResponse apiResponse = new ApiResponse();
        logger.error("clientResponseFallback 客户端接口响应超时 timeout... ip:{} , method:{}", ip, method);
        requestCountUtils.recordRequest(RequestCountCode.REQUEST_TIMEOUT , "client",method);
        return apiResponse.error(ResponseCode.ERROR_CODE, "客户端接口响应超时 timeout...！", null);
    }

    public ApiResponseDomain clientResponseFallback(Map jsonMap, HttpServletRequest httpServletRequest, Throwable throwable) {
        String ip = String.valueOf(httpServletRequest.getAttribute(AUTHORIZATION_DECRYPT));
        String method = String.valueOf(httpServletRequest.getAttribute(AUTHORIZATION_CLIENT_URL_NAME));
        ApiResponse apiResponse = new ApiResponse();
        requestCountUtils.recordRequest(RequestCountCode.REQUEST_EXCEPTION , "client",method);
        logger.error("clientResponseFallback throwable 客户端接口响应异常 throwable... ip:{} , method:{}, error meg:{}", ip, method, throwable.getMessage());
        return apiResponse.error(ResponseCode.ERROR_CODE, "客户端接口响应异常 throwable...！"+throwable.getMessage(), null);
    }

    public ApiResponseDomain businessResponseFallback(String params, HttpServletRequest httpServletRequest) {
        Object systemName = httpServletRequest.getAttribute(Constant.AUTHORIZATION_BUSINESS_NAME);
        Object method = httpServletRequest.getAttribute(Constant.AUTHORIZATION_REQUEST_URL_NAME);
        ApiResponse apiResponse = new ApiResponse();
        requestCountUtils.recordRequest(RequestCountCode.REQUEST_TIMEOUT , systemName, String.valueOf(method));
        logger.error("businessResponseFallback 业务系统请求接口响应超时 timeout... systemName:{} , method:{}", systemName, method);
        return apiResponse.error(ResponseCode.ERROR_CODE, "业务系统请求接口响应超时 timeout...！", null);
    }

    public ApiResponseDomain businessResponseFallback(String params, HttpServletRequest httpServletRequest, Throwable throwable) {
        Object systemName = httpServletRequest.getAttribute(Constant.AUTHORIZATION_BUSINESS_NAME);
        Object method = httpServletRequest.getAttribute(Constant.AUTHORIZATION_REQUEST_URL_NAME);
        ApiResponse apiResponse = new ApiResponse();
        requestCountUtils.recordRequest(RequestCountCode.REQUEST_EXCEPTION , systemName, String.valueOf(method));
        logger.error("businessResponseFallback throwable 业务系统请求接口响应异常 throwable... systemName:{} , method:{}, error meg:{}", systemName, method, throwable.getMessage());
        return apiResponse.error(ResponseCode.ERROR_CODE, "业务系统请求接口响应异常 throwable...！"+throwable.getMessage(), null);
    }

}
