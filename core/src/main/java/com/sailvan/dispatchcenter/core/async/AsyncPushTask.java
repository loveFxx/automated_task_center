package com.sailvan.dispatchcenter.core.async;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.sailvan.dispatchcenter.common.constant.CacheKey;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.constant.RequestCountCode;
import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.pipe.BusinessSystemService;
import com.sailvan.dispatchcenter.common.pipe.TaskResultService;
import com.sailvan.dispatchcenter.common.pipe.TaskService;
import com.sailvan.dispatchcenter.common.pipe.TaskSourceListService;
import com.sailvan.dispatchcenter.common.util.*;
import com.sailvan.dispatchcenter.core.domain.TaskBuffer;
import com.sailvan.dispatchcenter.core.pool.TaskPool;
import com.sailvan.dispatchcenter.core.service.TaskBufferService;
import com.sailvan.dispatchcenter.core.util.RequestCountUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

/**
 *  异步推送任务到业务端
 * @author mh
 * @date
 */
@Component
public class AsyncPushTask {

    private static Logger logger = LoggerFactory.getLogger(AsyncPushTask.class);

    @Autowired
    HttpClientUtils httpClient;

    @Autowired
    BusinessSystemService businessSystemService;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    BusSystemUtils busSystemUtils;

    @Autowired
    RequestCountUtils requestCountUtils;

    @Autowired
    TaskBufferService taskBufferService;

    @Autowired
    TaskPool taskPool;

    @Autowired
    TaskService taskService;

    @Autowired
    TaskSourceListService taskSourceListService;

    @Autowired
    TaskUtil taskUtil;

    @Autowired
    TaskResultService taskResultService;

    /**
     * 根据beanName指定特定线程池 异步推送
     * @param taskResult
     * @param system
     */
    public Future<Boolean> pushAsync(TaskResult taskResult, BusinessSystem system) throws Exception {

        long start = System.currentTimeMillis();
        HashMap<String, Object> resultInfo = buildResultMessage(taskResult);
        HttpHeaders headers = new HttpHeaders();
        String message = DigestUtils.createMessage(resultInfo);
        headers.add("NotificationSignature", DigestUtils.createDispatchCenterSign(system.getAppKey(),system.getAppSecret(),message));
        boolean flag = false;
        requestCountUtils.recordRequest(RequestCountCode.REQUEST_NUM , system.getSystemName(),"pushAsync");
        try {
            String data;

            if (system.getNetworkType() == 1) {
                headers.add("Content-Type", "application/json; charset=UTF-8");
                HttpEntity<HashMap<String, Object>> httpEntity = new HttpEntity<>(resultInfo, headers);
                data = httpClient.post(system.getCallbackAddress(), httpEntity);
            } else {
//                data = busSystemUtils.post(system.getCallbackAddress(), resultInfo, system.getSystemVersion(), "json", Constant.NETWORK_MAP.get(system.getNetworkType()));
                data = busSystemUtils.request(system.getCallbackAddress(), headers,resultInfo, system.getSystemVersion(),
                        "json", Constant.NETWORK_MAP.get(system.getNetworkType()),"post");
            }

            JSONObject parseObject = JSONObject.parseObject(data);
            flag = true;
            long end = System.currentTimeMillis();
            if (parseObject.containsKey("code")){
                if (parseObject.getIntValue("code") != 1){
                    logger.error("推送至系统[{}]的数据--{} times:{}ms --error:{}",system.getSystemName(), JSONUtils.toJSONString(resultInfo), (end-start),parseObject.getIntValue("code"));
                }else {
                    logger.info("推送至系统[{}]的数据--{} times:{}ms --success",system.getSystemName(), JSONUtils.toJSONString(resultInfo), (end-start));
                }
            }else {
                logger.error("推送至系统[{}]的数据--{} times:{}ms --error:回调格式不正确:{}",system.getSystemName(), JSONUtils.toJSONString(resultInfo), (end-start),data);
            }

        } catch (Exception e) {
            long end = System.currentTimeMillis();
            requestCountUtils.recordRequest(RequestCountCode.REQUEST_EXCEPTION , system.getSystemName(),"pushAsync");
            logger.error("推送至系统[{}]的数据--{} times:{}ms 请求异常--{}",system.getSystemName(), JSONUtils.toJSONString(resultInfo), (end-start), e.getMessage());
        }
        return new AsyncResult<Boolean>(flag);
    }


