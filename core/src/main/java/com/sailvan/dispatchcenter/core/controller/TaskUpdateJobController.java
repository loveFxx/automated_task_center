package com.sailvan.dispatchcenter.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.sailvan.dispatchcenter.common.cache.InitAccountCache;
import com.sailvan.dispatchcenter.common.constant.CacheKey;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.constant.Event;
import com.sailvan.dispatchcenter.common.constant.TaskStateKey;
import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.pipe.*;
import com.sailvan.dispatchcenter.common.util.CronDateUtils;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.common.util.TaskUtil;
import com.sailvan.dispatchcenter.core.async.AsyncPushTask;
import com.sailvan.dispatchcenter.core.config.TaskPoolConfig;
import com.sailvan.dispatchcenter.core.domain.TaskBuffer;
import com.sailvan.dispatchcenter.core.pool.TaskPool;
import com.sailvan.dispatchcenter.core.service.CoreTaskSourceListService;
import com.sailvan.dispatchcenter.core.service.JobService;
import com.sailvan.dispatchcenter.core.service.TaskBufferService;
import com.sailvan.dispatchcenter.core.service.TaskLogsService;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.*;

/**
 *  任务更新 会涉及到job的更新 由于是多个进程 需要远程调用
 *   @author menghui
 *   @date 2021-10
 */
@RestController
@RequestMapping("/remote")
public class TaskUpdateJobController {

    private static Logger logger = LoggerFactory.getLogger(TaskUpdateJobController.class);

    @Autowired
    JobService jobService;

    @Autowired
    TaskFunnelService taskFunnelService;

    @Autowired
    TaskSourceListService taskSourceListService;

    @Autowired
    TaskBufferService taskBufferService;

    @Autowired
    TaskService taskService;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    TaskPool taskPool;

    @Autowired
    CoreTaskSourceListService coreTaskSourceListService;

    @Autowired
    TaskUtil taskUtil;

    @Autowired
    TaskPoolConfig taskPoolConfig;

    @Autowired
    TaskResultService taskResultService;

    @Autowired
    TaskLogsService taskLogsService;

    @Autowired
    InitAccountCache initAccountCache;

    @Autowired
    AsyncPushTask asyncPushTask;

    @Autowired
    ProxyIpService proxyIpService;

    @Autowired
    com.sailvan.dispatchcenter.db.service.ProxyIpPlatformService proxyIpPlatformService;

    @RequestMapping("/update")
    @ResponseBody
    public void update(@RequestBody String jsonStr) throws ParseException {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        Task task = JSONObject.toJavaObject(jsonObject, Task.class);
        List<TaskFunnel> taskFunnels =  taskFunnelService.listByTaskId(task.getId());
        CronExpression cronExpression = new CronExpression(task.getCronExpression());
        Date date = cronExpression.getNextValidTimeAfter(new Date());
        if (taskFunnels != null){
            for (TaskFunnel taskFunnel: taskFunnels) {
                String jobName = taskFunnel.getTaskName();
                if (taskFunnel.getIsMain() == 1){
                    String expectedTime = DateUtils.getFormatTime(date,"yyyy-MM-dd HH:mm:ss");
                    BigInteger time =  BigInteger.valueOf(date.getTime());

                    taskFunnelService.updateNextFireTimeById(time,taskFunnel.getId());

                    //根据jobName更新任务库预计执行时间
                    taskSourceListService.updateExpectedTimeByJobName(expectedTime,jobName,0,0);
                    jobService.update(jobName,task.getCronExpression());
                }else {
                    String[] split = jobName.split(":");
                    TaskFunnel mainTaskFunnel = taskFunnelService.findMainFunnel(task.getId());
                    BigInteger time = mainTaskFunnel.getNextFireTime().add(BigInteger.valueOf(Long.parseLong(String.valueOf(Integer.parseInt(split[2])-1)) * task.getProduceInterval() * 1000));
                    String expectedTime = DateUtils.convertTimestampToDate(time);
                    taskFunnelService.updateNextFireTimeById(time,taskFunnel.getId());
                    String cron = CronDateUtils.getCron(expectedTime);
                    try{
                        jobService.update(jobName,cron);
                    }catch (Exception e){
                        jobService.createJob(jobName,0,cron);
                        logger.error("更新任务[{}]失败---异常原因：{}",jobName,e.getMessage());
                    }

                    taskSourceListService.updateExpectedTimeByJobName(expectedTime,jobName,0,0);
                }
            }
        }
    }

