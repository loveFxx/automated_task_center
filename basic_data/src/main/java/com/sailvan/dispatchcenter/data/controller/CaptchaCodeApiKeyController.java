package com.sailvan.dispatchcenter.data.controller;

import com.sailvan.dispatchcenter.common.cache.InitValidVersionCache;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;
import com.sailvan.dispatchcenter.common.domain.CaptchaCodeApiKey;
import com.sailvan.dispatchcenter.common.pipe.CaptchaCodeApiKeyService;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 编码管理
 * @date 2021-08
 * @author menghui
 */
@RestController

public class CaptchaCodeApiKeyController {

    private static Logger logger = LoggerFactory.getLogger(VersionController.class);

    @Autowired
    CaptchaCodeApiKeyService captchaCodeApiKeyService;

    @Autowired
    InitValidVersionCache initValidVersionCache;


    @RequestMapping(value = "/getCaptchaCodeApiKey", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult getCaptchaCode(){

        CaptchaCodeApiKey codeApiKey = new CaptchaCodeApiKey();
        PageDataResult pdResult = new PageDataResult();
        codeApiKey = captchaCodeApiKeyService.getCaptchaCodeApiKey();
        List<CaptchaCodeApiKey> codeApiKeyList = new ArrayList<>();
        codeApiKeyList = Arrays.asList(codeApiKey);
        pdResult.setList(codeApiKeyList);
        return pdResult;
    }


    @RequestMapping(value = "/updateApiKey", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain updateApiKey(CaptchaCodeApiKey captchaCodeApiKey) {
        int result;
        ApiResponse apiResponse = new ApiResponse();
        if (captchaCodeApiKey.getApiKey() == ""){
            return apiResponse.error(ResponseCode.ERROR_CODE,"编码不能为空",null);
        }
        result = captchaCodeApiKeyService.update(captchaCodeApiKey);
        if (result > 0){
            return apiResponse.success("修改成功",result);
        }else{
            return apiResponse.error(ResponseCode.ERROR_CODE,"修改失败",null);
        }
    }
}
