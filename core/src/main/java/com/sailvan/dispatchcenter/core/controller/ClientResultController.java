package com.sailvan.dispatchcenter.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;

import com.sailvan.dispatchcenter.core.service.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.sailvan.dispatchcenter.common.constant.Constant.*;


/**
 * 客户端结果返回接口
 *
 * @author menghui
 * @date 2021-04
 */
@RestController
@RequestMapping(CLIENT_PREFIX)
public class ClientResultController extends FallBackCommand{

    private static Logger logger = LoggerFactory.getLogger(ClientResultController.class);

    @Resource
    private HttpServletRequest httpServletRequest;

    @Autowired
    ClientService clientService;

    /**
     * 客户端任务处理完成，返回给中心端
     *
     * @return
     */
    @RequestMapping(value = "/result_job")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "clientResponseFallback")
    public ApiResponseDomain taskResult(@RequestParam Map jsonMap, HttpServletRequest httpServletRequest) {
        long start = System.currentTimeMillis();
        String ip = String.valueOf(httpServletRequest.getAttribute(AUTHORIZATION_DECRYPT));
        Object o = parseRequestBody(jsonMap, httpServletRequest);
        if (o instanceof ApiResponseDomain){
            return (ApiResponseDomain)o;
        }
        JSONObject jsonObject = (JSONObject) o;
        ApiResponseDomain apiResponseDomain = new ApiResponseDomain();
        try {
            apiResponseDomain = clientService.taskResult(jsonObject,ip);
        }catch (Exception e){
            logger.error ("返回结果error result_job ip:{}  jsonObject:{} , msg:{}", ip, jsonObject.toJSONString(),e.getMessage());
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        logger.info("返回结果 result_job ip:{}  jsonObject:{} time:{}ms", ip, jsonObject.toJSONString(),(end-start));
        return apiResponseDomain;
    }
}
