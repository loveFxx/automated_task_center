package com.sailvan.dispatchcenter.core.controller;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.constant.RequestCountCode;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;
import com.sailvan.dispatchcenter.common.pipe.BusinessService;
import com.sailvan.dispatchcenter.common.pipe.TaskResultService;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.common.response.PageDataResultCommon;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.core.limit.Limit;
import com.sailvan.dispatchcenter.core.monitor.Monitor;
import com.sailvan.dispatchcenter.core.service.CoreTaskSourceListService;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.text.ParseException;

import static com.sailvan.dispatchcenter.common.constant.Constant.BUSINESS_PREFIX;

/**
 * 业务使用接口
 * @date 2021-04
 * @author menghui
 */
@RestController
@RequestMapping(BUSINESS_PREFIX)
public class BusinessController extends FallBackCommand{


    private static Logger logger = LoggerFactory.getLogger(BusinessController.class);

    @Resource
    HttpServletRequest httpServletRequest;

    @Autowired
    TaskResultService taskResultService;

    @Resource
    CoreTaskSourceListService coreTaskSourceListService;

    @Autowired
    BusinessService businessService;

    @Autowired
    RedisUtils redisUtils;


    @RequestMapping("/register")
    @ResponseBody
    @Limit(types = {"businessInterfaceLimit","businessLimitMonth"}, prefix = "register")
    public ApiResponseDomain register(@RequestBody String json, HttpServletResponse response) {
        long start = System.currentTimeMillis();
        JSONObject jsonObject = JSONObject.parseObject(json);
        ApiResponseDomain apiResponseDomain = businessService.registerBusiness(jsonObject, response);
        long end = System.currentTimeMillis();
        logger.info("业务端端注册 register system_name:{} , jsonObject:{}, time:{}ms", jsonObject.get("system_name"), jsonObject.toJSONString(), (end - start));
        return apiResponseDomain;
    }

    @RequestMapping(value = "/add_task_source", method = RequestMethod.POST)
    @ResponseBody
    @Monitor(type = Constant.ADD_TASK_SOURCE)
    @Limit(types = {"taskNameLimit","businessInterfaceLimit","businessLimitMonth"}, prefix = "add")
    @HystrixCommand(fallbackMethod = "businessResponseFallback")
    public Object addTaskSource(@RequestBody String params,HttpServletRequest httpServletRequest){
        long start = System.currentTimeMillis();
        Object systemName = httpServletRequest.getAttribute(Constant.AUTHORIZATION_BUSINESS_NAME);
        JSONObject jsonObject = JSONObject.parseObject(params);
        jsonObject.put("system_name",systemName);
        Object result = coreTaskSourceListService.addTaskSource(jsonObject);
        long end = System.currentTimeMillis();
        logger.info("添加任务库 add_task_source-----system_name:{}, params:{}, time:{}ms", systemName, params, (end - start));
        return result;
    }

    @RequestMapping(value = "/bulk_add_task_source", method = RequestMethod.POST)
    @ResponseBody
    @Monitor(type = Constant.ADD_TASK_SOURCE)
    @Limit(types = {"taskNameLimit","businessInterfaceLimit","businessLimitMonth"}, prefix = "add")
//    @HystrixCommand(fallbackMethod = "businessResponseFallback")
    public Object bulkAddTaskSource(@RequestBody String params,HttpServletRequest httpServletRequest) throws ParseException {
        long start = System.currentTimeMillis();
        Object systemName = httpServletRequest.getAttribute(Constant.AUTHORIZATION_BUSINESS_NAME);
        JSONObject jsonObject = JSONObject.parseObject(params);
        jsonObject.put("system_name",systemName);
        Object result = coreTaskSourceListService.bulkAddTaskSource(jsonObject);
        long end = System.currentTimeMillis();
        logger.info("添加任务库 bulk_add_task_source-----system_name:{}, params:{}, time:{}ms", systemName, params, (end - start));
        return result;
    }

    @SneakyThrows
    @RequestMapping(value = "/delete_task_source", method = RequestMethod.POST)
    @ResponseBody
    @Limit(types = {"businessInterfaceLimit","businessLimitMonth"}, prefix = "delete")
    public ApiResponseDomain deleteTaskSource(@RequestParam(value = "id") String id){
        long start = System.currentTimeMillis();
        Object systemName = httpServletRequest.getAttribute(Constant.AUTHORIZATION_BUSINESS_NAME);
        ApiResponseDomain apiResponseDomain = coreTaskSourceListService.deleteTaskSourceById(id);
        long end = System.currentTimeMillis();
        logger.info("删除任务库 delete_task_source-----system_name:{}, id:{}, time:{}ms", systemName, id, (end - start));
        return apiResponseDomain;
    }

    @RequestMapping(value = "/get_task_result", method = RequestMethod.GET)
    @ResponseBody
    @Limit(types = {"businessInterfaceLimit","businessLimitMonth"}, prefix = "list")
    public Object listTaskResult(@RequestParam(value = "pageNum",required = false) Integer pageNum, @RequestParam(value = "pageSize",required = false) Integer pageSize,
                                 @RequestParam("taskSourceId") String taskSourceId,@RequestParam(value = "startDate",required = false) String startDate,@RequestParam(value = "endDate",required = false) String endDate) {
        long start = System.currentTimeMillis();
        Object systemName = httpServletRequest.getAttribute(Constant.AUTHORIZATION_BUSINESS_NAME);

        ApiResponse apiResponse = new ApiResponse();
        PageDataResultCommon pdr = new PageDataResultCommon();
        try {
            pdr = (PageDataResultCommon) taskResultService.listTaskResultByTaskSourceId(pageNum,pageSize,taskSourceId, pdr,startDate,endDate);
            long end = System.currentTimeMillis();
            logger.info("获取任务结果 get_task_result-----system_name:{}, taskSourceId:{}, pageNum:{}, pageSize:{}, totals:{}, time:{}ms",systemName, taskSourceId, pageNum, pageSize, pdr.getTotals(), (end - start));
            if (logger.isTraceEnabled()) {
                logger.info("获取任务结果 get_task_result-----system_name:{}, taskSourceId:{}, pageNum:{}, pageSize:{}, pdr:{}",systemName, taskSourceId, pageNum, pageSize, pdr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            apiResponse.error(ResponseCode.ERROR_CODE,"服务端报错", null);
            logger.error("获取任务结果 error get_task_result-----system_name:{}, taskSourceId:{}, pageNum:{}, pageSize:{}",systemName, taskSourceId, pageNum, pageSize);
        }
        return pdr;
    }
}
