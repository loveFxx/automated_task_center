package com.sailvan.dispatchcenter.stat.monitor.scheduler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.RequestCountCode;
import com.sailvan.dispatchcenter.common.domain.RequestCount;
import com.sailvan.dispatchcenter.common.pipe.RequestCountService;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.common.util.WeChatRobotUtils;
import com.sailvan.dispatchcenter.stat.monitor.config.WeChatRobotTokenConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *  请求数量落盘
 *  @author mh
 *  @date 2021-12
 */
@Component
public class RequestCountPersistence {

    private static Logger logger = LoggerFactory.getLogger(RequestCountPersistence.class);
    String[] methodClient = {"register","get_job","result_job","getProxy","bannedProxy","heartBeat","captcha_code","account_verify_code"};
    String[] methodBusiness = {"register","add_task_source","bulk_add_task_source","delete_task_source","get_task_result","pushAsync"};

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    RequestCountService requestCountService;

    @Autowired
    WeChatRobotTokenConfig wechatRobotTokenConfig;


    @Scheduled(cron = "0 1,58 */1 * * ?")
    public void persistence() throws IOException {
        refresh(1);
    }

    public void refresh(int day) throws IOException {
        String lastTime = DateUtils.getHourBeforeDate(day).substring(0,13).replace(" ","-");
        String[] stages = (new RequestCountCode()).stages;
        Object o = redisUtils.get("systemName:systemNameMap:");
        if (StringUtils.isEmpty(o)) {
            return;
        }
        JSONArray jsonArray = JSONArray.parseArray((String.valueOf(o)));
        List<String> nameList = new ArrayList<>();
        for (Object o1 : jsonArray) {
            Object name = ((JSONObject) o1).get("name");
            if (name == null){
                continue;
            }
            if(!nameList.contains(String.valueOf(name))){
                nameList.add(String.valueOf(name));
            }
        }
        nameList.add("client");
        StringBuilder stringBuilder = new StringBuilder();
        for (String name : nameList) {
            if(name.equals("client")){
                for (String method : methodClient) {
                    updateRequestCount( lastTime,  name,  method,  stages, stringBuilder);
                }
            }else {
                for (String method : methodBusiness) {
                    updateRequestCount( lastTime,  name,  method,  stages, stringBuilder);
                }
            }
        }
        if (stringBuilder.length()>0){
            WeChatRobotUtils weChatRobotUtils = new WeChatRobotUtils(wechatRobotTokenConfig.getWechatRobotToken());
            weChatRobotUtils.text("Http请求统计\n 系统\t 请求数\t 请求成功数\t 请求异常数\n" + stringBuilder.toString(), new String[]{}, new String[]{});
        }
    }

    private void updateRequestCount(String lastTime, String name, String method, String[] stages,StringBuilder stringBuilder){
        int success = 0;
        int num = 0;
        int timeout = 0;
        int exception = 0;
        RequestCount  requestCount = new RequestCount();
        requestCount.setPeriod(lastTime);
        requestCount.setSystemName(name);
        requestCount.setRequestMethod(method);
        for (String stage : stages) {
            String key = "dispatch_center:request:"+lastTime+":"+name+":"+method+":"+stage;
            Object o2 = redisUtils.get(key);
            if (o2 == null){
                continue;
            }
            String count = String.valueOf(o2);
            if(RequestCountCode.REQUEST_NUM.equals(stage)){
                num = Integer.parseInt(count);
            }else if(RequestCountCode.REQUEST_TIMEOUT.equals(stage)){
                timeout = Integer.parseInt(count);
            }else if(RequestCountCode.REQUEST_EXCEPTION.equals(stage)){
                exception = Integer.parseInt(count);
            }
            if(!RequestCountCode.REQUEST_SUCCESS.equals(stage)){
                updateCount(requestCount, stage, count);
            }
        }
        success = num - timeout - exception;
        updateCount(requestCount, RequestCountCode.REQUEST_SUCCESS, success+"");
        if(success == 0 && num == 0 && timeout==0 && exception ==0){
            return;
        }
        List<RequestCount> requestCounts = requestCountService.selectByPeriodAndSystemName(requestCount);
        if (requestCounts == null || requestCounts.isEmpty()) {
            requestCountService.insertRequestCount(requestCount);

            if (requestCount.getRequestMethod().equals("pushAsync")){
                if (requestCount.getRequestException() != null && Integer.parseInt(requestCount.getRequestException())>=100){
                    stringBuilder.append(requestCount.getSystemName()).append("\t ").append(requestCount.getRequestNum())
                            .append("\t ").append(requestCount.getRequestSuccess()).append("\t ")
                            .append(requestCount.getRequestException()).append("\n");
                }
            }
        }else {
            requestCountService.updateRequestCount(requestCount);
        }
    }

    private void updateCount(RequestCount requestCount, String stage, String count){
        if(RequestCountCode.REQUEST_NUM.equals(stage)){
            requestCount.setRequestNum(count);
        }else if(RequestCountCode.REQUEST_TIMEOUT.equals(stage)){
            requestCount.setRequestTimeout(count);
        }else if(RequestCountCode.REQUEST_EXCEPTION.equals(stage)){
            requestCount.setRequestException(count);
        }else if(RequestCountCode.REQUEST_LIMIT.equals(stage)){
            requestCount.setRequestLimit(count);
        }else if(RequestCountCode.REQUEST_SUCCESS.equals(stage)){
            requestCount.setRequestSuccess(count);
        }
    }

}
