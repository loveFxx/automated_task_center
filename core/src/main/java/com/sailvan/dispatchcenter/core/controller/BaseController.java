package com.sailvan.dispatchcenter.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;
import org.apache.commons.lang.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

@RestController
public class BaseController {
    /**
     * 获取传递的字符串
     *
     * @param request
     * @return
     */
    protected String getRequestBody(HttpServletRequest request) {
        try {
            ServletInputStream inputStream = request.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object parseRequestBody(Map jsonMap, HttpServletRequest httpServletRequest){
        String contentType =  httpServletRequest.getContentType();
        String characterEncoding = httpServletRequest.getCharacterEncoding();
        String contentEncoding = httpServletRequest.getHeader("Content-Encoding");
        ApiResponseDomain apiResponseDomain = new ApiResponseDomain();

        if (!characterEncoding.equals(CharEncoding.UTF_8)){
            apiResponseDomain.setCode(ResponseCode.ERROR_CODE);
            apiResponseDomain.setMsg("Not supported character-"+characterEncoding+",please used UTF-8");
            return apiResponseDomain;
        }
        JSONObject jsonObject;
        if (contentType.equals(MediaType.APPLICATION_FORM_URLENCODED_VALUE)){
            if (null != contentEncoding && contentEncoding.indexOf("gzip") != -1) {
                apiResponseDomain.setCode(ResponseCode.ERROR_CODE);
                apiResponseDomain.setMsg(MediaType.APPLICATION_FORM_URLENCODED_VALUE + " not supported Content-Encoding:gzip");
                return apiResponseDomain;
            }
            jsonObject = JSONObject.parseObject(JSON.toJSONString(jsonMap));
        }else {
            String requestBody = getRequestBody(httpServletRequest);
            jsonObject = JSONObject.parseObject(requestBody);
        }
        if (jsonObject == null || jsonObject.isEmpty()){
            apiResponseDomain.setCode(ResponseCode.ERROR_CODE);
            apiResponseDomain.setMsg("Request body is empty");
            return apiResponseDomain;
        }
        return jsonObject;
    }
}