    /**
     * 根据任务类型更新crontab
     * @param jsonStr
     * @throws ParseException
     */
    @RequestMapping("/update_concurrency")
    @ResponseBody
    public void updateConcurrency(@RequestBody String jsonStr) throws ParseException {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        Task task = taskService.findTaskByName(jsonObject.getString("type"));
        updateCron(task);
    }

    /**
     * 重置所有周期类型任务的crontab
     * @throws ParseException
     */
    @RequestMapping("/update_all_cron")
    @ResponseBody
    public void updateAllCron() throws ParseException {
        List<Task> tasks = taskService.listTasksByTypeAndStatus(1, 1);
        for (Task task : tasks){
            updateCron(task);
        }

    }

    private void updateCron(Task task) throws ParseException {
        int taskId = task.getId();
        List<TaskFunnel> taskFunnels =  taskFunnelService.listByTaskId(taskId);
        for (TaskFunnel taskFunnel : taskFunnels){
            jobService.delete(taskFunnel.getTaskName());
        }

        taskFunnelService.deleteByTaskId(taskId);
        int id = 0;

        while (true){
            List<Integer> ids = taskSourceListService.listTaskSourceByTaskId(taskId, id, task.getProduceCapacity());
            if (ids != null && !ids.isEmpty()){
                coreTaskSourceListService.batchCircleJobChunk(ids,task);
                if (ids.size()==task.getProduceCapacity()){
                    id = ids.get(ids.size()-1);
                }else {
                    break;
                }
            }else {
                break;
            }

        }
    }

    /**
     * 停止某个任务类型的所有crontab
     * @param jsonStr
     */
    @RequestMapping("/pause")
    @ResponseBody
    public void pause(@RequestBody String jsonStr){
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        Task task = JSONObject.toJavaObject(jsonObject, Task.class);
        List<TaskFunnel> taskFunnels =  taskFunnelService.listByTaskId(task.getId());
        if (taskFunnels != null){
            for (TaskFunnel taskFunnel: taskFunnels) {
                jobService.pauseJob(taskFunnel.getTaskName());
            }
        }
    }

    /**
     * 唤醒某个任务类型停止的crontab
     * @param jsonStr
     */
    @RequestMapping("/resume")
    @ResponseBody
    public void resume(@RequestBody String jsonStr){
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        Task task = JSONObject.toJavaObject(jsonObject, Task.class);
        List<TaskFunnel> taskFunnels =  taskFunnelService.listByTaskId(task.getId());
        if (taskFunnels != null){
            for (TaskFunnel taskFunnel: taskFunnels) {
                jobService.resume(taskFunnel.getTaskName());
            }
        }
    }

    /**
     * 根据任务库ID删除任务库
     * @param id 任务库ID
     */
	@RequestMapping("/delete")
    @ResponseBody
    public void delete(String id){
        coreTaskSourceListService.deleteTaskSourceById(id);
    }

    /**
     * 手工入池
     * @param jsonStr
     * @return
     * @throws ParseException
     */
    @RequestMapping("/repush")
    @ResponseBody
    public Object rePush(@RequestBody String jsonStr) throws ParseException {
        HashMap<String,Object> response = new HashMap<>();
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        int id = jsonObject.getIntValue("id");
        int isSingle = jsonObject.getIntValue("isSingle");
        String taskSourceId = "";
        if (isSingle == 0){
            taskSourceId = CacheKey.CIRCLE + "_" + id;
        } else {
            taskSourceId = CacheKey.SINGLE + "_" + id;
        }
        TaskSourceList taskSource = taskSourceListService.findTaskSourceById(id,isSingle);
        TaskBuffer taskBuffer = taskBufferService.findByTaskSourceIdAndCreatedTime(taskSourceId,DateUtils.getDate());
        if (taskBuffer != null){
            if (taskBuffer.getIs_in_pool() == 1){
                response.put("code",0);
                response.put("msg","当前任务在池子中，不可强制入池");
            } else {
                taskBuffer.setIs_enforced(1);
                TaskMetadata taskMetadata = taskPool.buildTaskMetadata(taskBuffer);
                String resultJson = JSONObject.toJSONString(taskMetadata);
                redisUtils.put(Constant.TASK_PREFIX + taskBuffer.getId(), resultJson, 3600 * 24 *2L);
                taskPool.push(taskBuffer.getWork_type(), taskBuffer.getType(), taskBuffer.getPriority(), taskBuffer.getId());
                logger.info("手工强制入任务池，任务库ID:{} 任务缓冲区ID:{}，当前任务池数量--{}", taskBuffer.getTask_source_id(), taskBuffer.getId(), taskPool.getTotalNum());
                int inPoolTimes = taskBuffer.getIn_pool_times() + 1;
                taskBufferService.updateBuffer(1, inPoolTimes, taskBuffer.getId(),1);
                taskLogsService.addTaskLogs(taskBuffer,Event.MANUAL_IN_POOL,"",Constant.MACHINE);
                response.put("code",1);
                response.put("msg","成功入池");
            }
        }else {
            taskSource.setIsEnforced(1);
            createBuffer(taskSource);
            response.put("code",1);
            response.put("msg","成功入池");
        }
        return response;
    }

