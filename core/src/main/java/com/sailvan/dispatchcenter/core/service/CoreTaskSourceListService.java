package com.sailvan.dispatchcenter.core.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.cache.InitAccountCache;
import com.sailvan.dispatchcenter.common.constant.CacheKey;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.constant.TaskStateKey;
import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.pipe.*;
import com.sailvan.dispatchcenter.common.util.*;
import com.sailvan.dispatchcenter.core.Funnel.ConcurrentFunnel;
import com.sailvan.dispatchcenter.core.config.TaskPoolConfig;
import com.sailvan.dispatchcenter.core.domain.TaskBuffer;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.core.pool.TaskPool;
import com.github.pagehelper.util.StringUtil;
import com.google.common.base.Joiner;
import lombok.SneakyThrows;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Primary
public class CoreTaskSourceListService {

    private static Logger logger = LoggerFactory.getLogger(CoreTaskSourceListService.class);

    @Autowired
    TaskService taskService;

    @Autowired
    ColumnService columnService;

    @Autowired
    JobService jobService;

    @Autowired
    TaskPool taskPool;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    BusinessSystemService businessSystemService;

    @Autowired
    InitAccountCache initAccountCache;

    @Autowired
    TaskFunnelService taskFunnelService;

    @Autowired
    ConcurrentFunnel concurrentFunnel;

    @Autowired
    TaskBufferService taskBufferService;

    @Autowired
    TaskUtil taskUtil;

    @Autowired
    TaskSourceListService taskSourceListService;

    @Autowired
    BusinessSystemTaskService businessSystemTaskService;

    @Autowired
    TaskPoolConfig taskPoolConfig;

    @Autowired
    ProxyIpPlatformService proxyIpPlatformService;

    @Autowired
    PlatformService platformService;

    /**
     * 验证任务类型
     * @param taskName 任务类型
     * @return 任务实体类
     */
    private Object taskValidator(String taskName){
        ApiResponse apiResponse = new ApiResponse();

        if (StringUtil.isEmpty(taskName)){
            return apiResponse.error(ResponseCode.ERROR_CODE,"任务类型不能为空",null);
        }

        Task task = taskService.getTaskByTaskName(taskName);

        if (task == null){
            return apiResponse.error(ResponseCode.ERROR_CODE,"任务类型不存在",null);
        }

        if (task.getStatus() == Constant.STATUS_INVALID){
            return apiResponse.error(ResponseCode.ERROR_CODE,"当前任务类型已被禁用",null);
        }
        return task;
    }

    /**
     * 验证系统
     * @param systemName 系统名
     * @param task 任务
     * @return 系统实体类
     */
    private Object systemValidator(String systemName, Task task){
        ApiResponse apiResponse = new ApiResponse();

        if (StringUtil.isEmpty(systemName)){
            return apiResponse.error(ResponseCode.ERROR_CODE,"系统名不能为空",null);
        }

        BusinessSystem system = businessSystemService.findBySystemName(systemName);

        if (system == null){
            return apiResponse.error(301,"当前系统并未配置，请联系负责人设置",null);
        }

        if (system.getStatus() == Constant.STATUS_INVALID){
            return apiResponse.error(301,"当前系统并未启用，请联系负责人设置",null);
        }

        List<BusinessSystemTask> businessSystemTask = businessSystemTaskService.getBusinessSystemTaskBySystemIdAndTaskIdAndStatus(system.getId(), task.getId(), Constant.STATUS_VALID);
        if (businessSystemTask == null || businessSystemTask.isEmpty()){
            return apiResponse.error(301,"当前系统未设置任务类型，请联系负责人设置",null);
        }

        return system;
    }