    /**
     * 根据回调结果构建需要发送的结果体
     * @param taskResult 任务结果
     * @return 结果消息
     */
    public HashMap<String,Object> buildResultMessage(TaskResult taskResult){
        HashMap<String,Object> resultInfo = new HashMap<>();
        resultInfo.put("resultId",String.valueOf(taskResult.getId()));
        resultInfo.put("clientCode",taskResult.getClientCode());
        resultInfo.put("taskType",taskResult.getTaskType());
        resultInfo.put("resultType",taskResult.getResultType());
        resultInfo.put("needRetry",taskResult.getNeedRetry());
        resultInfo.put("errorLevel",taskResult.getErrorLevel());
        resultInfo.put("taskSourceId",taskResult.getTaskSourceId());
        resultInfo.put("clientResult", JSONObject.parseObject(taskResult.getClientResult()));
        resultInfo.put("centerParams",JSONObject.parseObject(taskResult.getCenterParams()));
        resultInfo.put("returnParams",JSONObject.parseObject(taskResult.getReturnParams()));
        resultInfo.put("clientMsg",taskResult.getClientMsg());
        resultInfo.put("clientError",taskResult.getClientError());
        resultInfo.put("retryTimes",taskResult.getRetryTimes());
        resultInfo.put("createdTime", taskResult.getCreatedTime());

        return resultInfo;
    }

    /**
     * 将结果分发至不同的业务系统
     * @param taskResult 结果
     * @param systemId 系统id
     * @param cacheKey 缓存key
     */
    @Async("async_bean_executor")
    public void sendSystem(TaskResult taskResult, String systemId, String cacheKey) throws Exception {
        //将结果传给业务系统
        BusinessSystem system = businessSystemService.findSystemById(Integer.parseInt(systemId));

        Future<Boolean> booleanFuture = pushAsync(taskResult, system);
        if (!booleanFuture.get()) {
            delay(taskResult.getId(), taskResult.getTaskType(), systemId,cacheKey);
        }else {
            decrementAndGet(cacheKey,taskResult.getTaskType());
            decrementAndGet(cacheKey,systemId);
        }
    }

    public void delay(int resultId, String taskType, String systemId, String cacheKey){
        switch (cacheKey){
            case CacheKey.BEGIN_DELAY:
                logger.error("delay_result--结果[{}]发送失败，延迟1分钟等待发送", resultId);
                delay1m(resultId, systemId, taskType);
                break;
            case CacheKey.RESULT_1M_DELAY:
                logger.error("delay_result--结果[{}]发送失败，延迟10分钟等待发送",resultId);
                delay10m(resultId,systemId, taskType);
                decrementAndGet(cacheKey,taskType);
                decrementAndGet(cacheKey,systemId);
                break;
            case CacheKey.RESULT_10M_DELAY:
                logger.error("delay_result--结果[{}]发送失败，延迟40分钟等待发送",resultId);
                delay40m(resultId,systemId, taskType);
                decrementAndGet(cacheKey,taskType);
                decrementAndGet(cacheKey,systemId);
                break;
            case CacheKey.RESULT_40M_DELAY:
                logger.error("delay_result--结果[{}]发送失败，延迟2小时等待发送",resultId);
                delay2h(resultId,systemId, taskType);
                decrementAndGet(cacheKey,taskType);
                decrementAndGet(cacheKey,systemId);
                break;
            case CacheKey.RESULT_2H_DELAY:
                logger.error("delay_result--结果[{}]发送失败，延迟22小时等待发送",resultId);
                delay22h(resultId,systemId, taskType);
                decrementAndGet(cacheKey,taskType);
                decrementAndGet(cacheKey,systemId);
                break;
            case CacheKey.RESULT_22H_DELAY:
                logger.error("delay_result--结果[{}]发送失败，超过22小时未能发送结果，请检查业务系统服务",resultId);
                decrementAndGet(cacheKey,taskType);
                decrementAndGet(cacheKey,systemId);
                break;
            default:break;
        }
    }

    public void delay1m(int resultId, String systemId, String taskType){
        redisUtils.add(CacheKey.RESULT_1M_DELAY,resultId + "_" + systemId,System.currentTimeMillis());
        incrementAndGet(CacheKey.RESULT_1M_DELAY,taskType);
        incrementAndGet(CacheKey.RESULT_1M_DELAY,systemId);
    }