    /**
     * 周期任务重推
     * @param jsonStr
     * @throws ParseException
     */
    @RequestMapping("/repush_circle_tasks")
    @ResponseBody
    public void rePushCircleTasks(@RequestBody String jsonStr) throws ParseException {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        List<String> workTypeLists = null;
        int isEnforced = jsonObject.getIntValue("is_enforced");
        if(jsonObject.containsKey("work_type")){
            JSONArray workTypes = (JSONArray) jsonObject.get("work_type");
            if (!StringUtils.isEmpty(workTypes.get(0))){
                workTypeLists = JSONObject.parseArray(workTypes.toJSONString(), String.class);
            }
        }
        List<String> taskIdLists = null;
        if(jsonObject.containsKey("task_id")){
            JSONArray taskIds = (JSONArray) jsonObject.get("task_id");
            if (!StringUtils.isEmpty(taskIds.get(0))){
                taskIdLists = JSONObject.parseArray(taskIds.toJSONString(), String.class);
            }
        }

        List<TaskSourceList> taskSourceLists = taskSourceListService.queryTaskSource(workTypeLists,taskIdLists,taskUtil.getCircleTaskSearchSmallestId(),0);
        for (TaskSourceList taskSource: taskSourceLists) {
            TaskBuffer taskBuffer = taskBufferService.findByTaskSourceId(CacheKey.CIRCLE + "_" + taskSource.getId());
            if (taskBuffer != null){
                if (taskBuffer.getIs_in_pool() != 1){
                    taskBuffer.setIs_enforced(isEnforced);
                    TaskMetadata taskMetadata = taskPool.buildTaskMetadata(taskBuffer);
                    String resultJson = JSONObject.toJSONString(taskMetadata);
                    redisUtils.put(Constant.TASK_PREFIX + taskBuffer.getId(), resultJson, 3600 * 24 *2L);
                    taskPool.push(taskBuffer.getWork_type(), taskBuffer.getType(), taskBuffer.getPriority(), taskBuffer.getId());
                    int inPoolTimes = taskBuffer.getIn_pool_times() + 1;
                    taskBufferService.updateBuffer(1, inPoolTimes, taskBuffer.getId(),isEnforced);
                    taskLogsService.addTaskLogs(taskBuffer,Event.MANUAL_IN_POOL,"",Constant.MACHINE);
                }
            }else {
                taskSource.setIsEnforced(isEnforced);
                createBuffer(taskSource);
            }
        }
    }

    private void createBuffer(TaskSourceList taskSource) throws ParseException {
        Task task = taskService.findTaskById(taskSource.getTaskId());
        LinkedHashMap clientParamsMap = JSON.parseObject(taskSource.getParams(),LinkedHashMap.class, Feature.OrderedField);
        if (clientParamsMap.containsKey("account") && clientParamsMap.containsKey("site"))
        {
            Map<Integer,String> codeMean = new HashMap<>();
            String account = String.valueOf(clientParamsMap.get("account"));
            String site = String.valueOf(clientParamsMap.get("site"));
            if (!initAccountCache.isInvalidByAccountSite(task.getTaskName(), account, site, codeMean)){
                manualCreate(taskSource,clientParamsMap,task);
            }
        }else {
            manualCreate(taskSource,clientParamsMap,task);
        }
    }

    public void manualCreate(TaskSourceList taskSource, LinkedHashMap clientParamsMap, Task task) throws ParseException {
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
        int resultHashKey = taskResultService.parseResultHashKey(taskSource.getUniqueId(), centerParamsMap);

        String time;
        if (!StringUtils.isEmpty(taskSource.getLastCreateTime())){
            time = taskSource.getLastCreateTime();
        }else {
            time = taskSource.getExpectedTime();
        }
        String refreshTime = taskSourceListService.generateRefreshTimeByIntervalTime(task.getIntervalType(),task.getIntervalTimes(),time);
        insertBufferEvent(taskSource,task,resultHashKey,centerParamsMap,refreshTime);
    }