    /**
     * 验证业务系统传递参数
     * @param task
     * @param checkRepeatMap  加入判重的参数
     * @param taskMap 任务参数
     * @param returnMap  返回业务系统参数
     * @param idFlagMap  返回任务库ID时的标志参数
     * @param parseObject 业务系统传递参数
     * @param jsonObject
     * @return
     */
    private Object clientParamsValidator(Task task, LinkedHashMap checkRepeatMap, LinkedHashMap taskMap,
                                         LinkedHashMap returnMap, HashMap<String,Object> idFlagMap,
                                         JSONObject parseObject, JSONObject jsonObject){

        ApiResponse apiResponse = new ApiResponse();
        HashMap<String,Object> allParams = new HashMap<>();

        int isSingle = 0;
        if (jsonObject.containsKey("is_single")){
            isSingle = jsonObject.getIntValue("is_single");
        }

        if (task.getType() == Constant.SINGLE_TASK){
            isSingle = 1;
        }

        if (task.getIsTimely() == 1 && (parseObject.containsKey("start_date") || parseObject.containsKey("end_date"))){
            return apiResponse.error(ResponseCode.ERROR_CODE,"当前任务为及时性任务，所传参数start_date或end_date不合法",null);
        }

        List<Column> columns = columnService.listColumnsByTaskId(task.getId());

        for (Column column : columns) {
            if (parseObject.containsKey(column.getColumnsName())){
                if (!columnService.validateColumns(column.getColumnType(),parseObject.get(column.getColumnsName()))){
                    return apiResponse.error(ResponseCode.ERROR_CODE, column.getColumnsName()+"-参数类型应为" + column.getColumnType(),null);
                }

                //有判重标志，加入判重
                if (column.getIsCombinedUnique() == 1){
                    checkRepeatMap.put(column.getColumnsName(), parseObject.get(column.getColumnsName()));
                }

                //有需带在结果返回的标识
                if (column.getIsReturnFlag() == 1){
                    returnMap.put(column.getColumnsName(), parseObject.get(column.getColumnsName()));
                }

                //若为必传参数或者中心端组成参数，需加入，传递至客户端
                if (column.getIsRequired() == 1 || column.getIsCombined()== 1){
                    taskMap.put(column.getColumnsName(), parseObject.get(column.getColumnsName()));
                }

                //为返回ID的标识
                if (column.getIsIdFlag() == 1){
                    idFlagMap.put(column.getColumnsName(), parseObject.get(column.getColumnsName()));
                }
                allParams.put(column.getColumnsName(), parseObject.get(column.getColumnsName()));

            }else{
                if (column.getIsRequired() == 1){
                    return apiResponse.error(ResponseCode.ERROR_CODE,"参数" + column.getColumnsName()+"不存在",null);
                }
                //周期性单次必传
                if (isSingle == 1 && task.getType() == Constant.CIRCLE_TASK && column.getIsCombinedUnique() == 1) {
                    return apiResponse.error(ResponseCode.ERROR_CODE,"参数" + column.getColumnsName()+"不存在",null);
                }
            }
        }
        //带上重试标识的任务需根据代理IP池健康度进行拦截(无效率:15%)
        if (allParams.containsKey("is_retry")){
            Object isRetry = allParams.get("is_retry");
            if (Integer.parseInt(String.valueOf(isRetry)) == 1){
                if (task.getLargeTaskType() == Constant.LARGE_TASK_TYPE_CRAWL_PLATFORM){
                    Platform platform = platformService.getPlatformById(Integer.parseInt(task.getExecutePlatforms()));
                    int totalNum = proxyIpPlatformService.countByPlatformAndStatus(platform.getPlatformName(),Constant.STATUS_VALID);
                    int invalidNum = proxyIpPlatformService.countByPlatformAndOpenTimestamp(platform.getPlatformName(),System.currentTimeMillis());

                    float invalidRate = Float.parseFloat(CommonUtils.divide(invalidNum,totalNum));
                    if (invalidRate*100>15){
                        return apiResponse.error(ResponseCode.ERROR_CODE,"当前代理IP池无效率为"+invalidRate*100+"%",null);
                    }
                }
            }
        }
        return parseObject;
    }

    /**
     * 添加任务库（单个）
     * @param jsonObject
     * @return 返回任务库ID
     */
    @SneakyThrows
    public Object addTaskSource(JSONObject jsonObject){

        ApiResponse apiResponse = new ApiResponse();

        String taskName = jsonObject.getString("type");
        Object taskValidator = taskValidator(taskName);
        if (taskValidator instanceof ApiResponseDomain){
            return taskValidator;
        }
        Task task = (Task) taskValidator;

        String systemName = jsonObject.getString("system_name");
        Object systemValidator = systemValidator(systemName, task);
        if (systemValidator instanceof ApiResponseDomain){
            return systemValidator;
        }
        BusinessSystem system = (BusinessSystem) systemValidator;

        String params = jsonObject.getString("params");
        if (StringUtil.isEmpty(params)){
            return apiResponse.error(ResponseCode.ERROR_CODE,"参数params不能为空",null);
        }

        if (jsonObject.containsKey("is_enforced") && !(jsonObject.containsKey("is_single") && jsonObject.getInteger("is_single") == 1)){
            return apiResponse.error(ResponseCode.ERROR_CODE,"传参数is_enforced需携带参数is_single为1",null);
        }

        JSONObject parseObject = JSONObject.parseObject(params);

        Object result = addTaskSource(task,system,jsonObject,parseObject);
        if (result instanceof ApiResponseDomain){
            return result;
        }

        return apiResponse.success("success",result);
    }

