package com.sailvan.dispatchcenter.common.pipe;


import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;

import javax.servlet.http.HttpServletResponse;

/**
 * @author menghui
 * @date 21-08
 */
public abstract interface  BusinessService {


    public ApiResponseDomain registerBusiness(JSONObject businessSystem, HttpServletResponse response);
}
