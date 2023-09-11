package com.sailvan.dispatchcenter.data.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.sailvan.dispatchcenter.common.cache.ProxyIPPool;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.cache.InitPlatformCache;
import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.db.service.ProxyIpPlatformService;
import com.sailvan.dispatchcenter.db.service.ProxyIpService;
import com.sailvan.dispatchcenter.db.service.ProxyIpShopService;
import com.sailvan.dispatchcenter.common.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * @program: automated_task_center
 * @description:
 * @author: Wu Xingjian
 * @create: 2021-05-10 15:10
 **/
@RestController
public class ProxyIpController {

    private static Logger logger = LoggerFactory.getLogger(ProxyIpController.class);

    @Autowired
    ProxyIpService proxyIpService;

    @Autowired
    ProxyIpPlatformService proxyIpPlatformService;

    @Autowired
    ProxyIpShopService proxyIpShopService;

    @Autowired
    InitPlatformCache initPlatformCache;

    @Autowired
    ProxyIPPool proxyIPPool;



    @RequestMapping(value = "/refreshCrawlPlatform", method = RequestMethod.POST)
    @ResponseBody
    public void refreshCrawlPlatform() {
        logger.info("refreshCrawlPlatform");
        proxyIpService.refreshCrawlPlatform();
    }


    @RequestMapping(value = "/refreshProxyIPPlatform", method = RequestMethod.POST)
    @ResponseBody
    public void refreshProxyIPPlatform() {
        logger.info("refreshProxyIPPlatform");
//        proxyIpService.refreshProxyIPPlatform();
        proxyIpService.refreshProxyIPLargeTaskType();
    }