    public Object addTaskSource(Task task, BusinessSystem system, JSONObject jsonObject, JSONObject parseObject) throws ParseException {
        ApiResponse apiResponse = new ApiResponse();
        LinkedHashMap checkRepeatMap = new LinkedHashMap();
        LinkedHashMap taskMap = new LinkedHashMap();
        LinkedHashMap returnMap = new LinkedHashMap();
        HashMap<String,Object> idFlagMap = new HashMap<>();
        Object clientParamsValidator = clientParamsValidator(task, checkRepeatMap, taskMap, returnMap, idFlagMap, parseObject, jsonObject);
        if (clientParamsValidator instanceof ApiResponseDomain){
            return clientParamsValidator;
        }

        HashMap<String,Object> hashMap = new HashMap<>();
        //若任务类型配置的大类型为按爬虫平台，否则为账号站点，需要处理为账号洲
        if (task.getLargeTaskType() == Constant.LARGE_TASK_TYPE_CRAWL_PLATFORM){
            String platformId = task.getExecutePlatforms();
            if (StringUtil.isNotEmpty(platformId)){
                String workType = Constant.EXECUTE_PLATFORMS.get(platformId);
                hashMap = getTaskSourceId(jsonObject, task, workType, system, checkRepeatMap, returnMap, taskMap, idFlagMap);
            }
        }else {
            String account = parseObject.getString("account");
            if (StringUtil.isEmpty(account)){
                return apiResponse.error(ResponseCode.ERROR_CODE,"参数account不能为空",null);
            }
            String site = parseObject.getString("site");
            if (StringUtil.isEmpty(site)){
                return apiResponse.error(ResponseCode.ERROR_CODE,"参数site不能为空",null);
            }
            Map<Integer,String> codeMean = new HashMap<>();
            if(initAccountCache.isInvalidByAccountSite(task.getTaskName(), account, site, codeMean)){
                if(!codeMean.isEmpty()){
                    Set<Map.Entry<Integer, String>> entries = codeMean.entrySet();
                    for (Map.Entry<Integer, String> entry : entries) {
                        return apiResponse.error(entry.getKey(),"此账号站点:"+account+"_"+site+" 的状态是:"+ entry.getValue()+",不可添加任务",null);
                    }
                }else {
                    return apiResponse.error(ResponseCode.ERROR_CODE,"此账号站点:"+account+"_"+site+" 的状态是:未知原因,不可添加任务",null);
                }
            }
            String workType = account + "_" + Constant.SITE_CONTINENT_MAP.get(site);
            hashMap = getTaskSourceId(jsonObject, task, workType, system, checkRepeatMap, returnMap, taskMap, idFlagMap);

        }
        return hashMap;
    }

    public HashMap<String,Object> getTaskSourceId(JSONObject jsonObject, Task task, String workType,
                                                  BusinessSystem system,LinkedHashMap checkRepeatMap,LinkedHashMap returnMap,
                                                  LinkedHashMap taskMap, HashMap<String,Object> idFlagMap) throws ParseException {
        TaskSourceList constructData = constructTaskSourceList(jsonObject, task, workType, system, checkRepeatMap, returnMap, taskMap);
        int id;
        HashMap<String,Object> hashMap = buildResponseMap(idFlagMap);
        try {
            if (constructData.getIsSingle() == 1) {
                //强制生成任务
                if (jsonObject.containsKey("is_enforced") && jsonObject.getInteger("is_enforced") == 1) {
                    constructData.setId(taskUtil.getSingleNextTaskResultId());
                    id = taskSourceListService.insert(constructData);
                    List<Integer> ids = new ArrayList<>();
                    ids.add(id);
                    batchSingleJobChunk(ids, task);
                    hashMap.put("id", CacheKey.SINGLE + "_" + id);
                    hashMap.put("is_exists", 0);
                } else {
                    //任务库执行时间在有效期内，不重复生成
                    int smallestId = taskUtil.getSingleTaskSearchSmallestId();
                    TaskSourceList taskSourceList = taskSourceListService.getTaskSourceListByUniqueIdAndIsSingleAndRefreshTime(smallestId, constructData.getUniqueId(), constructData.getIsSingle(), constructData.getRefreshTime());
                    if (taskSourceList != null && taskSourceList.getIsEnforced() == 0) {
                        logger.info("任务库[{}]执行时间在有效期内，不重复生成", "single_" + taskSourceList.getId());
                        updateSystemIds(taskSourceList, system, constructData);
                        checkTaskBuffer(taskSourceList);
                        hashMap.put("id", CacheKey.SINGLE + "_" + taskSourceList.getId());
                        hashMap.put("is_exists", 1);
                        return hashMap;
                    }
                    constructData.setId(taskUtil.getSingleNextTaskResultId());
                    id = taskSourceListService.insert(constructData);
                    List<Integer> ids = new ArrayList<>();
                    ids.add(id);
                    batchSingleJobChunk(ids, task);
                    hashMap.put("id", CacheKey.SINGLE + "_" + id);
                    hashMap.put("is_exists", 0);
                }
            } else {
                int smallestId = taskUtil.getCircleTaskSearchSmallestId();
                TaskSourceList taskSourceList = taskSourceListService.getTaskSourceListByUniqueIdAndIsSingle(smallestId, constructData.getUniqueId(), constructData.getIsSingle());
                if (taskSourceList != null) {
                    updateSystemIds(taskSourceList, system, constructData);
                    id = taskSourceList.getId();
                    hashMap.put("id", CacheKey.CIRCLE + "_" + id);
                    hashMap.put("is_exists", 1);
                } else {
                    constructData.setId(taskUtil.getCircleNextTaskResultId());
                    id = taskSourceListService.insert(constructData);
                    List<Integer> ids = new ArrayList<>();
                    ids.add(id);
                    batchCircleJobChunk(ids, task);
                    hashMap.put("id", CacheKey.CIRCLE + "_" + id);
                    hashMap.put("is_exists", 0);
                }
            }
        } catch (Exception e) {
            logger.error("任务库插入更新异常:{}", e.getMessage());
            hashMap.put("id", "");
            hashMap.put("is_exists", 0);
            hashMap.put("error", e.getMessage());
        }

        return hashMap;
    }

