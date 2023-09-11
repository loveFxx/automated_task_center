package com.sailvan.dispatchcenter.common.response;

import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;

/**
 * 响应统一返回类
 * @author
 * @date
 */
public class ApiResponse{


    public ApiResponseDomain success(String message,Object content){
        ApiResponseDomain response = new ApiResponseDomain();
        response.setCode(ResponseCode.SUCCESS_CODE);
        response.setMsg(message);
        response.setContent(content);
        return response;
    }

    public ApiResponseDomain error(int code, String message,Object content){
        ApiResponseDomain response = new ApiResponseDomain();
        response.setCode(code);
        response.setMsg(message);
        response.setContent(content);
        return response;
    }


}