    @RequestMapping(value = "/getProxyIpList", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult getProxyIpList(@RequestParam("pageNum") Integer pageNum,
                                         @RequestParam("pageSize") Integer pageSize, ProxyIp proxyIp) {
        PageDataResult pdr = new PageDataResult();
        try {

            int pageNumTmp = CommonUtils.getPageNum("proxyIp", proxyIp.toString());

            if(null == pageNum || pageNumTmp == 1) {
                pageNum = 1;
            }
            if (null == pageSize) {
                pageSize = 10;
            }
            pdr = proxyIpService.getProxyIpList(proxyIp, pageNum, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pdr;
    }

    @RequestMapping(value = "/proxyIPPlatform", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult proxyIpPlatform(@RequestParam("pageNum") Integer pageNum,
                                         @RequestParam("pageSize") Integer pageSize, ProxyIpPlatform proxyIpPlatform) {
        PageDataResult pdr = new PageDataResult();
        try {
            if (null == pageNum) {
                pageNum = 1;
            }
            if (null == pageSize) {
                pageSize = 10;
            }
            pdr = proxyIpPlatformService.getProxyIpPlatformList(proxyIpPlatform, pageNum, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pdr;
    }

    @RequestMapping(value = "/deleteProxyIP", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain deleteProxyIp(Integer id) {
        int result = proxyIpService.delete(id);
        ApiResponse apiResponse = new ApiResponse();
        if (result > 0) {
            return apiResponse.success("删除成功", result);
        } else {
            return apiResponse.error(ResponseCode.ERROR_CODE, "删除id:" + id + "失败", null);
        }
    }

    @RequestMapping(value = "/updateProxyIP", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain updateProxyIp(ProxyIp proxyIp) {
        int result;
        ApiResponse apiResponse = new ApiResponse();
        if (!StringUtils.isEmpty(proxyIp.getId())) {
            ProxyIp oldProxyIp = proxyIpService.getProxyIpByIp(proxyIp.getIp());
            result = proxyIpService.update(proxyIp);
            if (result > 0) {
                logger.debug("updateProxyIP proxyIp{}",proxyIp);
                return apiResponse.success("更新成功", result);
            } else {
                logger.error("updateProxyIP proxyIp{}",proxyIp);
                return apiResponse.error(ResponseCode.ERROR_CODE, "更新失败", null);
            }
        } else {
            result = proxyIpService.insert(proxyIp);
            if (result > 0) {
                logger.debug("ProxyIP insert proxyIp{}",proxyIp);
                return apiResponse.success("添加成功", result);
            } else {
                logger.error("ProxyIP insert proxyIp{}",proxyIp);
                return apiResponse.error(ResponseCode.ERROR_CODE, "添加失败", null);
            }
        }
    }

    @RequestMapping(value = "/updateProxyStatus", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain updateProxyStatus(@RequestBody JSONObject obj) {
        int result;
        ApiResponse apiResponse = new ApiResponse();

        int validStatus = obj.getIntValue("validStatus");
        int id = obj.getIntValue("id");
        result = proxyIpService.updateProxyStatus(validStatus,id);

        if (validStatus == 1){
            proxyIPPool.pushProxy(id);
        }else {
            proxyIPPool.removeProxy(id);
        }
        if (result > 0) {
            return apiResponse.success("更新成功", result);
        } else {

            return apiResponse.error(ResponseCode.ERROR_CODE, "更新失败", null);
        }
    }


    /**
     * 批量删除任务
     *
     * @param obj
     * @return
     */
    @RequestMapping(value = "/batchDeleteProxyIP", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain batchDeleteProxyIp(@RequestBody JSONObject obj) {
        String ids = obj.getString("ids");
        JSONArray idsArray = JSONArray.parseArray(ids);
        logger.debug("batchDeleteProxyIP ids {}",ids);
        ApiResponse apiResponse = new ApiResponse();
        for (int i = 0; i < idsArray.size(); i++) {
            Integer id = Integer.valueOf(idsArray.get(i).toString());
            int result = proxyIpService.delete(id);
            if (result <= 0) {
                return apiResponse.error(ResponseCode.ERROR_CODE, "系统出现异常，删除id:" + id + "失败", null);
            }
        }

        return apiResponse.success("批量删除成功", null);
    }

    /**
     *  获取代理IP 在客户端拉取任务时，需要携带的代理IP
     * @param account
     * @param continents
     * @param platform
     * @return
     */
    @RequestMapping(value = "/getProxyIP")
    @ResponseBody
    public synchronized String getProxyIp(@RequestParam(required = false, value = "account") String account,
                                              @RequestParam(required = false, value = "continents") String continents,
                                              @RequestParam(required = false, value = "platform") String platform) {

        Object proxyIp = proxyIpService.getProxyIp(account, continents, platform);
        if(proxyIp instanceof StoreAccount){
            return ((StoreAccount) proxyIp).getProxyIp();
        }
        return String.valueOf(proxyIp);
    }

    @RequestMapping(value = "/getplatformProxyIpStatus", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult getplatformProxyIpStatus() {

        PageDataResult pdr = proxyIpService.getPlatformProxyIpStatus();
        return pdr;
    }

    @RequestMapping(value = "/getProxyIpNum", method = RequestMethod.POST)
    @ResponseBody
    public ProxyIpMonitor getProxyIpNum() {

        ProxyIpMonitor proxyIpMonitor= proxyIpService.getProxyIpNum();
        return proxyIpMonitor;
    }


    @RequestMapping(value = "/batchSetRate", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain batchSetRate(@RequestBody JSONObject obj) {
        String ids = obj.getString("proxyIds");
        String[] idsArray = ids.split(",");

        int limitHour = Integer.parseInt(obj.getString("limit_hour"));
        int limitHourTimes = Integer.parseInt(obj.getString("limit_hour_times"));
        int limitDay = Integer.parseInt(obj.getString("limit_day")) * 24;
        int limitDayTimes = Integer.parseInt(obj.getString("limit_day_times"));
        int unitTime = Integer.parseInt(obj.getString("rate_hour"));
        int maxBannedRate = Integer.parseInt(obj.getString("rate_hour_percent"));
        int delayTime = Integer.parseInt(obj.getString("rate_delay_hour"));
        LinkedHashMap<String,LinkedHashMap<String,Integer>> hashMap = new LinkedHashMap<>();
        LinkedHashMap<String,Integer> linkedHashMap = new LinkedHashMap<>();

        linkedHashMap.put("max_used_times",limitHourTimes);
        hashMap.put(String.valueOf(limitHour),linkedHashMap);
        LinkedHashMap<String,Integer> linkedHashMap2 = new LinkedHashMap<>();
        linkedHashMap2.put("max_used_times",limitDayTimes);
        hashMap.put(String.valueOf(limitDay),linkedHashMap2);

        String limitConfig = JSON.toJSONString(hashMap, SerializerFeature.DisableCircularReferenceDetect);
        proxyIpService.batchSetRate(limitConfig,unitTime,maxBannedRate,delayTime,idsArray);

        for (String id:idsArray) {
            ProxyIp proxyIp = proxyIpService.findProxyIpById(Integer.parseInt(String.valueOf(id)));
            List<String> platforms = proxyIpPlatformService.listPlatformByProxyId(proxyIp.getId());
            for (String platform:platforms) {
                proxyIPPool.addQueue(platform,System.currentTimeMillis(),proxyIp.getId());
            }

        }
        ApiResponse apiResponse = new ApiResponse();
        return apiResponse.success("批量更新成功", null);

    }
}