    /**
     * 批量添加任务库
     * @param jsonObject
     * @return
     * @throws ParseException
     */
    public Object bulkAddTaskSource(JSONObject jsonObject) throws ParseException{
        ApiResponse apiResponse = new ApiResponse();

        String taskName = jsonObject.getString("type");
        Object taskValidator = taskValidator(taskName);
        if (taskValidator instanceof ApiResponseDomain){
            return taskValidator;
        }
        Task task = (Task) taskValidator;

        String systemName = jsonObject.getString("system_name");
        Object systemValidator = systemValidator(systemName, task);
        if (systemValidator instanceof ApiResponseDomain){
            return systemValidator;
        }
        BusinessSystem system = (BusinessSystem) systemValidator;

        String params = jsonObject.getString("params");
        if (StringUtil.isEmpty(params)){
            return apiResponse.error(ResponseCode.ERROR_CODE,"参数params不能为空",null);
        }

        JSONArray jsonArray = JSONArray.parseArray(params);


        if (jsonArray.size() > taskPoolConfig.getBatchLimitNum()){
            return apiResponse.error(ResponseCode.ERROR_CODE,"当前上限为" + taskPoolConfig.getBatchLimitNum(),null);
        }

        List<Map<String,Object>> lists = bulkAddTaskSource(task, system, jsonObject, jsonArray);

        if (lists.isEmpty()){
            return apiResponse.error(ResponseCode.UNkOWN_ERROR,"未知错误",null);
        }
        return apiResponse.success("success",lists);
    }

    private List<Map<String,Object>> bulkAddTaskSource(Task task, BusinessSystem system, JSONObject jsonObject, JSONArray jsonArray) throws ParseException {
        List<TaskSourceList> taskSourceLists = new ArrayList<>();
        List<Map<String,Object>> resultLists = new LinkedList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject parseObject = (JSONObject) jsonArray.get(i);

            LinkedHashMap checkRepeatMap = new LinkedHashMap();
            LinkedHashMap taskMap = new LinkedHashMap();
            LinkedHashMap returnMap = new LinkedHashMap();
            HashMap<String,Object> idFlagMap = new HashMap<>();
            Object clientParamsValidator = clientParamsValidator(task, checkRepeatMap, taskMap, returnMap,idFlagMap, parseObject, jsonObject);
            if (clientParamsValidator instanceof ApiResponseDomain){
                resultLists.add(buildError(((ApiResponseDomain) clientParamsValidator).getMsg(),idFlagMap));
            }

            //若任务类型配置的大类型为按爬虫平台，否则为账号站点，需要处理为账号洲
            if (task.getLargeTaskType() == Constant.LARGE_TASK_TYPE_CRAWL_PLATFORM){
                String platformId = task.getExecutePlatforms();
                if (StringUtil.isNotEmpty(platformId)){
                    String workType = Constant.EXECUTE_PLATFORMS.get(platformId);
                    bulkAddTaskSource(taskSourceLists, jsonObject, task, workType, system, checkRepeatMap, returnMap, taskMap, resultLists,idFlagMap);
                }
            }else {
                String account = parseObject.getString("account");
                if (StringUtil.isEmpty(account)){
                    resultLists.add(buildError("参数account不能为空",idFlagMap));
                }
                String site = parseObject.getString("site");
                if (StringUtil.isEmpty(site)){
                    resultLists.add(buildError("参数site不能为空",idFlagMap));
                }
                Map<Integer,String> codeMean = new HashMap<>();
                if(initAccountCache.isInvalidByAccountSite(task.getTaskName(), account, site, codeMean)){
                    if(!codeMean.isEmpty()){
                        Set<Map.Entry<Integer, String>> entries = codeMean.entrySet();
                        for (Map.Entry<Integer, String> entry : entries) {
                            resultLists.add(buildError("此账号站点:"+account+"_"+site+" 的状态是:"+ entry.getValue()+",不可添加任务",idFlagMap));
                        }
                    }else {
                        resultLists.add(buildError("此账号站点:"+account+"_"+site+" 的状态是:未知原因,不可添加任务",idFlagMap));
                    }
                }else {
                    String workType = account + "_" + Constant.SITE_CONTINENT_MAP.get(site);
                    bulkAddTaskSource(taskSourceLists, jsonObject, task, workType, system, checkRepeatMap, returnMap, taskMap, resultLists,idFlagMap);
                }
            }
        }

