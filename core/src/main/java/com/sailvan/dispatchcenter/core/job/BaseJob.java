package com.sailvan.dispatchcenter.core.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.sailvan.dispatchcenter.common.cache.InitAccountCache;
import com.sailvan.dispatchcenter.common.constant.*;
import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.pipe.*;
import com.sailvan.dispatchcenter.common.util.CommonUtils;
import com.sailvan.dispatchcenter.common.util.CronDateUtils;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.TaskUtil;
import com.sailvan.dispatchcenter.core.async.AsyncPushTask;
import com.sailvan.dispatchcenter.core.config.TaskPoolConfig;
import com.sailvan.dispatchcenter.core.domain.TaskBuffer;
import com.sailvan.dispatchcenter.core.pool.TaskPool;
import com.sailvan.dispatchcenter.core.service.TaskBufferService;
import com.sailvan.dispatchcenter.core.service.JobService;
import com.sailvan.dispatchcenter.core.service.TaskLogsService;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class BaseJob extends QuartzJobBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    TaskSourceListService taskSourceListService;

    @Resource
    TaskService taskService;

    @Resource
    TaskResultService taskResultService;

    @Resource
    TaskPool taskPool;

    @Resource
    JobService jobService;

    @Resource
    TaskLogsService taskLogsService;

    @Resource
    TaskFunnelService taskFunnelService;

    @Resource
    TaskBufferService taskBufferService;

    @Autowired
    TaskUtil taskUtil;

    @Autowired
    InitAccountCache initAccountCache;

    @Autowired
    TaskResultIndexRangeService taskResultIndexRangeService;

    @Autowired
    AsyncPushTask asyncPushTask;

    @Autowired
    TaskPoolConfig taskPoolConfig;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext)
    {
        try {
            String jobName = jobExecutionContext.getJobDetail().getKey().getName();
            int isSingle = (int)jobExecutionContext.getJobDetail().getJobDataMap().get("isSingle");

            int id;
            if (isSingle == 0){
                id = taskUtil.getCircleTaskSearchSmallestId();
            }else {
                id = taskUtil.getSingleTaskSearchSmallestId();
            }

            List<TaskSourceList> taskSourceLists = taskSourceListService.listTaskSourcesByJobName(jobName,isSingle,id);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fireTime = formatter.format(jobExecutionContext.getFireTime());

            if (taskSourceLists != null && !taskSourceLists.isEmpty()){
                Task task = taskService.findTaskById(taskSourceLists.get(0).getTaskId());
                List<Integer> lists = new ArrayList<>();
                if (task.getStatus() != 0){
                    for (TaskSourceList taskSource : taskSourceLists) {
                        lists.add(taskSource.getId());
                        LinkedHashMap clientParamsMap = JSON.parseObject(taskSource.getParams(),LinkedHashMap.class, Feature.OrderedField);
                        if (clientParamsMap.containsKey("account") && clientParamsMap.containsKey("site"))
                        {
                            Map<Integer,String> codeMean = new HashMap<>();
                            String account = String.valueOf(clientParamsMap.get("account"));
                            String site = String.valueOf(clientParamsMap.get("site"));
                            if (initAccountCache.isInvalidByAccountSite(task.getTaskName(), account, site, codeMean)){
                                logger.info("任务生成异常，账号:{},站点:{},任务库ID:{}，是否单次:{}",account,site,taskSource.getId(),isSingle);
                                TaskResult taskResult = errorAccountCallBack(taskSource,task,clientParamsMap,account,site,codeMean);

                                //将结果分发不同的业务系统
                                if (taskResult != null){
                                    String[] split = taskSource.getSystemId().split(",");
                                    for (String systemId : split) {
                                        asyncPushTask.sendSystem(taskResult, systemId,CacheKey.BEGIN_DELAY);
                                    }
                                }
                                continue;
                            }
                        }
                        logger.info("任务{}生成任务，任务库ID:{}，是否单次:{}",jobName,taskSource.getId(),isSingle);

                        produceTask(taskSource,task,clientParamsMap);
                    }
                }

                TaskFunnel taskFunnel = taskFunnelService.findByTaskName(jobName);
                //周期性任务需更新cron
                if (taskFunnel != null){
                    String expectedTime;
                    if (taskFunnel.getIsMain() == 1){
                        String nextFireTime = formatter.format(jobExecutionContext.getNextFireTime());
                        BigInteger time =  BigInteger.valueOf(DateUtils.convertDate(nextFireTime).getTime());
                        expectedTime = DateUtils.convertTimestampToDate(time);
                        taskFunnelService.updateNextFireTimeById(time,taskFunnel.getId());
                    }else {
                        String[] split = jobName.split(":");
                        TaskFunnel mainTaskFunnel = taskFunnelService.findMainFunnel(task.getId());
                        BigInteger time = mainTaskFunnel.getNextFireTime().add(BigInteger.valueOf((Long.parseLong(split[2])-1) * task.getProduceInterval() * 1000));
                        expectedTime = DateUtils.convertTimestampToDate(time);
                        taskFunnelService.updateNextFireTimeById(time,taskFunnel.getId());
                        String cron = CronDateUtils.getCron(expectedTime);

                        jobService.update(jobName,cron);
                    }
                    if (!lists.isEmpty()){
                        taskSourceListService.bulkUpdateTimeByIds(lists,expectedTime,fireTime,0);
                    }

                }else {
                    //单次性任务需删除
                    jobService.delete(jobName);
                    //TODO 并发时会出现数量超过设置的个数
                    if (!lists.isEmpty()){
                        int num =(int) Math.ceil(lists.size() / 500.0);
                        List<List<Integer>> listsArray = CommonUtils.averageAssign(lists, num);
                        for (List<Integer> integerList:listsArray) {
                            taskSourceListService.bulkUpdateTimeByIds(integerList,null,fireTime,1);
                        }
                    }
                }
            }
        }catch (Exception e){
            logger.error("BaseJob执行异常:{}",e.getMessage());
        }

    }


    /**
     * 生成任务逻辑
     * @param taskSource 任务库
     * @param task 任务类型
     */
    @Transactional
    public void produceTask(TaskSourceList taskSource, Task task, LinkedHashMap clientParamsMap) {
        try {
            LinkedHashMap centerParamsMap = generateCenterParams(taskSource,task,clientParamsMap);

            int resultHashKey = taskResultService.parseResultHashKey(taskSource.getUniqueId(), centerParamsMap);
            String refreshTime = taskSourceListService.generateRefreshTimeByIntervalTime(task.getIntervalType(),task.getIntervalTimes(),taskSource.getExpectedTime());
            String taskSourceId = "";
            if (taskSource.getIsSingle() == 0) {
                taskSourceId = CacheKey.CIRCLE + "_" + taskSource.getId();
            }
            if (taskSource.getIsSingle() == 1) {
                taskSourceId = CacheKey.SINGLE + "_" + taskSource.getId();
            }
            //是否为强制生成任务
            if (taskSource.getIsEnforced() == 1){
                insertBufferEvent(taskSource,task,resultHashKey,centerParamsMap,refreshTime,clientParamsMap);
            }else{
                //判断任务结果是否有存在的
                int smallestId = taskUtil.getTaskResultSearchSmallestId();
                TaskResult repeatResult = taskResultService.getTaskResultByTaskSourceIdAndResultHashKeyAndRefreshTime(smallestId,taskSourceId,resultHashKey, refreshTime);
                TaskBuffer taskBuffer = taskBufferService.findByTaskSourceIdAndRefreshTime(taskSourceId, refreshTime);
                if (repeatResult == null) {
                    //判断任务池缓冲区是否有重复的任务

                    //不存在重复任务
                    if (taskBuffer == null) {
                        insertBufferEvent(taskSource,task,resultHashKey,centerParamsMap,refreshTime,clientParamsMap);
                    } else {
                        increaseRetryTimes(taskBuffer);
                        logger.info("当前任务重复，叠加重试次数，任务缓冲区ID:{}，任务库ID:{}，是否单次:{}", taskBuffer.getId(), taskSource.getId(), taskSource.getIsSingle());
                    }
                } else {
                    if (taskBuffer != null){
                        increaseRetryTimes(taskBuffer);
                    }
                    logger.info("当前任务结果已存在，叠加重试次数，任务库ID:{}，是否单次:{}", taskSource.getId(),taskSource.getIsSingle());
                }
            }
        }catch (Exception exception){
            logger.error(exception.getMessage());
        }

    }

    private TaskBuffer insertTaskBuffer(TaskSourceList taskSource, Task task,int resultHashKey,LinkedHashMap centerParamsMap, String refreshTime,LinkedHashMap clientParamsMap) throws ParseException {

        //插入缓冲区
        TaskBuffer taskBuffer = new TaskBuffer();

        if (taskSource.getIsSingle() == 0){
            taskBuffer.setTask_source_id(CacheKey.CIRCLE + "_" + taskSource.getId());
        }
        if (taskSource.getIsSingle() == 1){
            taskBuffer.setTask_source_id(CacheKey.SINGLE + "_" + taskSource.getId());
        }

        taskBuffer.setRetry_times(task.getLimitRetryTimes());
        taskBuffer.setUnique_id(taskSource.getUniqueId());
        taskBuffer.setResult_hash_key(resultHashKey);
        taskBuffer.setType(task.getTaskName());
        taskBuffer.setWork_type(taskSource.getWorkType());
        taskBuffer.setClient_params(JSONObject.toJSONString(clientParamsMap));
        taskBuffer.setCenter_params(JSONObject.toJSONString(centerParamsMap));
        taskBuffer.setPriority(taskSource.getPriority());
        taskBuffer.setIs_enforced(taskSource.getIsEnforced());
        taskBuffer.setRefresh_time(refreshTime);
        taskBuffer.setIs_in_pool(0); //在任务缓冲区
        taskBuffer.setRun_mode(task.getRunMode());
        taskBuffer.setPool_type(0);
        taskBuffer.setIn_pool_times(0); //默认次数0
        taskBuffer.setIn_buffer_time(DateUtils.getCurrentDate()); //入缓冲区时间
        taskBuffer.setIn_pool_time(null);
        taskBuffer.setCreated_at(DateUtils.getCurrentDate());
        taskBuffer.setUpdated_at(DateUtils.getCurrentDate());
        taskBufferService.insertTaskBuffer(taskBuffer);

        return taskBuffer;
    }


    private void insertBufferEvent(TaskSourceList taskSource, Task task, int resultHashKey,LinkedHashMap centerParamsMap,String refreshTime,LinkedHashMap clientParamsMap) throws ParseException {
        //生成任务并入任务池缓冲区
        TaskBuffer taskBuffer = insertTaskBuffer(taskSource,task,resultHashKey,centerParamsMap,refreshTime,clientParamsMap);
        boolean result = taskBufferService.offerQueue(taskBuffer.getPriority());
        TaskMetadata taskMetadata = taskPool.buildTaskMetadata(taskBuffer);
        taskLogsService.addTaskLogs(taskBuffer,Event.CREATE_TASK,"",Constant.RUN_INIT);
        logger.info("任务成功生成并入任务池缓冲区，任务缓冲区ID:{}，任务库ID:{}，是否单次:{}，任务数据：{}",taskBuffer.getId(),taskSource.getId(),taskSource.getIsSingle(),JSONObject.toJSONString(taskMetadata));
    }


    private LinkedHashMap generateCenterParams(TaskSourceList taskSource,Task task, LinkedHashMap clientParamsMap) throws ParseException {
        LinkedHashMap centerParamsMap = new LinkedHashMap<>();

        if (taskSource.getType() == Constant.CIRCLE_SINGLE_TASK){
            if (clientParamsMap.containsKey("start_date") && clientParamsMap.containsKey("end_date")){
                centerParamsMap = taskService.generateCenterParams(taskSource,task);
                clientParamsMap.remove("start_date");
                clientParamsMap.remove("end_date");
            }
        }else {
            centerParamsMap = taskService.generateCenterParams(taskSource,task);
        }
        return centerParamsMap;
    }

    /**
     * 拦截无效的账号站点 将拦截原因通知回业务系统
     * @param taskSource
     * @param task
     * @param clientParamsMap
     * @param account
     * @param site
     * @param codeMean
     * @return
     * @throws ParseException
     */
    private TaskResult errorAccountCallBack(TaskSourceList taskSource,Task task, LinkedHashMap clientParamsMap,String account,String site, Map<Integer,String> codeMean ) throws ParseException {
        LinkedHashMap centerParamsMap = generateCenterParams(taskSource,task,clientParamsMap);
        String taskSourceId;
        if (taskSource.getIsSingle() == 0){
            taskSourceId = CacheKey.CIRCLE + "_" + taskSource.getId();
        }else {
            taskSourceId = CacheKey.SINGLE + "_" + taskSource.getId();
        }
        //强制返回失败结果
        int smallestId = taskUtil.getTaskResultSearchSmallestId();
        int resultHashKey = taskResultService.parseResultHashKey(taskSource.getUniqueId(), centerParamsMap);
        String refreshTime = taskSourceListService.generateRefreshTimeByIntervalTime(task.getIntervalType(),task.getIntervalTimes(),taskSource.getExpectedTime());
        TaskResult taskResultInfo = taskResultService.getTaskResultByTaskSourceIdAndResultHashKeyAndRefreshTime(smallestId, taskSourceId,resultHashKey,refreshTime);
        TaskResult taskResult = new TaskResult();

        taskResult.setCreatedTime(DateUtils.getCurrentDate());
        taskResult.setRunMode(0);
        int retryTimes = 1;
        if (taskResultInfo != null){
            retryTimes = taskResultInfo.getRetryTimes() + 1;
            taskResult.setRetryTimes(retryTimes);
            taskResult.setId(taskResultInfo.getId());
            taskResultService.updateTaskResult(taskResult);
        }else {
            taskResult.setTaskType(task.getTaskName());
            taskResult.setTaskSourceId(taskSourceId);
            taskResult.setTaskBufferId("");
            taskResult.setUniqueId(taskSource.getUniqueId());
            taskResult.setResultHashKey(resultHashKey);
            taskResult.setRefreshTime(refreshTime);
            taskResult.setCenterParams(JSONObject.toJSONString(centerParamsMap));
            taskResult.setReturnParams(taskSource.getReturnParams());
            if (codeMean.isEmpty()){
                taskResult.setClientCode(ResponseCode.INVALID_ACCOUNT);
                taskResult.setClientError("店铺账号无效，未生成任务");
            }else{
                for (Map.Entry<Integer, String> entry : codeMean.entrySet()) {
                    taskResult.setClientCode(entry.getKey());
                    taskResult.setClientError(entry.getValue());
                }
            }
            taskResult.setAccount(account);
            taskResult.setSite(site);
            taskResult.setWorkType(taskSource.getWorkType());
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
            return taskResult;
        }
        return null;
    }

    /**
     * 叠加重试
     * @param taskBuffer 任务缓冲区
     */
    private void increaseRetryTimes(TaskBuffer taskBuffer){
        synchronized (taskBuffer.getId().intern()){
            if (taskBuffer.getRetry_times() == 0 && taskBuffer.getIs_in_pool() == 2){
                taskBufferService.offerQueue(taskBuffer.getPriority());
                taskBufferService.updateRetryTimesAndIsInPoolById(1,0,taskBuffer.getId());  //更新为入缓冲区状态
            }else {
                taskBufferService.updateRetryTimesById(taskBuffer.getRetry_times()+1,taskBuffer.getId());
            }
        }
    }
}
