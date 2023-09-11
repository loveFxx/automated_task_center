package com.sailvan.dispatchcenter.core.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.sailvan.dispatchcenter.common.constant.*;
import com.sailvan.dispatchcenter.common.domain.TaskResult;
import com.sailvan.dispatchcenter.common.domain.TaskResultIndexRange;
import com.sailvan.dispatchcenter.common.domain.TaskSourceList;
import com.sailvan.dispatchcenter.common.pipe.TaskResultIndexRangeService;
import com.sailvan.dispatchcenter.common.pipe.TaskResultService;
import com.sailvan.dispatchcenter.common.pipe.TaskSourceListService;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.common.util.TaskUtil;
import com.sailvan.dispatchcenter.core.async.AsyncPushTask;
import com.sailvan.dispatchcenter.core.domain.TaskBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;

import static com.sailvan.dispatchcenter.common.constant.Constant.COOKIE;

@Component
public class SQSConsumer {

    private static Logger logger = LoggerFactory.getLogger(SQSConsumer.class);
    @Autowired
    TaskBufferService taskBufferService;

    @Autowired
    TaskSourceListService taskSourceListService;

    @Autowired
    TaskLogsService taskLogsService;

    @Autowired
    AsyncPushTask asyncPushTask;

    @Autowired
    ClientService clientService;

    @Autowired
    TaskUtil taskUtil;

    @Autowired
    TaskResultService taskResultService;

    @Autowired
    TaskResultIndexRangeService taskResultIndexRangeService;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    LambdaService lambdaService;

    @SqsListener(value = "${cloud.aws.queue-name}",deletionPolicy = SqsMessageDeletionPolicy.ALWAYS)
    public void processMessage(String message) throws Exception {
        logger.info("Message from SQS {}", message);
        JSONObject jsonObject = JSONObject.parseObject(message);
        JSONObject responsePayload = jsonObject.getJSONObject("responsePayload");
        if (responsePayload.containsKey("code")){
            handleResult(responsePayload);
        }else {
            JSONObject requestPayload = jsonObject.getJSONObject("requestPayload");
            JSONObject returnParams = requestPayload.getJSONObject("return_params");
            String taskBufferId = returnParams.getString("task_buffer_id");

            TaskBuffer taskBuffer = taskBufferService.findById(taskBufferId);
            if (taskBuffer!=null){
                String taskSourceId = taskBuffer.getTask_source_id();

                String[] s = taskSourceId.split("_");
                int isSingle;
                if (s[0].equals(CacheKey.CIRCLE)) {
                    isSingle = 0;
                } else {
                    isSingle = 1;
                }
                TaskSourceList taskSource = taskSourceListService.findTaskSourceById(Integer.parseInt(s[1]), isSingle);
                TaskResult taskResult = updateOrCreateTaskResult(taskBuffer,taskSource,responsePayload.getString("errorMessage"));
                taskSourceListService.updateLastResultTimeById(taskSource.getId(), DateUtils.getCurrentDate(), isSingle, TaskStateKey.TASK_STATE_FAIL);
                //将结果分发不同的业务系统
                if (taskResult != null) {
                    String[] split = taskSource.getSystemId().split(",");
                    for (String systemId : split) {
                        asyncPushTask.sendSystem(taskResult, systemId, CacheKey.BEGIN_DELAY);
                    }
                }

                Constant.LAMBDA_REQUEST_LIMIT.incrementAndGet();
                if (taskBuffer.getRetry_times() > 0) {
                    logger.info("任务执行异常，任务缓冲区ID:{}，任务库ID-{}，重新入缓冲区", taskBuffer.getId(), taskBuffer.getTask_source_id());
                    taskBufferService.updateRetryTimesAndIsInPoolById(taskBuffer.getRetry_times() - 1, 0, taskBuffer.getId());
                }
            }
        }
    }

