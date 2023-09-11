package com.sailvan.dispatchcenter.data.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.cache.*;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.AwsUserRegionFunction;
import com.sailvan.dispatchcenter.common.pipe.LambdaUserService;
import com.sailvan.dispatchcenter.common.pipe.RegionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @program: automated_task_center
 * @description:需要查表的下拉选择框api集中放到这里了
 * @author: Wu Xingjian
 * @create: 2021-07-01 09:38
 **/
@RestController
public class DropDownListController {

    private static Logger logger = LoggerFactory.getLogger(TaskLogsController.class);

    @Autowired
    InitTaskCache initTaskCache;

    @Autowired
    InitAccountCache initAccountCache;

    @Autowired
    InitSystemCache initSystemCache;

    @Autowired
    InitPlatformCache initPlatformCache;

    @Autowired
    RegionService regionService;

    @Autowired
    InitLambdaCache initLambdaCache;

    @Autowired
    LambdaUserService lambdaUserService;

    /**
     * 获取{taskName:taskName}类型下拉选择框
     *
     */
    @RequestMapping(value = "/getDropDownTaskName")
    @ResponseBody
    public JSONArray getDropDownTaskName() {
        if (logger.isTraceEnabled()) {
            logger.trace("getDropDownTaskName {} ", initTaskCache.getTaskNameMapCache());
        }
        return initTaskCache.getTaskNameMapCache();
    }

    /**
     * 获取account下拉选择框
     *
     */
    @RequestMapping(value = "/getDropDownAccount")
    @ResponseBody
    public JSONArray getDropDownAccount() {
        if (logger.isTraceEnabled()) {
            logger.trace("getDropDownAccount   {} ", initAccountCache.getAccountMap());
        }
        return initAccountCache.getAccountMap();
    }


    /**
     * 获取{taskName:taskId}类型下拉选择框
     *
     */
    @RequestMapping(value = "/getDropDownTaskId")
    @ResponseBody
    public JSONArray getDropDownTaskId() {
        if (logger.isTraceEnabled()) {
            logger.trace("getDropDownTaskId {}", initTaskCache.getTaskIdMapCache());
        }
        return initTaskCache.getTaskIdMapCache();
    }

    /**
     * 获取System下拉选择框
     *
     */
    @RequestMapping(value = "/getDropDownSystem")
    @ResponseBody
    public JSONArray getDropDownSystem() {
        if (logger.isTraceEnabled()) {
            logger.trace("getDropDownSystem {} ", initSystemCache.getSystemNameMapCache());
        }
        return initSystemCache.getSystemNameMapCache();
    }


    @RequestMapping(value = "/getCrawlPlatform")
    @ResponseBody
    public JSONArray getCrawlPlatform() {
        if (logger.isTraceEnabled()) {
            logger.trace("getDropDownSystem {} ", initPlatformCache.getCrawlPlatformSelectCache());
        }
        return initPlatformCache.getCrawlPlatformSelectCache();
    }


    /**
     * 不走缓存 从常量获取 写死了
     * @return
     */
    @RequestMapping(value = "/getStoreAccountStatus")
    @ResponseBody
    public JSONArray getStoreAccountStatus() {


        JSONArray jsonArray = new JSONArray();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name","正常（未运营）");
        jsonObject.put("value", Constant.ACCOUNT_STATUS_NORMAL_NOT_OPERATION);
        jsonArray.add(jsonObject);

        jsonObject = new JSONObject();
        jsonObject.put("name","正常（运营中）");
        jsonObject.put("value", Constant.ACCOUNT_STATUS_NORMAL_IN_OPERATION);
        jsonArray.add(jsonObject);

        jsonObject = new JSONObject();
        jsonObject.put("name","关店（不可登录）");
        jsonObject.put("value", Constant.ACCOUNT_STATUS_CLOSE_SHOP_CANNOT_LOGIN);
        jsonArray.add(jsonObject);

        jsonObject = new JSONObject();
        jsonObject.put("name","关店（可登录）");
        jsonObject.put("value", Constant.ACCOUNT_STATUS_CLOSE_SHOP_CAN_LOGIN);
        jsonArray.add(jsonObject);

        jsonObject = new JSONObject();
        jsonObject.put("name","暂停运营（假期模式）");
        jsonObject.put("value", Constant.ACCOUNT_STATUS_SUSPENSION_OPERATIONS);
        jsonArray.add(jsonObject);

        jsonObject = new JSONObject();
        jsonObject.put("name","无效店铺");
        jsonObject.put("value", Constant.ACCOUNT_STATUS_INVALID_SHOP);
        jsonArray.add(jsonObject);



        return jsonArray;
    }


    /**
     * 不走缓存 不走常量 写死了
     * @return
     */
    @RequestMapping(value = "/getAccountSiteStatus")
    @ResponseBody
    public JSONArray getAccountSiteStatus() {


        JSONArray jsonArray = new JSONArray();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name","正常");
        jsonObject.put("value",1);
        jsonArray.add(jsonObject);

        jsonObject = new JSONObject();
        jsonObject.put("name","不正常");
        jsonObject.put("value", 0);
        jsonArray.add(jsonObject);

        return jsonArray;
    }


    /**
     * 获取区域类型下拉选择框
     *
     */
    @RequestMapping(value = "/getRegionId")
    @ResponseBody
    public JSONArray getRegionId() {
//        if (logger.isTraceEnabled()) {
//            logger.trace("getDropDownTaskId {}", initTaskCache.getTaskIdMapCache());
//        }
        JSONArray jsonArray = initLambdaCache.getRegionIdAndNameCache();
        return jsonArray;
    }

    /**
     * 获取lambda函数类型下拉选择框
     *
     */
    @RequestMapping(value = "/getFunctionName")
    @ResponseBody
    public JSONArray getFunctionName() {
//        if (logger.isTraceEnabled()) {
//            logger.trace("getDropDownTaskId {}", initTaskCache.getTaskIdMapCache());
//        }
        JSONArray jsonArray = initLambdaCache.getFunctionNameCache();
        return jsonArray;
    }

    /**
     * 获取lambda函数类型下拉选择框
     *
     */
    @RequestMapping(value = "/getLambdaUserName")
    @ResponseBody
    public JSONArray getLambdaUserName() {

        JSONArray jsonArray = initLambdaCache.getLambdaUserName();
        return jsonArray;
    }

    /**
     * 获取任务配置页面 区域函数名选择框内容
     *
     */
    @RequestMapping(value = "/getRegionFunctionName")
    @ResponseBody
    public JSONArray getRegionFunctionName() {

        JSONArray jsonArray = initLambdaCache.getRegionFunctionName();
        return jsonArray;
    }

    @RequestMapping(value = "/getUserRegionFunctions")
    @ResponseBody
    public List<AwsUserRegionFunction> getUserRegionFunctions(){

        List<AwsUserRegionFunction> awsUserRegionFunctions
                = lambdaUserService.getUserRegionFunctions();

        return awsUserRegionFunctions;
    }

}
