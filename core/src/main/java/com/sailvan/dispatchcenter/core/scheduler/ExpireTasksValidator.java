package com.sailvan.dispatchcenter.core.scheduler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.sailvan.dispatchcenter.common.constant.*;
import com.sailvan.dispatchcenter.common.domain.Task;
import com.sailvan.dispatchcenter.common.domain.TaskResult;
import com.sailvan.dispatchcenter.common.domain.TaskResultIndexRange;
import com.sailvan.dispatchcenter.common.domain.TaskSourceList;
import com.sailvan.dispatchcenter.common.pipe.*;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.common.util.TaskUtil;
import com.sailvan.dispatchcenter.core.async.AsyncPushTask;
import com.sailvan.dispatchcenter.core.config.TaskPoolConfig;
import com.sailvan.dispatchcenter.core.domain.TaskBuffer;
import com.sailvan.dispatchcenter.core.pool.TaskPool;
import com.sailvan.dispatchcenter.core.service.TaskBufferService;
import com.sailvan.dispatchcenter.core.service.TaskLogsService;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;

@Component
public class ExpireTasksValidator {

    private static Logger logger = LoggerFactory.getLogger(ExpireTasksValidator.class);

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    TaskPool taskPool;

    @Autowired
    TaskSourceListService taskSourceListService;

    @Autowired
    TaskResultService taskResultService;

    @Autowired
    AsyncPushTask asyncPushTask;

    @Autowired
    TaskLogsService taskLogsService;

    @Autowired
    TaskBufferService taskBufferService;

    @Autowired
    TaskUtil taskUtil;

    @Autowired
    TaskResultIndexRangeService taskResultIndexRangeService;

    @Autowired
    TaskPoolConfig taskPoolConfig;

    @Autowired
    TaskService taskService;

    /**
     * 校验24小时一直未取的任务进行清理
     * @throws ParseException
     */
    @SneakyThrows
    @Scheduled(cron = "15 */5 * * * ?")
    public void checkInPool() {
//        List<String> includeTaskNames = taskService.getTasksByExecutePlatform("7");
//        List<String> excludeTaskNames = taskService.getTasksExcludeExecutePlatform("7");
//        List<TaskBuffer> excludeTaskBuffers = taskBufferService.listExpiredTasksByType(DateUtils.getHourBeforeDate(24), 1,excludeTaskNames);
//        checkInPool(excludeTaskBuffers,24);
//        List<TaskBuffer> includeTaskBuffers = taskBufferService.listExpiredTasksByType(DateUtils.getHourBeforeDate(48), 1,includeTaskNames);
//        checkInPool(includeTaskBuffers,48);

        List<TaskBuffer> includeTaskBuffers = taskBufferService.listExpiredTasks(DateUtils.getHourBeforeDate(24), 1);
        checkInPool(includeTaskBuffers,24);

    }

    @SneakyThrows
    private void checkInPool(List<TaskBuffer> taskBuffers,int hour){
        for (TaskBuffer taskBuffer : taskBuffers) {
            synchronized (taskBuffer.getId().intern()) {
                String taskSourceId = taskBuffer.getTask_source_id();

                String[] s = taskSourceId.split("_");
                int isSingle;
                if (s[0].equals(CacheKey.CIRCLE)) {
                    isSingle = 0;
                } else {
                    isSingle = 1;
                }
                TaskSourceList taskSource = taskSourceListService.findTaskSourceById(Integer.parseInt(s[1]), isSingle);
                logger.info("checkInPool-当前任务在任务池--任务库ID[{}]，任务缓冲池ID:{}--存在时长超过{}小时，需进行清理", taskSourceId, taskBuffer.getId(),hour);

                redisUtils.remove(Constant.TASK_PREFIX + taskBuffer.getId());  //redis移除任务
                taskPool.deleteData(taskBuffer.getWork_type(), taskBuffer.getType(), taskBuffer.getPriority(), taskBuffer.getId()); //任务池移除数据
                taskBufferService.deleteById(taskBuffer.getId());
                if(taskSource != null){
                    String error = "Task in pool,client server timeout";
                    TaskResult taskResult = updateOrCreateTaskResult(taskBuffer, taskSource, error, "");
                    taskSourceListService.updateLastResultTimeById(taskSource.getId(), DateUtils.getCurrentDate(), isSingle, TaskStateKey.TASK_STATE_FAIL);
                    //将结果分发不同的业务系统
                    if (taskResult != null) {
                        String[] split = taskSource.getSystemId().split(",");
                        for (String systemId : split) {
                            asyncPushTask.sendSystem(taskResult, systemId, CacheKey.BEGIN_DELAY);
                        }
                    }
                }
            }
        }
    }