    private void handleResult(JSONObject jsonObject) throws Exception {

        if (jsonObject.containsKey("cookie")){
            Object cookie = jsonObject.get("cookie");
            if (StringUtils.isEmpty(cookie)){
                redisUtils.put(COOKIE,String.valueOf(cookie),5*60L);
            }
        }
        LinkedHashMap returnParamsMap = JSON.parseObject(jsonObject.getString("return_params"), LinkedHashMap.class, Feature.OrderedField);

        String taskBufferId = String.valueOf(returnParamsMap.get("task_buffer_id"));

        TaskBuffer taskBuffer = taskBufferService.findById(taskBufferId);
        if (taskBuffer != null) {
            String taskSourceId = taskBuffer.getTask_source_id();
            String[] s = taskSourceId.split("_");
            int isSingle;
            if (s[0].equals(CacheKey.CIRCLE)) {
                isSingle = 0;
            } else {
                isSingle = 1;
            }
            TaskSourceList taskSource = taskSourceListService.findTaskSourceById(Integer.parseInt(s[1]), isSingle);

            if (taskSource != null) {
                LinkedHashMap centerParamsMap = new LinkedHashMap();

                TaskResult taskResult = clientService.updateOrCreateResult(jsonObject, returnParamsMap, centerParamsMap, taskSource, taskBuffer, "", Constant.LAMBDA_CONTAINER);

                String taskState = "";
                if (taskResult.getClientCode() == ResponseCode.SUCCESS_CODE) {
                    taskState = TaskStateKey.TASK_STATE_SUCCESS;
                } else {
                    taskState = TaskStateKey.TASK_STATE_FAIL;
                }
                taskSourceListService.updateLastResultTimeById(Integer.parseInt(s[1]), DateUtils.getCurrentDate(), isSingle, taskState);

                //记录流水
                taskLogsService.addTaskEvent(taskResult, taskBuffer, "",Constant.LAMBDA_CONTAINER);

                logger.info("客户端返回结果并删除redis等待任务缓存，任务缓冲区ID:{},任务库ID:{}", taskBuffer.getId(), taskBuffer.getTask_source_id());

                clientService.updateAccountSiteStatus(jsonObject, taskSource);

                //任务执行失败，任务重新回收入池，执行成功，删除任务池缓冲区记录
                if (taskResult.getClientCode() != ResponseCode.SUCCESS_CODE) {

                    Constant.LAMBDA_REQUEST_LIMIT.incrementAndGet();
                    //入池
                    if (taskResult.getNeedRetry() == 1) {
                        if (taskBuffer.getRetry_times() > 0) {
                            logger.info("任务执行失败，任务缓冲区ID:{}，任务库ID-{}，重新入缓冲区", taskBuffer.getId(), taskBuffer.getTask_source_id());
                            taskBufferService.offerQueue(taskBuffer.getPriority());
                            taskBufferService.updateRetryTimesAndIsInPoolById(taskBuffer.getRetry_times() - 1, 0, taskBuffer.getId());
                        } else {
                            logger.info("当前任务已无重试机会，任务缓冲区ID:{}，任务库ID:{}", taskBuffer.getId(), taskBuffer.getTask_source_id());
                            taskBufferService.updateRetryTimesById(0, taskBuffer.getId());
                        }
                    } else {
                        taskBufferService.updateRetryTimesById(0, taskBuffer.getId());
                        logger.info("当前任务不需要重试，任务缓冲区ID:{}，任务库ID:{}", taskBuffer.getId(), taskBuffer.getTask_source_id());
                    }
                } else {
                    taskBufferService.deleteById(taskBuffer.getId());
                    lambdaService.requestLambda();
                }
                //将结果分发不同的业务系统
                String[] split = taskSource.getSystemId().split(",");
                for (String systemId : split) {
                    asyncPushTask.sendSystem(taskResult, systemId, CacheKey.BEGIN_DELAY);
                }
            }
        }
    }

    private TaskResult updateOrCreateTaskResult(TaskBuffer taskBuffer, TaskSourceList taskSource, String error){

        //强制返回失败结果
        int smallestId = taskUtil.getTaskResultSearchSmallestId();
        TaskResult taskResultInfo = taskResultService.getTaskResultByTaskSourceIdAndResultHashKeyAndRefreshTime(smallestId, taskBuffer.getTask_source_id(),taskBuffer.getResult_hash_key(),taskBuffer.getRefresh_time());
        TaskResult taskResult = new TaskResult();

        taskResult.setCreatedTime(DateUtils.getCurrentDate());
        taskResult.setRunMode(Constant.LAMBDA_CONTAINER);
        int retryTimes = 1;
        if (taskResultInfo != null){

            retryTimes = taskResultInfo.getRetryTimes() + 1;
            taskResult.setRetryTimes(retryTimes);
            taskResult.setId(taskResultInfo.getId());
            taskResultService.updateTaskResult(taskResult);
        }else {
            LinkedHashMap clientParamsMap = JSON.parseObject(taskSource.getParams(),LinkedHashMap.class, Feature.OrderedField);
            if (clientParamsMap.containsKey("account") && clientParamsMap.containsKey("site")) {
                String account = String.valueOf(clientParamsMap.get("account"));
                String site = String.valueOf(clientParamsMap.get("site"));
                taskResult.setSite(site);
                taskResult.setAccount(account);
            }
            taskResult.setWorkType(taskSource.getWorkType());
            taskResult.setTaskType(taskBuffer.getType());
            taskResult.setTaskSourceId(taskBuffer.getTask_source_id());
            taskResult.setTaskBufferId(String.valueOf(taskBuffer.getId()));
            taskResult.setUniqueId(taskBuffer.getUnique_id());
            taskResult.setResultHashKey(taskBuffer.getResult_hash_key());
            taskResult.setRefreshTime(taskBuffer.getRefresh_time());
            taskResult.setCenterParams(taskBuffer.getCenter_params());
            taskResult.setReturnParams(taskSource.getReturnParams());
            taskResult.setClientCode(ResponseCode.LAMBDA_ERROR);
            taskResult.setClientError(error);
            retryTimes++;
            taskResult.setRetryTimes(retryTimes);
            int resultId = taskUtil.getNextTaskResultId();
            taskResult.setId(resultId);
            if (resultId % CacheKey.SINGLE_TABLE_CAPACITY == 0 || resultId % CacheKey.SINGLE_TABLE_CAPACITY == 1) {
                TaskResultIndexRange taskResultIndexRange = new TaskResultIndexRange();
                taskResultIndexRange.setIndex(resultId);
                taskResultIndexRange.setDate(DateUtils.getDate());
                taskResultIndexRangeService.insertTaskResultIndexRange(taskResultIndexRange);
            }
            taskResultService.insertTaskResult(taskResult);
            taskLogsService.addTaskEvent(taskResult, taskBuffer,"",Constant.LAMBDA_CONTAINER);
            return taskResult;
        }
        return null;
    }
}
