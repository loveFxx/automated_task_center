package com.sailvan.dispatchcenter.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;
import com.sailvan.dispatchcenter.core.service.ClientService;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.Map;

import static com.sailvan.dispatchcenter.common.constant.Constant.*;


/**
 * 客户端接口
 *
 * @author menghui
 * @date 2021-11
 */
@RestController
@RequestMapping(CLIENT_PREFIX)
public class ClientGetJobController extends FallBackCommand{

    private static Logger logger = LoggerFactory.getLogger(ClientGetJobController.class);

    @Autowired
    ClientService clientService;


    /**
     * 获取任务
     *
     * @return
     */
    @RequestMapping(value = "/get_job")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "clientResponseFallback")
    public ApiResponseDomain getTask(@RequestParam Map jsonMap, HttpServletRequest httpServletRequest) {
        long start = System.currentTimeMillis();
        String ip = String.valueOf(httpServletRequest.getAttribute(AUTHORIZATION_DECRYPT));
        Object o = parseRequestBody(jsonMap, httpServletRequest);
        if (o instanceof ApiResponseDomain){
            return (ApiResponseDomain)o;
        }
        JSONObject jsonObject = (JSONObject) o;
        ApiResponseDomain task = clientService.getTask(jsonObject, ip);
        long end = System.currentTimeMillis();
        boolean resultFlag = false;
        Object content = task.getContent();
        if(!StringUtils.isEmpty(content)){
            try{
                if (!((LinkedList)content).isEmpty()) {
                    resultFlag = true;
                }
            }catch (Exception e){
            }
        }
        logger.info("获取任务 get_job ip:{},传入参数 jsonObject {}, 结果是否有值 resultFlag:{}, time:{}ms", ip, jsonObject.toJSONString(), resultFlag, (end-start));
        return task;
    }

}