    /**
     * 检测任务结果持续等待超两小时强制失败
     */
    @Scheduled(cron = "30 */2 * * * ?")
    public void checkTaskWaitResult() throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        Set range = redisUtils.rangeWithScores(Constant.TASK_WAIT_RESULT, 0, -1);
        if (range != null && !range.isEmpty()) {
            Iterator obj = range.iterator();
            while (obj.hasNext()) {
                DefaultTypedTuple next = (DefaultTypedTuple)obj.next();
                Double score = next.getScore();
                Object value = next.getValue();
                if (score != null){
                    //任务结果等待时长超过半小时，舍弃，直接设置失败结果，并重新回收入池
                    if (currentTimeMillis - score > 1800*1000){
                        redisUtils.remove(Constant.TASK_WAIT_RESULT,value);  //redis移除任务

                        String[] idAndIp = String.valueOf(value).split("#");
                        String id = "";
                        String ip = "";
                        if (idAndIp.length == 1){
                            id = idAndIp[0];
                        }else {
                            id = idAndIp[0];
                            ip = idAndIp[1];
                        }

                        synchronized (id.intern()){
                            TaskBuffer taskBuffer = taskBufferService.findById(id);
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

                                rePushTaskPool(taskBuffer);

                                String error = "client didn't response a result";
                                TaskResult taskResult = updateOrCreateTaskResult(taskBuffer, taskSource, error, ip);
                                taskSourceListService.updateLastResultTimeById(taskSource.getId(), DateUtils.getCurrentDate(), isSingle,TaskStateKey.TASK_STATE_FAIL);

                                if (taskResult != null){
                                    //将结果分发不同的业务系统
                                    String[] split = taskSource.getSystemId().split(",");
                                    for (String systemId : split) {
                                        asyncPushTask.sendSystem(taskResult, systemId,CacheKey.BEGIN_DELAY);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private TaskResult updateOrCreateTaskResult(TaskBuffer taskBuffer, TaskSourceList taskSource, String error, String ip){

        //强制返回失败结果
        int smallestId = taskUtil.getTaskResultSearchSmallestId();
        TaskResult taskResultInfo = taskResultService.getTaskResultByTaskSourceIdAndResultHashKeyAndRefreshTime(smallestId, taskBuffer.getTask_source_id(),taskBuffer.getResult_hash_key(),taskBuffer.getRefresh_time());
        TaskResult taskResult = new TaskResult();

        taskResult.setCreatedTime(DateUtils.getCurrentDate());
        taskResult.setRunMode(Constant.MACHINE);
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
            taskResult.setClientCode(ResponseCode.CLIENT_SERVER_TIMEOUT);
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
            taskLogsService.addTaskEvent(taskResult, taskBuffer,ip,Constant.MACHINE);
            return taskResult;
        }
        return null;
    }

    //3天前mongo数据进行分批清理
    @Scheduled(cron = "0 */5 * * * ?")
    public void delete(){
        //3天前的数据分批处理
        List<TaskBuffer> taskBuffers = taskBufferService.listTaskBufferBeforeDays(3,1000);

        ArrayList ids = new ArrayList();
        int i = 0;
        for (TaskBuffer taskBuffer:taskBuffers) {
            logger.info("当前任务记录已超3天，需进行清理，任务缓冲区ID:{}，任务库ID:{}",taskBuffer.getId(),taskBuffer.getTask_source_id());
            redisUtils.remove(Constant.TASK_PREFIX + taskBuffer.getId());
            taskPool.deleteData(taskBuffer.getWork_type(), taskBuffer.getType(), taskBuffer.getPriority(), taskBuffer.getId());
            ids.add(taskBuffer.getId());
            if ((i+1) % 100 == 0){
                long count = taskBufferService.batchDelete(ids.toArray());
                ids.clear();
            }
            i++;
        }
        if (!ids.isEmpty()){
            long count = taskBufferService.batchDelete(ids.toArray());
            ids.clear();
        }
    }

    private void rePushTaskPool(TaskBuffer taskBuffer) {
        if (taskBuffer.getRetry_times() > 0) {
            logger.info("任务半小时内无返回结果，强制执行失败，任务缓冲区ID:{}，任务库ID-{}，重新入缓冲区", taskBuffer.getId(), taskBuffer.getTask_source_id());
            taskBufferService.offerQueue(taskBuffer.getPriority());
            taskBufferService.updateRetryTimesAndIsInPoolById(taskBuffer.getRetry_times() - 1, 0, taskBuffer.getId());
        } else {
            logger.info("任务半小时内无返回结果，当前任务已无重试机会，任务缓冲区ID:{}，任务库ID:{}", taskBuffer.getId(), taskBuffer.getTask_source_id());
            taskBufferService.updateRetryTimesById(0, taskBuffer.getId());
        }
    }
}