    private void insertBufferEvent(TaskSourceList taskSource, Task task, int resultHashKey,LinkedHashMap centerParamsMap,String refreshTime) throws ParseException {
        //生成任务并入任务池缓冲区
        TaskBuffer taskBuffer = insertTaskBuffer(taskSource,task,resultHashKey,centerParamsMap,refreshTime);
        boolean result = taskBufferService.offerQueue(taskBuffer.getPriority());
        TaskMetadata taskMetadata = taskPool.buildTaskMetadata(taskBuffer);
        taskLogsService.addTaskLogs(taskBuffer,Event.MANUAL_CREATE_TASK,"",task.getRunMode());
        logger.info("手工生成任务并入任务池缓冲区，任务缓冲区ID:{}，任务库ID:{}，是否单次:{}，任务数据：{}",taskBuffer.getId(),taskSource.getId(),taskSource.getIsSingle(),JSONObject.toJSONString(taskMetadata));
    }

    private TaskBuffer insertTaskBuffer(TaskSourceList taskSource, Task task, int resultHashKey, LinkedHashMap centerParamsMap,String refreshTime) throws ParseException {

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
        taskBuffer.setClient_params(taskSource.getParams());
        taskBuffer.setCenter_params(JSONObject.toJSONString(centerParamsMap));
        taskBuffer.setPriority(taskSource.getPriority()+1);
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


    @RequestMapping(value = "/deleteTasksInPool")
    @ResponseBody
    public String deleteTasksInPool(@RequestBody String jsonStr){
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        asyncPushTask.deleteTasksInPoolAsync(jsonObject.getString("type"));
        return "success";
    }

    @RequestMapping(value = "/getDelayQueue")
    @ResponseBody
    public List<DelayQueueInfo> getDelayQueue(){
        List<DelayQueueInfo> asyncQueueInfoList = asyncPushTask.getDelayQueue();
        return asyncQueueInfoList;
    }

    @RequestMapping(value = "/rePushToSystem")
    @ResponseBody
    public String rePushToSystem(@RequestBody String jsonStr) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        String type = jsonObject.getString("type");
        int minResultId = jsonObject.getInteger("minResultId");
        int maxResultId = jsonObject.getInteger("maxResultId");
        String minTime = jsonObject.getString("minTime");
        String maxTime = jsonObject.getString("maxTime");
        int systemId = jsonObject.getInteger("systemId");

        asyncPushTask.rePushToSystem(systemId,type,minResultId,maxResultId,minTime,maxTime);
        return "success";
    }

    @RequestMapping(value = "/fixTaskResult")
    @ResponseBody
    public String fixTaskResult(@RequestBody String jsonStr){
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        String type = jsonObject.getString("type");
        int minResultId = jsonObject.getInteger("minResultId");
        int maxResultId = jsonObject.getInteger("maxResultId");

        asyncPushTask.fixTaskResult(type,minResultId,maxResultId);
        return "success";
    }

    @RequestMapping(value = "/countTasksInPool",method = RequestMethod.POST)
    public Object countTasksInPool(@RequestParam(value = "type",required = false) String type){
        List<HashMap<String,Integer>> list = new ArrayList<>();
        if (StringUtils.isEmpty(type)){
            List<Task> tasks = taskService.listTask();
            for (Task task : tasks) {
                if (task.getStatus() == 0) {
                    continue;
                }
                int i = taskBufferService.countTasksByType(1, task.getTaskName());
                HashMap<String,Integer> map = new HashMap<>();
                map.put(task.getTaskName(),i);
                list.add(map);
            }

        }else{
            int i = taskBufferService.countTasksByType(1, type);
            HashMap<String,Integer> map = new HashMap<>();
            map.put(type,i);
            list.add(map);
        }

        return list;
    }

    @RequestMapping("/fixProxyIpBug")
    @ResponseBody
    public void fixProxyIpBug(){
        List<ProxyIp> proxyIpAll = proxyIpService.getProxyIpAll();
        for (ProxyIp proxyIp : proxyIpAll){
            String[] split = proxyIp.getIp().split(":");
            if (!split[0].equals("https")){
                String crawlPlatform = proxyIp.getCrawlPlatform();
                String[] split1 = crawlPlatform.split(",");
                StringBuilder stringBuilder = new StringBuilder();
                for (String s: split1){
                    if (!s.equals("7") && !s.equals("8")){
                        stringBuilder.append(s).append(",");
                    }
                }
                stringBuilder.delete(stringBuilder.length()-1,stringBuilder.length());
                proxyIp.setCrawlPlatform(stringBuilder.toString());
                proxyIpService.update(proxyIp);
                proxyIpPlatformService.deleteByProxyIdAndPlatform(proxyIp.getId(),"Amazon_VC");
                proxyIpPlatformService.deleteByProxyIdAndPlatform(proxyIp.getId(),"AmazonDaemon");
            }
        }
    }
}