    public void delay10m(int resultId, String systemId, String taskType){
        redisUtils.add(CacheKey.RESULT_10M_DELAY,resultId + "_" + systemId,System.currentTimeMillis());
        incrementAndGet(CacheKey.RESULT_10M_DELAY,taskType);
        incrementAndGet(CacheKey.RESULT_10M_DELAY,systemId);
    }

    public void delay40m(int resultId, String systemId, String taskType){
        redisUtils.add(CacheKey.RESULT_40M_DELAY,resultId + "_" + systemId,System.currentTimeMillis());
        incrementAndGet(CacheKey.RESULT_40M_DELAY,taskType);
        incrementAndGet(CacheKey.RESULT_40M_DELAY,systemId);
    }

    public void delay2h(int resultId, String systemId, String taskType){
        redisUtils.add(CacheKey.RESULT_2H_DELAY,resultId + "_" + systemId,System.currentTimeMillis());
        incrementAndGet(CacheKey.RESULT_2H_DELAY,taskType);
        incrementAndGet(CacheKey.RESULT_2H_DELAY,systemId);
    }

    public void delay22h(int resultId, String systemId, String taskType){
        redisUtils.add(CacheKey.RESULT_22H_DELAY,resultId + "_" + systemId,System.currentTimeMillis());
        incrementAndGet(CacheKey.RESULT_22H_DELAY,taskType);
        incrementAndGet(CacheKey.RESULT_22H_DELAY,systemId);
    }

    public void incrementAndGet(String cacheKey, String suffix){
        String key = cacheKey + ":" + suffix;
        synchronized (key.intern()){
            if (redisUtils.exists(key)){
                int value = Integer.parseInt(String.valueOf(redisUtils.get(key)));
                redisUtils.put(key,String.valueOf(value+1),3600*24L);
            }else {
                redisUtils.put(key,"1",3600*24L);
            }
        }
    }

    public void decrementAndGet(String cacheKey, String suffix){
        String key = cacheKey + ":" + suffix;
        synchronized (key.intern()){
            if (redisUtils.exists(key)){
                int value = Integer.parseInt(String.valueOf(redisUtils.get(key)));
                redisUtils.put(key,String.valueOf(value-1),3600*24L);
            }
        }
    }


    @Async("async_bean_executor")
    public void deleteTasksInPoolAsync(String type) {
        if (StringUtil.isNotEmpty(type)){
            deleteTasksInPool(1,type);
            deleteTasksInPool(0,type);
        }
    }


    private void deleteTasksInPool(int isInPool,String type){
        int count = taskBufferService.countTasksByType(isInPool,type);
        if (count != 0){
            int limit = 1000;
            int page = count /limit;

            for (int i =0; i <= page;i++){
                int skips = i*limit;
                List<TaskBuffer> taskBuffers = taskBufferService.chunkTasksByType(isInPool,type,skips,limit);

                ArrayList ids = new ArrayList();
                for (TaskBuffer taskBuffer : taskBuffers) {
                    if (isInPool == 1){
                        taskPool.deleteData(taskBuffer.getWork_type(), taskBuffer.getType(), taskBuffer.getPriority(), taskBuffer.getId());
                    }

                    redisUtils.remove(Constant.TASK_PREFIX + taskBuffer.getId());
                    ids.add(taskBuffer.getId());
                }
                taskBufferService.batchDelete(ids.toArray());
            }
        }
    }


