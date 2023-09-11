package com.sailvan.dispatchcenter.core.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.sailvan.dispatchcenter.common.constant.CacheKey;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.common.util.WriteToFileUtil;
import com.sailvan.dispatchcenter.core.service.ClientService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.text.ParseException;
import java.util.*;

import static com.sailvan.dispatchcenter.common.constant.Constant.*;


/**
 * 客户端接口
 *
 * @author menghui
 * @date 2021-04
 */
@RestController
@RequestMapping(CLIENT_PREFIX)
public class ClientController extends FallBackCommand{

    private static Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Autowired
    ClientService clientService;

    /**
     * 客户端注册机器接口
     */
    @RequestMapping("/register")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "clientResponseFallback")
    public ApiResponseDomain register(@RequestParam Map jsonMap, HttpServletRequest httpServletRequest) {
        long start = System.currentTimeMillis();
        Object o = parseRequestBody(jsonMap, httpServletRequest);
        if (o instanceof ApiResponseDomain){
            return (ApiResponseDomain)o;
        }
        JSONObject jsonObject = (JSONObject) o;

        ApiResponseDomain apiResponseDomain = clientService.registerClient(jsonObject);
        long end = System.currentTimeMillis();
        logger.info("客户端注册 register unique_id:{} , jsonObject:{}, time:{}ms", jsonObject.get("unique_id"), jsonObject.toJSONString(), (end-start));
        return apiResponseDomain;
    }


    /**
     * 代理IP接口 爬虫才会使用,亚马逊任务代理IP需要在任务里面带上
     * work_type 大类型  type小类型
     *
     * @return
     */
    @RequestMapping("/getProxy")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "clientResponseFallback")
    public ApiResponseDomain getProxy(@RequestParam Map jsonMap, HttpServletRequest httpServletRequest) {
        long start = System.currentTimeMillis();
        String ip = String.valueOf(httpServletRequest.getAttribute(AUTHORIZATION_DECRYPT));
        Object o = parseRequestBody(jsonMap, httpServletRequest);
        if (o instanceof ApiResponseDomain){
            return (ApiResponseDomain)o;
        }
        JSONObject jsonObject = (JSONObject) o;
        ApiResponseDomain crawlPlatformProxy = clientService.getCrawlPlatformProxy(jsonObject, ip);
        long end = System.currentTimeMillis();
        logger.info("获取代理IP getProxy  机器ip:{} jsonObject:{}, time:{}ms", ip, jsonObject.toJSONString(), (end-start));
        return crawlPlatformProxy;
    }

    /**
     * 代理IP 被禁接口
     *
     * @return
     */
    @RequestMapping(value = "/bannedProxy")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "clientResponseFallback")
    public ApiResponseDomain bannedProxy(@RequestParam Map jsonMap, HttpServletRequest httpServletRequest) {
        long start = System.currentTimeMillis();
        String ip = String.valueOf(httpServletRequest.getAttribute(AUTHORIZATION_DECRYPT));
        Object o = parseRequestBody(jsonMap, httpServletRequest);
        if (o instanceof ApiResponseDomain){
            return (ApiResponseDomain)o;
        }
        JSONObject jsonObject = (JSONObject) o;
        ApiResponseDomain apiResponseDomain = clientService.disableProxyIp(jsonObject, ip);
        long end = System.currentTimeMillis();
        logger.info("禁用代理IP bannedProxy 机器IP:{},  jsonObject {}, time:{}ms", ip, jsonObject.toJSONString(), (end-start));
        return apiResponseDomain;
    }

    /**
     * 代理IP移除接口
     * @param jsonMap
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/removeProxy")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "clientResponseFallback")
    public ApiResponseDomain removeProxy(@RequestParam Map jsonMap, HttpServletRequest httpServletRequest) {
        long start = System.currentTimeMillis();
        String ip = String.valueOf(httpServletRequest.getAttribute(AUTHORIZATION_DECRYPT));
        Object o = parseRequestBody(jsonMap, httpServletRequest);
        if (o instanceof ApiResponseDomain){
            return (ApiResponseDomain)o;
        }
        JSONObject jsonObject = (JSONObject) o;
        ApiResponseDomain apiResponseDomain = clientService.removeProxyIp(jsonObject);
        long end = System.currentTimeMillis();
        logger.info("移除代理IP removeProxy 机器IP:{},  jsonObject {}, time:{}ms", ip, jsonObject.toJSONString(), (end-start));
        return apiResponseDomain;
    }

    /**
     * 心跳包接口
     *
     * @return
     */
    @RequestMapping(value = "/heartBeat")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "clientResponseFallback")
    public ApiResponseDomain heartBeat(@RequestParam Map jsonMap, HttpServletRequest httpServletRequest) {
        long start = System.currentTimeMillis();
        String ip = String.valueOf(httpServletRequest.getAttribute(AUTHORIZATION_DECRYPT));
        String token = String.valueOf(httpServletRequest.getAttribute(AUTHORIZATION_ENCRYPT));

        Object o = parseRequestBody(jsonMap, httpServletRequest);
        if (o instanceof ApiResponseDomain){
            return (ApiResponseDomain)o;
        }
        JSONObject jsonObject = (JSONObject) o;
        ApiResponseDomain apiResponseDomain = clientService.heartBeat(jsonObject, ip, token);
        long end = System.currentTimeMillis();
        logger.info("心跳 heartBeat ip {}, jsonObject {}, time:{}ms", ip, jsonObject.toJSONString(), (end-start));
        return apiResponseDomain;

    }

    /**
     * 验证码接口 图片
     * 根据account判断 一天50此
     * @return
     */
    @RequestMapping(value = "/captcha_code")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "clientResponseFallback")
    public ApiResponseDomain captchaCode(@RequestParam Map jsonMap, HttpServletRequest httpServletRequest) {
        long start = System.currentTimeMillis();
        String ip = String.valueOf(httpServletRequest.getAttribute(AUTHORIZATION_DECRYPT));
        Object o = parseRequestBody(jsonMap, httpServletRequest);
        if (o instanceof ApiResponseDomain){
            return (ApiResponseDomain)o;
        }
        JSONObject jsonObject = (JSONObject) o;
        ApiResponseDomain apiResponseDomain = clientService.captchaCode(jsonObject, ip);
        long end = System.currentTimeMillis();
        logger.info("获取图片验证码接口 captcha_code ip:{},传入参数 jsonObject {}, time:{}ms", ip, jsonObject.toJSONString(), (end - start));
        return apiResponseDomain;
    }


    /**
     * 二步验证接口,google认证 通过qrContent获取
     *
     * @return
     */
    @RequestMapping(value = "/account_verify_code")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "clientResponseFallback")
    public ApiResponseDomain accountVerifyCode(@RequestParam Map jsonMap, HttpServletRequest httpServletRequest) {
        long start = System.currentTimeMillis();
        String ip = String.valueOf(httpServletRequest.getAttribute(AUTHORIZATION_DECRYPT));
        Object o = parseRequestBody(jsonMap, httpServletRequest);
        if (o instanceof ApiResponseDomain){
            return (ApiResponseDomain)o;
        }
        JSONObject jsonObject = (JSONObject) o;
        ApiResponseDomain apiResponseDomain = clientService.accountVerifyCode(jsonObject, ip);
        long end = System.currentTimeMillis();
        logger.info("获取google认证验证码接口 account_verify_code ip:{}, jsonObject {}, time:{}ms", ip, jsonObject.toJSONString(), (end - start));
        return apiResponseDomain;
    }

    /**
     * 获取csrfToken
     * @return
     */
    @RequestMapping(value = "/getCsrfToken")
    @ResponseBody
    public ApiResponseDomain getCsrfToken() {
        ApiResponse apiResponse = new ApiResponse();
        JSONObject jsonObject = new JSONObject();

        String csrfToken = WriteToFileUtil.readId(CacheKey.CSRF_TOKEN);
        if (csrfToken != null){
            jsonObject.put("csrf_token",csrfToken);
        }else {
            jsonObject.put("csrf_token","");
        }

        return apiResponse.success("success", jsonObject);
    }

    /**
     * 设置csrfToken-本地缓存
     * @return
     */
    @RequestMapping(value = "/setCsrfToken")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "clientResponseFallback")
    public ApiResponseDomain setCsrfToken(@RequestParam Map jsonMap, HttpServletRequest httpServletRequest) {
        ApiResponse apiResponse = new ApiResponse();

        Object o = parseRequestBody(jsonMap, httpServletRequest);
        if (o instanceof ApiResponseDomain){
            return (ApiResponseDomain)o;
        }
        JSONObject jsonObject = (JSONObject) o;
        Object csrfToken = jsonObject.get("csrf_token");
        if (!StringUtils.isEmpty(csrfToken)){
            WriteToFileUtil.writeId(CacheKey.CSRF_TOKEN,String.valueOf(csrfToken));
        }
        return apiResponse.success("success", "");
    }

    @RequestMapping("/set_proxy_info")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "clientResponseFallback")
    public ApiResponseDomain setProxyInfo(@RequestParam Map jsonMap, HttpServletRequest httpServletRequest) {
        String ip = String.valueOf(httpServletRequest.getAttribute(AUTHORIZATION_DECRYPT));
        Object o = parseRequestBody(jsonMap, httpServletRequest);
        if (o instanceof ApiResponseDomain){
            return (ApiResponseDomain)o;
        }
        JSONObject jsonObject = (JSONObject) o;

        return clientService.setProxyInfo(jsonObject, ip);
    }

    /**
     * 记录客户端返回的http请求日志
     * @param jsonMap
     * @param httpServletRequest
     * @return
     */
    @RequestMapping("/reportRequestLog")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "clientResponseFallback")
    public ApiResponseDomain reportRequestLog(@RequestParam Map jsonMap, HttpServletRequest httpServletRequest) throws ParseException {
        String ip = String.valueOf(httpServletRequest.getAttribute(AUTHORIZATION_DECRYPT));
        Object o = parseRequestBody(jsonMap, httpServletRequest);
        if (o instanceof ApiResponseDomain){
            return (ApiResponseDomain)o;
        }
        JSONObject jsonObject = (JSONObject) o;
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        ApiResponse apiResponse = new ApiResponse();
        clientService.reportRequestLog(jsonArray, ip);
        return apiResponse.success("success", "");
    }
}