        //新的任务批量插入库
        if (!taskSourceLists.isEmpty()){
            taskSourceListService.batchInsertTaskSource(taskSourceLists);

            String prefix;
            if (task.getType() == Constant.CIRCLE_TASK){
                if (jsonObject.containsKey("is_single") && jsonObject.getInteger("is_single") == 1){
                    prefix = CacheKey.SINGLE;
                }else {
                    prefix = CacheKey.CIRCLE;
                }
            }else {
                prefix = CacheKey.SINGLE;
            }
            int i = 0;
            for (Map<String,Object> map : resultLists){
                //判断是否有id标志
                if (!map.containsKey("id")){
                    TaskSourceList taskSourceList = taskSourceLists.get(i);
                    map.put("id",prefix + "_" + taskSourceList.getId());
                    map.put("is_exists", 0);
                    i++;
                }
            }

            List<Integer> taskSourceListIds = taskSourceLists.stream().map(TaskSourceList::getId).collect(Collectors.toList());
            if (prefix.equals(CacheKey.SINGLE)){
                batchSingleJobChunk(taskSourceListIds,task);
            }else {
                batchCircleJobChunk(taskSourceListIds,task);
            }
        }

        return resultLists;
    }

    private HashMap<String,Object> buildError(String error,HashMap<String,Object> idFlagMap){
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("id", "");
        hashMap.put("is_exists", 0);
        hashMap.put("error", error);
        for (Map.Entry<String,Object> entry: idFlagMap.entrySet())  {
            hashMap.put(entry.getKey(), entry.getValue());
        }
        return hashMap;
    }

    /**
     * 有idFlag标识带回去返回
     * @param idFlagMap
     * @return
     */
    private HashMap<String,Object> buildResponseMap(HashMap<String,Object> idFlagMap){
        HashMap<String,Object> hashMap = new HashMap<>();
        for (Map.Entry<String,Object> entry: idFlagMap.entrySet())  {
            hashMap.put(entry.getKey(), entry.getValue());
        }
        return hashMap;
    }

    /**
     * 布隆过滤器判断任务是否重复，不重复存入taskSourceLists
     * @param taskSourceLists
     * @param jsonObject
     * @param task
     * @param workType
     * @param system
     * @param checkRepeatMap
     * @param returnMap
     * @param taskMap
     * @param resultLists
     * @throws ParseException
     */
    public void bulkAddTaskSource(List<TaskSourceList> taskSourceLists, JSONObject jsonObject, Task task, String workType,
                                     BusinessSystem system,LinkedHashMap checkRepeatMap,LinkedHashMap returnMap,
                                  LinkedHashMap taskMap, List<Map<String,Object>> resultLists,HashMap<String,Object> idFlagMap) throws ParseException {
        TaskSourceList constructData = constructTaskSourceList(jsonObject, task, workType, system, checkRepeatMap, returnMap, taskMap);
        HashMap<String,Object> hashMap = buildResponseMap(idFlagMap);

        if (constructData.getIsSingle() == 1) {
            //强制生成任务
            if (jsonObject.containsKey("is_enforced") && jsonObject.getInteger("is_enforced") == 1) {
                constructData.setId(taskUtil.getSingleNextTaskResultId());
                taskSourceLists.add(constructData);
                resultLists.add(hashMap);
            } else {
                //任务库执行时间在有效期内，不重复生成，单次任务量大用布隆过滤器按天去判重
                if (Constant.bloomFilter.mightContain(constructData.getUniqueId() + constructData.getRefreshTime().trim())) {
                    int smallestId = taskUtil.getSingleTaskSearchSmallestId();
                    TaskSourceList taskSourceList = taskSourceListService.getTaskSourceListByUniqueIdAndIsSingleAndRefreshTime(smallestId, constructData.getUniqueId(), constructData.getIsSingle(), constructData.getRefreshTime());
                    if (taskSourceList != null && taskSourceList.getIsEnforced() == 0) {
                        logger.info("任务库[{}]执行时间在有效期内，不重复生成", "single_" + taskSourceList.getId());
                        updateSystemIds(taskSourceList, system, constructData);
                        checkTaskBuffer(taskSourceList);
                        hashMap.put("id", CacheKey.SINGLE + "_" + taskSourceList.getId());
                        hashMap.put("is_exists", 1);
                        resultLists.add(hashMap);
                    }else {
                        constructData.setId(taskUtil.getSingleNextTaskResultId());
                        taskSourceLists.add(constructData);
                        resultLists.add(hashMap);
                    }
                } else {
                    constructData.setId(taskUtil.getSingleNextTaskResultId());
                    taskSourceLists.add(constructData);
                    resultLists.add(hashMap);
                    Constant.bloomFilter.put(constructData.getUniqueId() + constructData.getRefreshTime().trim());
                }
            }
        } else {
            int smallestId = taskUtil.getCircleTaskSearchSmallestId();
            TaskSourceList taskSourceList = taskSourceListService.getTaskSourceListByUniqueIdAndIsSingle(smallestId, constructData.getUniqueId(), constructData.getIsSingle());
            if (taskSourceList != null) {
                updateSystemIds(taskSourceList, system, constructData);
                hashMap.put("id", CacheKey.CIRCLE + "_" + taskSourceList.getId());
                hashMap.put("is_exists", 1);
                resultLists.add(hashMap);
            } else {
                constructData.setId(taskUtil.getCircleNextTaskResultId());
                taskSourceLists.add(constructData);
                resultLists.add(hashMap);
            }
        }
    }


    /**
     *  更新有哪些系统使用了这个任务库
     * @param taskSourceList
     * @param system
     * @param constructData
     */
    private void updateSystemIds(TaskSourceList taskSourceList, BusinessSystem system, TaskSourceList constructData){
        List<String> list = new ArrayList<>(Arrays.asList(taskSourceList.getSystemId().split(",")));
        if (!list.contains(String.valueOf(system.getId()))){
            list.add(String.valueOf(system.getId()));
            constructData.setSystemId(Joiner.on(",").join(list));
            constructData.setId(taskSourceList.getId());
        }
        taskSourceListService.update(constructData);
    }


    /**
     *  构建任务库参数
     * @param jsonObject
     * @param task
     * @param workType
     * @param system
     * @param checkRepeatMap
     * @param returnMap
     * @param taskMap
     * @return
     */
    private TaskSourceList constructTaskSourceList(JSONObject jsonObject, Task task, String workType, BusinessSystem system,LinkedHashMap checkRepeatMap,LinkedHashMap returnMap, LinkedHashMap taskMap) throws ParseException {
        int taskId = task.getId();
        //是否带单次标识
        int isSingle = 0;
        if (jsonObject.containsKey("is_single")){
            isSingle = jsonObject.getIntValue("is_single");
        }
        //任务类型
        int type;
        if (task.getType() == Constant.SINGLE_TASK){
            type = Constant.SINGLE_TASK;
            isSingle = 1;
        }else{
            if (isSingle > 0){
                type = Constant.CIRCLE_SINGLE_TASK;
            }else{
                type = Constant.CIRCLE_TASK;
            }
        }
        //hash key 任务类型，大类型，业务系统传递的参数
        int uniqueId;
        String  params = null;
        if (checkRepeatMap.isEmpty()){
            uniqueId = taskSourceListService.parseUniqueId(task.getTaskName(),workType,params);
        }else {
            params = JSON.toJSONString(checkRepeatMap);
            uniqueId = taskSourceListService.parseUniqueId(task.getTaskName(),workType,params);
        }

        int priority;
        if (jsonObject.containsKey("priority")){
            priority = jsonObject.getIntValue("priority");
            //兼容逻辑，外部传入小于1的优先级默认为配置的优先级
            if (priority < 1){
                priority = task.getPriority();
            }
        }else{
            priority = task.getPriority();
        }

        //是否带强制标识
        int isEnforced = 0;
        if (jsonObject.containsKey("is_enforced")){
            isEnforced = jsonObject.getIntValue("is_enforced");
        }

        //是否有预计执行时间
        String expectedTime = null;
        if (jsonObject.containsKey("expected_time")){
            expectedTime = jsonObject.getString("expected_time");
        }else{
            if (isSingle > 0){
                //当前时间加一分钟
                Date date = new Date(System.currentTimeMillis() + 60000);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                expectedTime = formatter.format(date);
            }
        }

        String refreshTime = null;
        //有效时间
        if (expectedTime != null){
            refreshTime = taskSourceListService.generateRefreshTimeByIntervalTime(task.getIntervalType(),task.getIntervalTimes(),expectedTime);
        }

        TaskSourceList data = new TaskSourceList();

        data.setUniqueId(uniqueId);
        data.setTaskId(taskId);
        data.setType(type);
        data.setSystemId(String.valueOf(system.getId()));
        data.setPriority(priority);
        data.setIsEnforced(isEnforced);
        data.setIsSingle(isSingle);
        data.setWorkType(workType);
        data.setParams(JSON.toJSONString(taskMap));
        if (!returnMap.isEmpty()){
            data.setReturnParams(JSON.toJSONString(returnMap));
        }
        data.setExpectedTime(expectedTime);
        data.setLastCreateTime(expectedTime);
        data.setRefreshTime(refreshTime);
        data.setTaskState(TaskStateKey.TASK_STATE_NOTGENERATED);

        return data;
    }

    /**
     * 删除任务库，已生成的任务数据不作删除
     * @param id 任务库Id
     */
    @SneakyThrows
    public synchronized ApiResponseDomain deleteTaskSourceById(String id){
        ApiResponse apiResponse = new ApiResponse();
        String[] s = id.split("_");
        int isSingle;
        if (s[0].equals(CacheKey.CIRCLE)){
            isSingle = 0;
        }else {
            isSingle = 1;
        }
        TaskSourceList taskSource = taskSourceListService.findTaskSourceById(Integer.parseInt(s[1]),isSingle);

        if (taskSource == null){
            return apiResponse.error(ResponseCode.ERROR_CODE,"任务库ID["+id+"]不存在",null);
        }
        taskSourceListService.delete(s[1],isSingle);
        String jobName = taskSource.getJobName();
        //如果是周期性任务 修改延迟入池表
        if (isSingle == 0){
            TaskFunnel funnel = taskFunnelService.findByTaskName(jobName);
            if (funnel.getTaskNum() - 1 == 0){
                jobService.delete(jobName);
                taskFunnelService.deleteById(funnel.getId());
                logger.info("接口删除当前调度器中对应的任务-id:{},jobName:{}",id,jobName);
            }else {
                taskFunnelService.updateTaskNumById(funnel.getTaskNum() -1, funnel.getId());
            }
        }else{  //如果为单次性任务
            //当前还未生成任务，直接删除任务调度
            Date now = DateUtils.getCurrentDateToDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date expectedTime = sdf.parse(taskSource.getExpectedTime());
            if (now.compareTo(expectedTime) <= 0) {
                List<TaskSourceList> taskSourceLists = taskSourceListService.listTaskSourcesByJobName(jobName, isSingle, taskUtil.getSingleTaskSearchSmallestId());
                if (taskSourceLists == null) {
                    jobService.delete(jobName);
                    logger.info("接口删除当前调度器中对应的任务-id:{},jobName:{}",id,jobName);
                }
            }
        }
        //不管是周期任务还是单次任务，不管是否已经进入任务池  都要去判断并删除缓冲区与redis
        TaskBuffer taskBuffer = taskBufferService.findByTaskSourceId(id);
        String taskBufferId = "";
        if (taskBuffer != null){
            taskBufferId = taskBuffer.getId();
            taskBufferService.deleteById(taskBufferId);
            logger.info("接口删除缓冲区中对应任务,id:{},taskBufferId:{}",id,taskBufferId);

            redisUtils.remove(Constant.TASK_PREFIX+taskBufferId);
            taskPool.deleteData(taskBuffer.getWork_type(),taskBuffer.getType(),taskBuffer.getPriority(),taskBufferId);
        }

        return apiResponse.success("删除成功",null);
    }

    /**
     * 单次性分块延迟队列处理 （新的，可批量）
     * @param taskSourceListIds
     * @param task
     * @throws ParseException
     */
    synchronized private void batchSingleJobChunk(List<Integer> taskSourceListIds, Task task) throws ParseException {
        //TODO 并发时会出现数量超过设置的容量个数，待解决
        ConcurrentFunnel.TypeFunnel typeFunnel = concurrentFunnel.checkTypeFunnel(task.getTaskName());
        //是否达到条件
        if (typeFunnel.checkIsSingle()) {
            typeFunnel.init(); //初始化
        }

        typeFunnel.setCapacity(task.getProduceCapacity());
        typeFunnel.setSeconds(task.getProduceInterval());
        List<Integer> lists = typeFunnel.getLists();
        String expectedTime = "";
        String cron = "";
        if (lists == null || lists.isEmpty()){
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = formatter.parse(typeFunnel.getExpectedTime());
            Date now = DateUtils.getCurrentDateToDate();
            if (date.compareTo(now) < 0){
                date = now;
            }
            date.setTime(date.getTime() + task.getProduceInterval() * 1000L);
            expectedTime = formatter.format(date);
            //更新预计执行时间及jobName
            cron = CronDateUtils.getCron(expectedTime);
        }
        List<List<Integer>> partitions = CommonUtils.averageAssign(taskSourceListIds, task.getProduceConcurrency());
        int i = 1;
        for (List<Integer> partition : partitions) {
            if (partition.size()!=0){
                ConcurrentFunnel.TypeFunnel.TypeFunnelList typeFunnelList = typeFunnel.typeFunnelLists.get(i);
                if (typeFunnelList == null || typeFunnelList.getLists().isEmpty()){
                    jobService.createJob("single_"+task.getTaskName()+ ":" +i+":" + partition.get(0) ,1,cron);
                    typeFunnelList = new ConcurrentFunnel.TypeFunnel.TypeFunnelList();
                    typeFunnelList.setJobName("single_"+task.getTaskName()+ ":" +i+":" + partition.get(0));
                    typeFunnel.setExpectedTime(expectedTime);
                    typeFunnel.typeFunnelLists.put(i,typeFunnelList);
                }
                typeFunnelList.push(partition);
                taskSourceListService.batchUpdateJobNameById(typeFunnelList.getJobName(), typeFunnel.getExpectedTime(), partition,1);
            }
            i++;
        }
    }

    /**
     * 周期性分块延迟队列处理 （新的，可批量）
     * @param taskSourceListIds
     * @param task
     * @throws ParseException
     */
    synchronized public void batchCircleJobChunk(List<Integer> taskSourceListIds, Task task) throws ParseException {
        TaskFunnel taskFunnel = taskFunnelService.findByTaskId(task.getId());
        CronExpression cronExpression = new CronExpression(task.getCronExpression());
        Date date = cronExpression.getNextValidTimeAfter(new Date());
        String expectedTime = DateUtils.getFormatTime(date,"yyyy-MM-dd HH:mm:ss");
        BigInteger time = BigInteger.valueOf(date.getTime());
        if (taskFunnel == null){
            //创建周期性任务,主的
            TaskFunnel newTaskFunnel = new TaskFunnel();
            jobService.createJob(task.getTaskName() + ":1:1",0,task.getCronExpression());
            newTaskFunnel.setTaskId(task.getId());
            newTaskFunnel.setTaskName(task.getTaskName() + ":1:1");
            newTaskFunnel.setIsMain(1);
            newTaskFunnel.setTaskNum(taskSourceListIds.size());

            newTaskFunnel.setNextFireTime(time);
            taskFunnelService.insertTaskFunnel(newTaskFunnel);
            taskSourceListService.batchUpdateJobNameById(task.getTaskName() + ":1:1",expectedTime,taskSourceListIds,0);
        }else {

            String[] split = taskFunnel.getTaskName().split(":");

            if (taskFunnel.getTaskNum()<task.getProduceCapacity()){
                taskSourceListService.batchUpdateJobNameById(taskFunnel.getTaskName(),DateUtils.convertTimestampToDate(taskFunnel.getNextFireTime()),taskSourceListIds,0);
                taskFunnelService.updateTaskNumById(taskFunnel.getTaskNum()+taskSourceListIds.size(),taskFunnel.getId());
            }else {
                if (Integer.parseInt(split[1]) != task.getProduceConcurrency()){
                    int concurrencyNum = Integer.parseInt(split[1]) + 1;

                    String jobName = task.getTaskName() + ":"+concurrencyNum+":"+split[2];
                    if (split[2].equals("1")){
                        TaskFunnel newTaskFunnel = new TaskFunnel();
                        jobService.createJob(jobName,0,task.getCronExpression());
                        newTaskFunnel.setTaskId(task.getId());
                        newTaskFunnel.setTaskName(jobName);
                        newTaskFunnel.setIsMain(1);
                        newTaskFunnel.setTaskNum(taskSourceListIds.size());

                        newTaskFunnel.setNextFireTime(time);
                        taskFunnelService.insertTaskFunnel(newTaskFunnel);
                        taskSourceListService.batchUpdateJobNameById(jobName,expectedTime,taskSourceListIds,0);
                    }else {
                        BigInteger nextFireTime = time.add(BigInteger.valueOf((long) (Integer.parseInt(split[2])-1) *task.getProduceInterval()*1000));
                        expectedTime = DateUtils.convertTimestampToDate(nextFireTime);
                        String cron = CronDateUtils.getCron(expectedTime);
                        TaskFunnel newTaskFunnel = new TaskFunnel();
                        //创建新的周期性任务，子的
                        jobService.createJob(jobName,0,cron);
                        newTaskFunnel.setTaskId(task.getId());
                        newTaskFunnel.setTaskName(jobName);
                        newTaskFunnel.setIsMain(0);
                        newTaskFunnel.setTaskNum(taskSourceListIds.size());

                        newTaskFunnel.setNextFireTime(nextFireTime);
                        taskFunnelService.insertTaskFunnel(newTaskFunnel);
                        taskSourceListService.batchUpdateJobNameById(jobName,expectedTime,taskSourceListIds,0);
                    }
                }else {
                    int suffix = Integer.parseInt(split[2]) + 1;
                    String jobName = split[0]+":1:" + suffix;
                    //创建新的周期性任务，子的
                    BigInteger nextFireTime = time.add(BigInteger.valueOf((long) (suffix-1) *task.getProduceInterval()*1000));
                    expectedTime = DateUtils.convertTimestampToDate(nextFireTime);
                    String cron = CronDateUtils.getCron(expectedTime);
                    TaskFunnel newTaskFunnel = new TaskFunnel();
                    //创建新的周期性任务，子的
                    jobService.createJob(jobName,0,cron);
                    newTaskFunnel.setTaskId(task.getId());
                    newTaskFunnel.setTaskName(jobName);
                    newTaskFunnel.setIsMain(0);
                    newTaskFunnel.setTaskNum(taskSourceListIds.size());

                    newTaskFunnel.setNextFireTime(nextFireTime);
                    taskFunnelService.insertTaskFunnel(newTaskFunnel);
                    taskSourceListService.batchUpdateJobNameById(jobName,expectedTime,taskSourceListIds,0);
                }
            }
        }
    }

    /**
     * 单次任务验证任务缓冲区是否还有记录，有则重试次数上限+1
     * @param taskSource
     */
    public void checkTaskBuffer(TaskSourceList taskSource){
        TaskBuffer taskBuffer = taskBufferService.findByTaskSourceId(CacheKey.SINGLE + "_" + taskSource.getId());
        if (taskBuffer != null){
            if (taskBuffer.getRetry_times() == 0 && taskBuffer.getIs_in_pool() == 2){
                taskBufferService.offerQueue(taskBuffer.getPriority());
                taskBufferService.updateRetryTimesAndIsInPoolById(1,0,taskBuffer.getId());  //更新为入缓冲区状态
            }else {
                taskBufferService.updateRetryTimesById(taskBuffer.getRetry_times()+1,taskBuffer.getId());
            }
        }
    }
}