    public List getDelayQueue() {
        List<DelayQueueInfo> delayQueueInfoList = new ArrayList<>();

        List<BusinessSystem> businessSystemAll = businessSystemService.getBusinessSystemAll();
        List<String> taskNames = taskService.getAllTaskName();
        for (BusinessSystem bs : businessSystemAll){
            DelayQueueInfo delayQueueInfo = new DelayQueueInfo();
            delayQueueInfo.setSystemOrTask(bs.getSystemName());
            Object one = redisUtils.get(CacheKey.RESULT_1M_DELAY + ":" + bs.getId());
            if (null ==one){
                delayQueueInfo.setOneMinuteDelay(0);
            }else {
                delayQueueInfo.setOneMinuteDelay(Integer.parseInt(one.toString()));
            }
            Object ten = redisUtils.get(CacheKey.RESULT_10M_DELAY +":"+ bs.getId());
            if (null ==ten){
                delayQueueInfo.setTenMinuteDelay(0);
            }else {
                delayQueueInfo.setTenMinuteDelay(Integer.parseInt(ten.toString()));
            }
            Object twoHour = redisUtils.get(CacheKey.RESULT_2H_DELAY+":" + bs.getId());
            if (null ==twoHour){
                delayQueueInfo.setTwoHourDelay(0);
            }else {
                delayQueueInfo.setTwoHourDelay(Integer.parseInt(twoHour.toString()));
            }
            Object forty =  redisUtils.get(CacheKey.RESULT_40M_DELAY+":" + bs.getId());
            if (null ==forty){
                delayQueueInfo.setFortyMinuteDelay(0);
            }else {
                delayQueueInfo.setFortyMinuteDelay(Integer.parseInt(forty.toString()));
            }
            Object twentyTwoHour = redisUtils.get(CacheKey.RESULT_22H_DELAY+":" + bs.getId());
            if (null ==twentyTwoHour){
                delayQueueInfo.setTwentyTwoHourDelay(0);
            }else {
                delayQueueInfo.setTwentyTwoHourDelay(Integer.parseInt(twentyTwoHour.toString()));
            }
            delayQueueInfoList.add(delayQueueInfo);
        }
        for (String taskName : taskNames){
            DelayQueueInfo delayQueueInfo = new DelayQueueInfo();
            delayQueueInfo.setSystemOrTask(taskName);
            Object one =  redisUtils.get(CacheKey.RESULT_1M_DELAY+":" + taskName);
            if (null ==one){
                delayQueueInfo.setOneMinuteDelay(0);
            }else {
                delayQueueInfo.setOneMinuteDelay(Integer.parseInt(one.toString()));
            }
            Object ten = redisUtils.get(CacheKey.RESULT_10M_DELAY+":" +taskName);
            if (null ==ten){
                delayQueueInfo.setTenMinuteDelay(0);
            }else {
                delayQueueInfo.setTenMinuteDelay(Integer.parseInt(ten.toString()));
            }
            Object twoHour =  redisUtils.get(CacheKey.RESULT_2H_DELAY+":" + taskName);
            if (null ==twoHour){
                delayQueueInfo.setTwoHourDelay(0);
            }else {
                delayQueueInfo.setTwoHourDelay(Integer.parseInt(twoHour.toString()));
            }
            Object forty = redisUtils.get(CacheKey.RESULT_40M_DELAY+":" + taskName);
            if (null ==forty){
                delayQueueInfo.setFortyMinuteDelay(0);
            }else {
                delayQueueInfo.setFortyMinuteDelay(Integer.parseInt(forty.toString()));
            }

            Object twentyTwoHour =  redisUtils.get(CacheKey.RESULT_22H_DELAY+":" + taskName);
            if (null ==twentyTwoHour){
                delayQueueInfo.setTwentyTwoHourDelay(0);
            }else {
                delayQueueInfo.setTwentyTwoHourDelay(Integer.parseInt(twentyTwoHour.toString()));
            }
            delayQueueInfoList.add(delayQueueInfo);
        }
        return delayQueueInfoList;


    }

    @Async("async_bean_executor")
    public void rePushToSystem(int systemId,String type,int minResultId,int maxResultId,String minTime,String maxTime) throws Exception {
        BusinessSystem system = businessSystemService.findSystemById(systemId);

        List<TaskResult> taskResults = taskResultService.listTaskResultByTime(type, minResultId, maxResultId, minTime, maxTime);
        for (TaskResult taskResult: taskResults) {
            Future<Boolean> booleanFuture = pushAsync(taskResult, system);
        }
        logger.info("rePushToSystem--任务类型{}推送完成",type);
    }

    @Async("async_bean_executor")
    public void fixTaskResult(String type,int minResultId,int maxResultId){
        Task task = taskService.findTaskByName(type);
        List<Integer> ids = taskSourceListService.listIds(task.getId());
        List<TaskSourceList> taskSourceLists = taskSourceListService.groupByTaskSources(ids);

        for (TaskSourceList taskSourceList:taskSourceLists) {
            List<TaskSourceList> lists = taskSourceListService.listTaskSourceByParams(task.getId(), taskSourceList.getParams());
            if (lists.size()>1){
                TaskSourceList oldList = lists.get(0);
                TaskSourceList newList = lists.get(1);
                List<Integer> resultIds = taskResultService.listResultIds(type,minResultId,maxResultId,"circle_"+oldList.getId());
                taskResultService.fixTaskResult(resultIds,newList.getUniqueId(), "circle_"+newList.getId());
            }
        }
        logger.info("fix--任务类型{}修补完成",type);
    }
}

