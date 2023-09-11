package com.sailvan.dispatchcenter.core.service;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.sailvan.dispatchcenter.common.cache.InitAccountCache;
import com.sailvan.dispatchcenter.common.cache.InitMachineCache;
import com.sailvan.dispatchcenter.common.cache.InitValidVersionCache;
import com.sailvan.dispatchcenter.common.config.FtpConfig;
import com.sailvan.dispatchcenter.common.config.FtpConfigInner;
import com.sailvan.dispatchcenter.common.constant.*;
import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.pipe.*;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.common.util.*;
import com.sailvan.dispatchcenter.core.async.AsyncPushTask;
import com.sailvan.dispatchcenter.core.domain.TaskBuffer;
import com.sailvan.dispatchcenter.core.log.ClientActionLogsPrintUtil;
import com.sailvan.dispatchcenter.core.log.ClientRequestLogsPrintUtil;
import com.sailvan.dispatchcenter.core.log.ClientJobLogsPrintUtil;
import com.sailvan.dispatchcenter.core.log.MachineHeartbeatLogsPrintUtils;
import com.sailvan.dispatchcenter.common.cache.ProxyIPPool;
import com.sailvan.dispatchcenter.core.pool.TaskPool;
import com.github.pagehelper.util.StringUtil;
import com.sailvan.dispatchcenter.core.util.BingTopDaMa;
import com.sailvan.dispatchcenter.db.service.ProxyRequestLogsService;
import com.sailvan.dispatchcenter.db.service.ProxyTrendService;
import lombok.SneakyThrows;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sailvan.dispatchcenter.common.constant.Constant.*;


/**
 * @author menghui
 * @date 21-04
 */
@Service
public class ClientService {
    private static Logger logger = LoggerFactory.getLogger(ClientService.class);

    @Autowired
    MachineService machineService;

    @Autowired
    ProxyIpService proxyIpService;

    @Autowired
    TaskPool taskPool;

    @Autowired
    TaskSourceListService taskSourceListService;

    @Autowired
    TaskService taskService;

    @Autowired
    TaskResultService taskResultService;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    VersionService versionService;

    @Autowired
    TaskLogsService taskLogsService;

    @Autowired
    InitValidVersionCache initValidVersionCache;

    @Autowired
    InitMachineCache initMachineCache;

    @Autowired
    InitAccountCache initAccountCache;

    @Autowired
    StoreAccountService storeAccountService;

    @Autowired
    StoreAccountSitesService storeAccountSitesService;

    @Autowired
    AsyncPushTask asyncPushTask;

    @Autowired
    MachineHeartbeatLogsService machineHeartbeatLogsService;

    @Autowired
    TaskBufferService taskBufferService;

    @Autowired
    FtpConfig ftpConfig;

    @Autowired
    FtpConfigInner ftpConfigInner;

    @Autowired
    TaskUtil taskUtil;

    @Autowired
    TaskResultIndexRangeService taskResultIndexRangeService;


    @Autowired
    MachineHeartbeatLogsPrintUtils printUtils;

    @Autowired
    ProxyIPPool proxyIPPool;

    @Autowired
    ProxyIpPlatformService proxyIpPlatformService;

    @Autowired
    ProxyRequestLogsService proxyRequestLogsService;

    @Autowired
    ClientRequestLogsPrintUtil clientHttpRequestLogsPrintUtil;

    @Autowired
    ProxyTrendService proxyTrendService;

    @Autowired
    ClientActionLogsPrintUtil clientActionLogsPrintUtil;

    @Autowired
    ParseCaptcha parseCaptcha;

    @Autowired
    ClientJobLogsPrintUtil clientJobLogsPrintUtil;

    /**
     * 注册客户端IP(激活更新token)
     *
     * @param jsonObject
     * @return
     */
    public ApiResponseDomain registerClient(JSONObject jsonObject) {
        ApiResponse apiResponse = new ApiResponse();
        String uniqueId = String.valueOf(jsonObject.get("unique_id"));
        logger.info("register unique_id:" + uniqueId + " jsonObject:" + jsonObject.toString());
        if (StringUtils.isEmpty(uniqueId.trim())) {
            return apiResponse.error(ResponseCode.REGISTER_PARAM_ERROR_CODE, "empty", null);
        }
        JSONObject content = new JSONObject();
        String ip = "";
        String mac = "";
        if (uniqueId.contains("-")) {
            ip = uniqueId.split("-")[0];
            try {
                mac = uniqueId.split("-")[1];
            } catch (Exception e) {
                logger.info("registerClient ip:{}, uniqueId:{}, mac:{} is empty", ip, uniqueId, mac);
                return apiResponse.error(ResponseCode.CAN_NOT_REGISTERED_CODE, "请求mac是空值,请检查是否有问题", null);
            }
        } else {
            ip = uniqueId;
            logger.info("registerClient ip:{}, uniqueId:{}, mac:{} is empty", ip, uniqueId, mac);
            return apiResponse.error(ResponseCode.CAN_NOT_REGISTERED_CODE, "请求mac存在空值,请检查是否有问题", null);
        }
        Machine select = initMachineCache.getMachineCacheMapCacheByIp(ip);
        if (select != null) {
//            if (select.getStatus() == STATUS_INVALID) {
//                logger.info("registerClient ip:{}  machine status:{}",ip, select.getStatus());
//                return apiResponse.error(ResponseCode.CAN_NOT_REGISTERED_CODE, "当前机器是无效状态不能注册,请联系管理员开启使用", null);
//            }
            if (StringUtils.isEmpty(mac) || StringUtils.isEmpty(select.getMac())) {
                logger.info("registerClient ip:{}, mac:{}, mysql_mac:{} is empty", ip, mac, select.getMac());
                return apiResponse.error(ResponseCode.CAN_NOT_REGISTERED_CODE, "当前机器mac或请求mac存在空值,请检查是否有问题", null);
            } else if (!select.getMac().equals(mac)) {
                logger.info("registerClient ip:{}, mac:{}, mysql_mac:{} not equal", ip, mac, select.getMac());
                return apiResponse.error(ResponseCode.CAN_NOT_REGISTERED_CODE, "当前机器mac地址不相同,请检查是否有问题", null);
            }


            String authorization = null;
            try {
                authorization = AesUtils.encrypt(ip, C_KEY, IV_KEY, CIPHER_VALUE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Machine machine = new Machine();
            BeanUtils.copyProperties(select, machine);
            if (StringUtils.isEmpty(machine.getToken()) || !machine.getToken().equals(authorization)) {
                machine.setToken(authorization);
                machineService.update(machine);
                initMachineCache.updateMachineCacheMapCacheByIp(ip, machine);
            }
            content.put("access_token", authorization);
            return apiResponse.success("success", content);
        }
        return apiResponse.error(ResponseCode.REGISTER_DB_EMPTY_ERROR_CODE, "数据库不存在注册的机器IP,清联系管理员添加", content);
    }


    public ApiResponseDomain getCrawlPlatformProxy(JSONObject jsonObjectParam, String ip) {
        String workType = String.valueOf(jsonObjectParam.get("work_type"));
        String type = String.valueOf(jsonObjectParam.get("type"));
        JSONObject content = new JSONObject();
        ApiResponse apiResponse = new ApiResponse();
        if (StringUtils.isEmpty(workType)) {
            return apiResponse.error(ResponseCode.ERROR_CODE, "workType参数有问题是空的", null);
        }

        String formatPlatform = CommonUtils.getFormatPlatform(workType);
        if (StringUtils.isEmpty(formatPlatform)) {
            return apiResponse.error(ResponseCode.ERROR_CODE, "workType参数有问题,请检查是否已经配置", null);
        }
        Machine select = initMachineCache.getMachineCacheMapCacheByIp(ip);
        if (select != null) {
            if (select.getMachineType() != MACHINE_TYPE_INTRANET_VPS) {
                // 不是内网VPS
                List<MachineTaskType> machineTaskTypeList = select.getMachineTaskTypeList();
                if (machineTaskTypeList != null) {
                    for (MachineTaskType machineTaskType : machineTaskTypeList) {
                        if (machineTaskType.getPlatformType() == LARGE_TASK_TYPE_CRAWL_PLATFORM) {
                            continue;
                        }
                        if (machineTaskType.getPlatform().equals(formatPlatform)) {
                            return apiResponse.error(ResponseCode.ERROR_CODE, "此平台:" + formatPlatform + "是账号平台,不能获取对应的爬取代理IP", content);
                        }
                    }
                }
            }
        }

        String proxy = "";
        String port = "";
        int proxyId = proxyIPPool.popQueue(formatPlatform);
        if (proxyId != 0){
            ProxyIp proxyIp = proxyIpService.findProxyIpById(proxyId);
            proxy = proxyIp.getIp();
            port = String.valueOf(proxyIp.getPort());

            ProxyIpPlatform proxyIpPlatform = new ProxyIpPlatform();
            proxyIpPlatform.setPlatform(formatPlatform);
            proxyIpPlatform.setProxyIpId(proxyIp.getId());
            //上次使用时间
            proxyIpPlatform.setLastUsedTimestamp(BigInteger.valueOf(System.currentTimeMillis()));

            proxyIpPlatformService.updateProxyIpPlatformLastUsedTimestamp(proxyIpPlatform);
        }
        content.put("proxy", proxy);
        content.put("port", port);
        logger.info("获取代理IP-getProxy-机器ip:{}，爬取平台:{},代理IP{}:{}", ip, formatPlatform, proxy, port);

        return apiResponse.success("success", content);
    }

    @Deprecated
    public ApiResponseDomain disableProxyIp(JSONObject jsonObjectParam, String ip) {
        String proxy = String.valueOf(jsonObjectParam.get("proxy"));
        String port = String.valueOf(jsonObjectParam.get("port"));
        String platform = String.valueOf(jsonObjectParam.get("work_type"));
        ApiResponse apiResponse = new ApiResponse();
        String formatPlatform = CommonUtils.getFormatPlatform(platform);
        if (StringUtils.isEmpty(formatPlatform)) {
            return apiResponse.error(ResponseCode.ERROR_CODE, "platform参数有问题,请检查是否已经配置", null);
        }

        ProxyIp proxyIp = proxyIpService.getProxyIpByUniqueKey(proxy, Integer.parseInt(port));
        if (proxyIp != null){
            proxyIPPool.updateLastBannedTime(formatPlatform,proxyIp.getId());

            ProxyIpPlatform disableIp = new ProxyIpPlatform();
            disableIp.setPlatform(formatPlatform);
            disableIp.setProxyIpId(proxyIp.getId());
            //2禁用状态
            disableIp.setStatus(STATUS_INVALID);
            disableIp.setBanPeriod(DateUtils.getCurrentDate());

            proxyIpPlatformService.update(disableIp);
        }


        logger.info("禁用代理IP-bannedProxy-机器ip:{}，爬取平台：platform{}，proxy:{}:{}", ip, formatPlatform,proxy,port);
        return apiResponse.success("success", "");
    }


    /**
     * 移除代理Ip
     * @param jsonObjectParam
     * @return
     */
    public ApiResponseDomain removeProxyIp(JSONObject jsonObjectParam) {
        String proxy = String.valueOf(jsonObjectParam.get("proxy"));
        String port = String.valueOf(jsonObjectParam.get("port"));
        String platform = String.valueOf(jsonObjectParam.get("work_type"));
        ApiResponse apiResponse = new ApiResponse();
        String formatPlatform = CommonUtils.getFormatPlatform(platform);
        if (StringUtils.isEmpty(formatPlatform)) {
            return apiResponse.error(ResponseCode.ERROR_CODE, "platform参数有问题,请检查是否已经配置", null);
        }
        ProxyIp proxyIp = proxyIpService.getProxyIpByUniqueKey(proxy, Integer.parseInt(port));
        if (proxyIp != null){
            proxyIpService.updateProxyStatus(-1,proxyIp.getId());
            proxyIPPool.removeProxy(proxyIp.getId());
        }
        return apiResponse.success("success", "");
    }


    /**
     * 心跳
     *
     * @param ip
     * @param token
     * @param jsonObject
     * @return
     */
    public ApiResponseDomain heartBeat(JSONObject jsonObject, String ip, String token) {
        String clientVersion = String.valueOf(jsonObject.get("client_version"));
        String clientFileVersion = String.valueOf(jsonObject.get("client_file_version"));
        String number = "2";
        if(jsonObject.containsKey("number")){
            number = String.valueOf(jsonObject.get("number"));
        }
        ApiResponse apiResponse = new ApiResponse();
        Machine machine = new Machine();
        machine.setIp(ip);
        machine.setToken(token);
        // 此处加个缓存，避免每次查询数据库
        Machine select = initMachineCache.getMachineCacheMapCacheByIp(ip);
        JSONObject selectJson = new JSONObject();
        if (select != null) {
            content(select, selectJson, number);
            heatBeatUpdate(select, jsonObject);
            updateClientVersion(selectJson, clientVersion, clientFileVersion, ip, select);
        }
        if(select.getStatus() == Constant.STATUS_INVALID){
            JSONObject selectJsonInvalid = new JSONObject();
            selectJsonInvalid.putAll(selectJson);
            selectJsonInvalid.put("worker", null);
            return apiResponse.success("success", selectJsonInvalid);
        }
        return apiResponse.success("success", selectJson);
    }


    public TaskResult updateOrCreateResult(JSONObject jsonObject, LinkedHashMap returnParamsMap, LinkedHashMap centerParamsMap, TaskSourceList taskSource, TaskBuffer taskBuffer, String ip, int runMode) {
        TaskResult taskResult = new TaskResult();

        Integer code = jsonObject.getInteger("code");
        String result = null;
        Object content = jsonObject.get("content");
        if (content instanceof Boolean) {
            if ((Boolean) content == true) {
                result = "true";
            }
        } else {
            result = String.valueOf(content);
        }
        String msg = jsonObject.getString("msg");
        String error = jsonObject.getString("error");
        String refreshTime = taskBuffer.getRefresh_time();
        String resultType = jsonObject.getString("result_type");
        Integer needRetry = jsonObject.getInteger("need_retry");
        String errorLevel = jsonObject.getString("error_level");

        String centerParams = null;

        if (returnParamsMap.containsKey("start_date")) {
            centerParamsMap.put("start_date", returnParamsMap.get("start_date"));
        }
        if (returnParamsMap.containsKey("end_date")) {
            centerParamsMap.put("end_date", returnParamsMap.get("end_date"));
        }
        if (returnParamsMap.containsKey("filename")) {
            centerParamsMap.put("filename", returnParamsMap.get("filename"));
        }
        if (!centerParamsMap.isEmpty()) {
            centerParams = JSONUtils.toJSONString(centerParamsMap);
        }
        int resultHashKey = taskBuffer.getResult_hash_key();
        int smallestId = taskUtil.getTaskResultSearchSmallestId();

        TaskResult taskResultInfo = taskResultService.getTaskResultByTaskSourceIdAndResultHashKeyAndRefreshTime(smallestId, taskBuffer.getTask_source_id(), resultHashKey, refreshTime);

        int retryTimes = 1;
        if (taskResultInfo != null) {
            retryTimes = taskResultInfo.getRetryTimes() + 1;
        }
        LinkedHashMap clientParamsMap = JSON.parseObject(taskSource.getParams(), LinkedHashMap.class, Feature.OrderedField);
        if (clientParamsMap.containsKey("account") && clientParamsMap.containsKey("site")) {
            String account = String.valueOf(clientParamsMap.get("account"));
            String site = String.valueOf(clientParamsMap.get("site"));
            taskResult.setAccount(account);
            taskResult.setSite(site);
        }
        taskResult.setRunMode(runMode);
        taskResult.setWorkType(taskSource.getWorkType());
        taskResult.setIp(ip);
        taskResult.setClientCode(code);
        taskResult.setTaskType(taskBuffer.getType());
        taskResult.setTaskSourceId(taskBuffer.getTask_source_id());
        taskResult.setTaskBufferId(String.valueOf(taskBuffer.getId()));
        taskResult.setUniqueId(taskBuffer.getUnique_id());
        taskResult.setResultHashKey(resultHashKey);
        taskResult.setResultType(resultType);
        if (needRetry != null) {
            taskResult.setNeedRetry(needRetry);
        }
        taskResult.setErrorLevel(errorLevel);
        taskResult.setClientResult(result);
        taskResult.setRefreshTime(refreshTime);
        taskResult.setCenterParams(centerParams);
        taskResult.setReturnParams(taskSource.getReturnParams());
        taskResult.setClientMsg(msg);
        taskResult.setClientError(error);
        taskResult.setRetryTimes(retryTimes);
        taskResult.setCreatedTime(DateUtils.getCurrentDate());

        if (taskResultInfo == null) {
            int resultId = taskUtil.getNextTaskResultId();
            taskResult.setId(resultId);
            if (resultId % CacheKey.SINGLE_TABLE_CAPACITY == 0 || resultId % CacheKey.SINGLE_TABLE_CAPACITY == 1) {
                TaskResultIndexRange taskResultIndexRange = new TaskResultIndexRange();
                taskResultIndexRange.setIndex(resultId);
                taskResultIndexRange.setDate(DateUtils.getDate());
                taskResultIndexRangeService.insertTaskResultIndexRange(taskResultIndexRange);
            }
            taskResultService.insertTaskResult(taskResult);
        } else {
            taskResult.setId(taskResultInfo.getId());
            if (taskBuffer.getIs_enforced() == 1) {
                taskResultService.updateTaskResult(taskResult);
            }
            if (taskBuffer.getIs_enforced() == 0 && taskResultInfo.getClientCode() != ResponseCode.SUCCESS_CODE) {
                taskResultService.updateTaskResult(taskResult);
            }
        }
        return taskResult;
    }

    /**
     * 客户端结果返回处理逻辑
     *
     * @param jsonObject
     * @return
     */
    @SneakyThrows
    public ApiResponseDomain taskResult(JSONObject jsonObject, String ip) {
        ApiResponse apiResponse = new ApiResponse();
        LinkedHashMap returnParamsMap = JSON.parseObject(jsonObject.getString("return_params"), LinkedHashMap.class, Feature.OrderedField);
        if (!returnParamsMap.containsKey("task_buffer_id")) {
            return apiResponse.error(ResponseCode.ERROR_CODE, "请求参数中task_buffer_id不存在", null);
        }

        String taskBufferId = String.valueOf(returnParamsMap.get("task_buffer_id"));

        synchronized (taskBufferId.intern()) {
            TaskBuffer taskBuffer = taskBufferService.findById(taskBufferId);
            if (taskBuffer == null) {
                return apiResponse.error(ResponseCode.ERROR_CODE, "任务缓冲区Id不存在 taskBufferId:" + taskBufferId, null);
            }
            String taskSourceId = taskBuffer.getTask_source_id();
            String[] s = taskSourceId.split("_");
            int isSingle;
            if (s[0].equals(CacheKey.CIRCLE)) {
                isSingle = 0;
            } else {
                isSingle = 1;
            }
            TaskSourceList taskSource = taskSourceListService.findTaskSourceById(Integer.parseInt(s[1]), isSingle);
            if (taskSource == null) {
                return apiResponse.error(ResponseCode.ERROR_CODE, "任务库Id不存在 taskSourceId:" + taskSourceId, null);
            }

            LinkedHashMap centerParamsMap = new LinkedHashMap();

            TaskResult taskResult = updateOrCreateResult(jsonObject, returnParamsMap, centerParamsMap, taskSource, taskBuffer, ip, MACHINE);

            String taskState = "";
            if (taskResult.getClientCode() == ResponseCode.SUCCESS_CODE) {
                taskState = TaskStateKey.TASK_STATE_SUCCESS;
            } else {
                taskState = TaskStateKey.TASK_STATE_FAIL;
            }
            taskSourceListService.updateLastResultTimeById(Integer.parseInt(s[1]), DateUtils.getCurrentDate(), isSingle, taskState);

            //记录流水
            taskLogsService.addTaskEvent(taskResult, taskBuffer, ip, MACHINE);
            updateMachineLastExecute(ip, taskSource);

            redisUtils.remove(Constant.TASK_WAIT_RESULT, taskBuffer.getId() + "#" + ip);
            logger.info("客户端返回结果并删除redis等待任务缓存，任务缓冲区ID:{},任务库ID:{}", taskBuffer.getId(), taskBuffer.getTask_source_id());

            updateAccountSiteStatus(jsonObject, taskSource);

            //任务执行失败，任务重新回收入池，执行成功，删除任务池缓冲区记录
            if (taskResult.getClientCode() != ResponseCode.SUCCESS_CODE) {
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
            }
            //将结果分发不同的业务系统
            String[] split = taskSource.getSystemId().split(",");
            for (String systemId : split) {
                asyncPushTask.sendSystem(taskResult, systemId, CacheKey.BEGIN_DELAY);
            }
        }

        return apiResponse.success("success", null);
    }

    private void updateMachineLastExecute(String ip, TaskSourceList taskSourceList) {
        try {
            if (StringUtils.isEmpty(ip)) {
                return;
            }
            if (StringUtils.isEmpty(taskSourceList)) {
                logger.error("updateMachineLastExecute taskSourceList is null");
                return;
            }
            Task taskByUniqueId = taskService.getTaskByUniqueId(taskSourceList.getTaskId());
            if (StringUtils.isEmpty(taskByUniqueId)) {
                return;
            }
            Machine machine = new Machine();
            machine.setIp(ip);
            machine.setLastExecuteWorkType(taskSourceList.getWorkType());
            machine.setLastExecuteTask(taskByUniqueId.getTaskName());
            machine.setLastExecuteTime(DateUtils.getAfterDays(0));
            machineService.updateLastWorkTaskByIp(machine);
            logger.info("updateMachineLastExecute ip:{}, lastExecuteWorkType:{} ,lastExecuteTask:{}, lastExecuteTime:{}", machine.getIp(), machine.getLastExecuteWorkType(), machine.getLastExecuteTask(), machine.getLastExecuteTime());
        } catch (Exception e) {
            logger.error("updateMachineLastExecute error:{}", e.getMessage());
        }

    }

    public void updateAccountSiteStatus(JSONObject jsonObject, TaskSourceList taskSource) {
        String resultType = jsonObject.getString("result_type");
        Integer needRetry = jsonObject.getInteger("need_retry");
        String type = jsonObject.getString("type");
        Integer code = jsonObject.getInteger("code");
        if (!StringUtils.isEmpty(resultType) && "account_status".equals(resultType) && needRetry == 0) {
            // 这里更新不正常code
            resetAccountSiteStatus(jsonObject, taskSource, code, true);
            logger.info("resetAccountSiteStatus update code...jsonObject:{}, taskSourceObject:{}, code:{}, isUpdateStatus{}",jsonObject,JSONObject.toJSONString(taskSource),code,true);
        } else if (!StringUtils.isEmpty(resultType) && "account_status".equals(resultType) && needRetry == 1) {
            // 不更新状态值
            resetAccountSiteStatus(jsonObject, taskSource, code, false);
            logger.info("resetAccountSiteStatus update no...jsonObject:{}, taskSourceObject:{}, code:{}, isUpdateStatus{}",jsonObject,JSONObject.toJSONString(taskSource),code,false);
        }
        if (!StringUtils.isEmpty(type) && "account_status".equals(type) && code == ResponseCode.SUCCESS_CODE) {
            // 重置为正常
            resetAccountSiteStatus(jsonObject, taskSource, code, true);
            logger.info("resetAccountSiteStatus reset...jsonObject:{}, taskSourceObject:{}, code:{}, isUpdateStatus{}",jsonObject,JSONObject.toJSONString(taskSource),code,true);
        }

    }

    private void resetAccountSiteStatus(JSONObject jsonObject, TaskSourceList taskSource, int code, boolean isUpdateStatus) {
        String msg = jsonObject.getString("msg");
        String error = jsonObject.getString("error");
        String errorLevel = jsonObject.getString("error_level");
        JSONObject object = JSONObject.parseObject(taskSource.getParams());
        StoreAccountSites storeAccountSiteSearch = new StoreAccountSites();
        storeAccountSiteSearch.setAccount(object.getString("account"));
        List<StoreAccountSites> storeAccountSitesByAccountSite = new ArrayList<>();
        if ("site".equals(errorLevel)) {
            storeAccountSiteSearch.setSite(object.getString("site"));
            storeAccountSitesByAccountSite = storeAccountSitesService.getStoreAccountSitesByAccountSite(storeAccountSiteSearch);
        } else if ("continent".equals(errorLevel)) {
            String continent = SITE_CONTINENT_MAP.get(object.getString("site"));
            storeAccountSiteSearch.setContinents(continent);
            storeAccountSitesByAccountSite = storeAccountSitesService.getStoreAccountSitesByAccountContinents(storeAccountSiteSearch);
        }
        if (code == ResponseCode.SUCCESS_CODE) {
            String continent = SITE_CONTINENT_MAP.get(object.getString("site"));
            storeAccountSiteSearch.setContinents(continent);
            storeAccountSitesByAccountSite = storeAccountSitesService.getStoreAccountSitesByAccountContinents(storeAccountSiteSearch);
        }
        for (StoreAccountSites accountSites : storeAccountSitesByAccountSite) {
            accountSites.setClientError(error);
            accountSites.setClientMsg(msg);
            accountSites.setStatusMachine(code);
            int beforeStatus = accountSites.getStatus();
            if (isUpdateStatus) {
                if (code == ResponseCode.SUCCESS_CODE) {
                    accountSites.setStatus(STATUS_VALID);
                } else if (initAccountCache.isInValidAccountErrorCode(code)) {
                    accountSites.setStatus(STATUS_INVALID);
                }
            }
            storeAccountSitesService.updateStoreAccountSitesByClient(accountSites);
            logger.info("resetAccountSiteStatus updateStoreAccountSitesByClient accountSites:{}, statusMachine:{},isUpdateStatus:{}, status:{}, beforeStatus:{}",JSONObject.toJSONString(accountSites),accountSites.getStatusMachine(),isUpdateStatus,accountSites.getStatus(),beforeStatus);
            String key = accountSites.getAccount() + "_" + accountSites.getSite();
            initAccountCache.updateAccountSiteStatusCache(key, accountSites);
            logger.info("resetAccountSiteStatus updateStoreAccountSitesByClient redis success accountSites:{}, key:{}",JSONObject.toJSONString(accountSites),key);
        }
    }


    /**
     * 获取任务
     *
     * @param object
     * @param ip
     * @return
     */
    public ApiResponseDomain getTask(JSONObject object, String ip) {
        ApiResponse apiResponse = new ApiResponse();
        String workType = object.getString("work_type");
        if (StringUtil.isEmpty(workType)) {
            return apiResponse.error(ResponseCode.ERROR_CODE, "参数workType不能为空", null);
        }
        String types = object.getString("types");
        if (StringUtil.isEmpty(types)) {
            return apiResponse.error(ResponseCode.ERROR_CODE, "参数types不能为空", null);
        }

        int isInvalid = 0;
        String[] split = types.split(",");
        List<String> typeLists = Arrays.asList(types.split(","));
        List<Object> lists = new ArrayList<>();

        Integer num = TASK_POP_NUM_MAP.get(split[0]);
        if (num == null || num == 0) {
            num = 1;
        }
        for (int i = 0; i < num; i++) {
            Object pop = taskPool.pop(workType, typeLists);
            if (pop != null) {
                lists.add(pop);
            }
        }

        LinkedList list = new LinkedList();
        if (!lists.isEmpty()) {
            for (Object item : lists) {
                String taskBufferId = String.valueOf(item);
                if (taskBufferId != null) {
                    synchronized (taskBufferId.intern()) {
                        //取出第一个
                        Object data = redisUtils.get(TASK_PREFIX + taskBufferId);
                        if (!StringUtils.isEmpty(data)) {
                            String jsonString = String.valueOf(data);
                            JSONObject jsonObject = JSONObject.parseObject(jsonString, Feature.OrderedField);
                            TaskMetadata metadata = JSONObject.toJavaObject(jsonObject, TaskMetadata.class);
                            TaskBuffer taskBuffer = taskBufferService.findById(taskBufferId);
                            if (taskBuffer != null){
                                //TODO 添加组合任务拆分为子任务

                                taskBufferService.updateIsInPoolById(2, taskBuffer.getId());
                                taskLogsService.addTaskLogs(taskBuffer, Event.OUT_POOL, ip, MACHINE);
                                redisUtils.remove(Constant.TASK_PREFIX + taskBufferId);
                                logger.info("客户端获取任务并删除redis任务缓存,任务库ID:{},任务缓冲区ID:{}", taskBuffer.getTask_source_id(), taskBufferId);
                                jsonObject.remove("retry_times");
                                LinkedHashMap centerParams = metadata.getCenter_params();

                                // 客戶端 获取任务时 过滤掉特殊任务及无效的账号站点
                                if (centerParams != null && centerParams.containsKey("account") && centerParams.containsKey("site")) {
                                    Map<Integer, String> codeMean = new HashMap<>();
                                    if (initAccountCache.isInvalidByAccountSite(String.valueOf(metadata.getType()), String.valueOf(centerParams.get("account")), String.valueOf(centerParams.get("site")), codeMean)) {
                                        if (!codeMean.isEmpty()) {
                                            for (Map.Entry<Integer, String> integerStringEntry : codeMean.entrySet()) {
                                                logger.error("get_job error: ip--{},大类型--{},小类型--{},任务内容---{},center_params:{}, isInvalid:{}, code:{}, codeMean:{}", ip, workType, types, list, centerParams, isInvalid, integerStringEntry.getKey(), integerStringEntry.getValue());
                                            }
                                        } else {
                                            logger.error("get_job error: ip--{},大类型--{},小类型--{},任务内容---{},center_params:{}, isInvalid:{}", ip, workType, types, list, centerParams, isInvalid);
                                        }
                                        isInvalid++;
                                        continue;
                                    }
                                }

                                list.add(jsonObject);

                                //任务等待结果
                                redisUtils.add(Constant.TASK_WAIT_RESULT, taskBuffer.getId() + "#" + ip, System.currentTimeMillis());
                            }

                        }
                    }
                }
            }
        }

        if (isInvalid == split.length) {
            logger.error("get_job: ip--{},大类型--{},小类型--{},任务内容---{},isInvalid:{}", ip, workType, types, list, isInvalid);
            return apiResponse.error(ResponseCode.ERROR_CODE, "店铺状态异常", null);
        }
        logger.info("get_job: ip--{},大类型--{},小类型--{},任务内容---{}", ip, workType, types, list);
        return apiResponse.success("success", list);
    }


    /**
     * 获取图片验证码接口
     *
     * @param jsonObjectParam
     * @param ip
     * @return
     */
    public ApiResponseDomain captchaCode(JSONObject jsonObjectParam, String ip) {

        ApiResponse apiResponse = new ApiResponse();
        String account = String.valueOf(jsonObjectParam.get("account"));
        String imageBase64 = String.valueOf(jsonObjectParam.get("image_base64"));
        String code = String.valueOf(jsonObjectParam.get("code"));
        JSONObject content = new JSONObject();
        try {
            String captchaCode = parseCaptcha.getCode(imageBase64);
//            String captchaCode = BingTopDaMa.getCode(imageBase64);
            content.put("code", captchaCode);
            logger.info("图片验证码接口 ip:{}, captcha_code account:{}, imageBase64:{}, code:{}, captchaCode:{}", ip, account, imageBase64, code, captchaCode);
        } catch (Exception e) {
            logger.info("图片验证码接口 ip:{}, captcha_code error account:{}, imageBase64:{}, code:{}, captchaCode:{}, message:{}", ip, account, imageBase64, code, null, e.getMessage());
            return apiResponse.error(ResponseCode.ERROR_CODE, e.getMessage(), content);
        }

        return apiResponse.success("success", content);
    }


    /**
     * 获取google认证验证码接口
     *
     * @param jsonObjectParam
     * @param ip
     * @return
     */
    public ApiResponseDomain accountVerifyCode(JSONObject jsonObjectParam, String ip) {
        ApiResponse apiResponse = new ApiResponse();
        String account = String.valueOf(jsonObjectParam.get("account"));
        String site = String.valueOf(jsonObjectParam.get("site"));
        String code = String.valueOf(jsonObjectParam.get("code"));
        StoreAccount storeAccountCodeByAccountSite = storeAccountService.getStoreAccountByAccount(account, site);
        JSONObject content = new JSONObject();
        if (storeAccountCodeByAccountSite == null) {
            content.put("code", null);
            logger.info("二步验证接口 account_verify_code error ip:{}, account:{}, site:{}, code{}", ip, account, site, null);
            return apiResponse.error(ResponseCode.ERROR_CODE, "storeAccount is null", content);
        }
        String qrContent = storeAccountCodeByAccountSite.getQrContent();
        Map<String, Object> codes = new HashMap<>();
        try {
            codes = GoogleAuthenticators.getToTpCode(qrContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (codes == null || codes.isEmpty()) {
            content.put("code", null);
            content.put("time", 0);
        } else {
            content.put("code", codes.get("code"));
            content.put("time", codes.get("time"));
        }

        logger.info("二步验证接口 account_verify_code ip:{}, account:{}, site:{}, code{}", ip, account, site, codes);
        return apiResponse.success("success", content);
    }


    /**
     * 构建content内容
     */
    private void content(Machine select, JSONObject selectJson, String number) {
        try {
            if (clientCheckScreen(DateUtils.getAfterDays(0).substring(5), select.getIp())) {
                selectJson.put("check_screen", true);
            } else {
                selectJson.put("check_screen", false);
            }
        } catch (Exception e) {
            logger.error("clientCheckScreen error:{}", e.getMessage());
            selectJson.put("check_screen", false);
        }
        if (select.getMachineType() == MACHINE_TYPE_INTRANET_VPS || select.getMachineType() == Constant.MACHINE_TYPE_EXTRANET_VPS || select.getMachineType() == Constant.MACHINE_TYPE_CHONGQING_VPS) {
            //VPS机器 需要拨号机账号和密码
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("reset_dial", 1);
            jsonObject1.put("dial_account", select.getDialUsername());
            jsonObject1.put("dial_password", select.getDialPassword());
            selectJson.put("dial", jsonObject1);
        }
        if (select.getStatus() == STATUS_INVALID) {
            selectJson.put("work_types", null);
            selectJson.put("types", null);
            selectJson.put("worker", null);
            selectJson.put("ip", select.getIp());
            selectJson.put("machine_type", select.getMachineType());
            logger.info(" heartBeat 机器无效 ip:{}", select.getIp());
            return;
        }

        List<MachineWorkType> machineWorkTypeList = select.getMachineWorkTypeList();
        if (machineWorkTypeList == null || machineWorkTypeList.isEmpty()) {
            return;
        }
        JSONArray jsonArray = new JSONArray();
        String workTypes = "";
        String types = "";
        for (MachineWorkType machineWorkType : machineWorkTypeList) {
            if (machineWorkType.getStatus() == STATUS_INVALID) {
                continue;
            }
            // 0账号平台
            if (!StringUtils.isEmpty(workTypes)) {
                workTypes += ",";
            }
            if (!StringUtils.isEmpty(types)) {
                types += ",";
            }
            if (!StringUtils.isEmpty(machineWorkType.getWorkTypeTaskName())) {
                types += machineWorkType.getWorkTypeTaskName();
            }
            if (machineWorkType.getPlatformType() == LARGE_TASK_TYPE_ACCOUNT_PLATFORM) {
                workTypes += machineWorkType.getAccount() + "_" + machineWorkType.getContinents();
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("worker_type", machineWorkType.getAccount() + "_" + machineWorkType.getContinents());
                jsonObject1.put("types", machineWorkType.getWorkTypeTaskName());
                jsonObject1.put("is_browser", machineWorkType.getIsBrowser());
                jsonObject1.put("is_need_rand_local", 0);
                jsonObject1.put("is_same_user_agent", 1);
                jsonObject1.put("is_use_proxy", 0);
                //账号平台 代理IP 放入缓存中
                String key = PROXY_IP_ACCOUNT_CONTINENTS_PREFIX + machineWorkType.getAccount() + "_" + machineWorkType.getContinents();
                Object redisValue = redisUtils.get(key);
                if (redisValue == null) {
                    StoreAccount proxyIp = (StoreAccount) proxyIpService.getProxyIp(machineWorkType.getAccount(), machineWorkType.getContinents(), "");
                    jsonObject1.put("proxy", proxyIp.getProxyIp());
                    jsonObject1.put("port", proxyIp.getProxyIpPort());
                    jsonArray.add(jsonObject1);
                    redisUtils.put(key, proxyIp.getProxyIp() + ":" + proxyIp.getProxyIpPort(), Long.valueOf(3600 * 24));
                } else {
                    String s = String.valueOf(redisValue);
                    if (s.split(":").length == 0) {
                        jsonObject1.put("proxy", "");
                        jsonObject1.put("port", "");
                        jsonArray.add(jsonObject1);
                    } else if (s.split(":").length == 1) {
                        jsonObject1.put("proxy", s.split(":")[0]);
                        jsonObject1.put("port", "");
                        jsonArray.add(jsonObject1);
                    } else if (s.split(":").length == 2) {
                        jsonObject1.put("proxy", s.split(":")[0]);
                        jsonObject1.put("port", s.split(":")[1]);
                        jsonArray.add(jsonObject1);
                    }
                }
            } else {
                JSONObject jsonObject = new JSONObject();
                String platform = machineWorkType.getPlatform();
                workTypes += platform;
                jsonObject.put("worker_type", platform);
                if (platform.trim().equals("AmazonDaemon")){
                    selectJson.put("max_io",select.getMaxIO());
                }
                jsonObject.put("types", machineWorkType.getWorkTypeTaskName());
                jsonObject.put("is_browser", machineWorkType.getIsBrowser());

                Task task = taskService.findTaskByName(machineWorkType.getWorkTypeTaskName());
                if (task != null){
                    jsonObject.put("ex_max_times",task.getExMaxTimes());
                }

                if (("Walmart").equals(platform)) {
                    jsonObject.put("is_need_rand_local", 0);
                    jsonObject.put("is_same_user_agent", 0);
                } else {
                    jsonObject.put("is_need_rand_local", 1);
                    jsonObject.put("is_same_user_agent", 1);
                }
                if (select.getMachineType() != MACHINE_TYPE_EXTRANET_VPS) {
                    //爬虫任务 非外网VPS
                    jsonObject.put("is_use_proxy", 1);
                } else {
                    jsonObject.put("is_use_proxy", 0);
                }
                // 爬虫1
                jsonObject.put("deleted_local", 1);
                jsonObject.put("proxy", "");
                jsonObject.put("port", "");
                jsonArray.add(jsonObject);
            }
        }

        selectJson.put("work_types", workTypes);
        selectJson.put("types", types);
        if (StringUtils.isEmpty(types)) {
            selectJson.put("worker", new JSONArray());
            logger.error("心跳参数 content 机器IP:{}, work_types:{}  types is null", select.getIp(), workTypes);
        } else {
            selectJson.put("worker", jsonArray);
            updateWorker(select, jsonArray,  selectJson, number);
        }
        selectJson.put("ip", select.getIp());
        selectJson.put("machine_type", select.getMachineType());
    }

    private void updateWorker(Machine select,JSONArray jsonArray, JSONObject selectJson, String number){
        if(select.getIp().equals("5.180.146.6:3389")){
            JSONArray jsonArrayTmp = new JSONArray();
            boolean change = false;
            int count = Integer.parseInt(number);
            for (Object o : jsonArray) {
                JSONObject jsonObject = (JSONObject) o;
                jsonArrayTmp.add(jsonObject);
                String worker_type = String.valueOf(jsonObject.get("worker_type"));
                if (worker_type.equals("AmazonDaemon")) {
                    putByNumber( jsonObject,  jsonArrayTmp, count );
                    change = true;
                }
            }
            if(change){
                selectJson.put("worker", jsonArrayTmp);
            }
        }
    }

    private void  putByNumber(JSONObject jsonObject, JSONArray jsonArrayTmp, int number){
        for (int i = 0; i < number; i++) {
            JSONObject jsonObject0 = new JSONObject();
            jsonObject0.putAll(jsonObject);
            jsonObject0.put("worker_type","AmazonDaemon_extend_"+i);
            jsonArrayTmp.add(jsonObject0);
        }

    }


    @SneakyThrows
    private boolean clientCheckScreen(String current, String ip) {
        boolean clientCheckScreen = redisUtils.exists("clientCheckScreen");
        if (clientCheckScreen) {
            Object clientCheckScreen1 = redisUtils.get("clientCheckScreen");
            if (clientCheckScreen1 != null) {
                List<String> list = Arrays.asList(String.valueOf(clientCheckScreen1).split(","));
                if (list != null && !list.isEmpty()) {
                    if (list.contains(ip)) {
                        return true;
                    }
                }
            }
        }
        // 3月和12月 31号 09:00:00到23:59:59
        if (checkDate(new String[]{"3", "12"}, "-31 09:00:00", "-31 23:59:59", current)) {
            return true;
        }

        // 6月和9月 30号 09:00:00到23:59:59
        if (checkDate(new String[]{"6", "9"}, "-30 09:00:00", "-30 23:59:59", current)) {
            return true;
        }

        // 延续 1、3、6、9季度末 多加两个小时
        if (checkDate(new String[]{"1", "4", "7", "10"}, "-01 00:00:00", "-01 04:00:00", current)) {
            return true;
        }

        return false;
    }

    private  boolean checkDate(String[] months, String startDate, String endDate, String current) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
            for (String s : months) {
                int start = sdf.parse(s + startDate).compareTo(sdf.parse(current));
                int end = sdf.parse(s + endDate).compareTo(sdf.parse(current));
                if (start == 0 || end == 0) {
                    return true;
                } else if (start < 0 && end > 0) {
                    return true;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 心跳更新数据库
     *
     * @param select
     * @param jsonObject
     */
    private void heatBeatUpdate(Machine select, JSONObject jsonObject) {

        String distInfo = String.valueOf(jsonObject.get("dist_info"));
        String cpuUsed = String.valueOf(jsonObject.get("cpu_used"));
        String memUsed = String.valueOf(jsonObject.get("mem_used"));
        String workType = String.valueOf(jsonObject.get("work_type"));
        String types = String.valueOf(jsonObject.get("types"));
        String clientVersion = String.valueOf(jsonObject.get("client_version"));
        String clientFileVersion = String.valueOf(jsonObject.get("client_file_version"));
        String userNames = String.valueOf(jsonObject.get("user_names"));
        String userNum = String.valueOf(jsonObject.get("user_num"));
        String tracertIps = String.valueOf(jsonObject.get("tracert_ips"));
        String machineLocalTime = null;
        if (jsonObject.containsKey("machine_local_time")) {
            machineLocalTime = String.valueOf(jsonObject.get("machine_local_time"));
        }
        Integer net_work = 0;
        if (jsonObject.containsKey("net_work")) {
            try {
                net_work = jsonObject.getInteger("net_work");
            } catch (Exception e) {
                logger.error("heatBeatUpdate net_work msg:{}", e.getMessage());
            }
        }

        try {
            String currentTime = DateUtils.getAfterDays(0);

            select.setMachineStatus(STATUS_VALID);
            select.setCpu(cpuUsed);
            select.setMemory(memUsed);
            select.setDiskSpace(distInfo);
            select.setLastHeartbeat(currentTime);
            select.setMachineLocalTime(machineLocalTime);
            select.setNetWork(net_work);
            select.setUserNames(userNames);
            select.setUserNum(userNum);
            select.setClientVersion(clientVersion);
            select.setClientFileVersion(clientFileVersion);
            machineService.updateByHeatBeat(select);
            initMachineCache.updateMachineCacheMapCacheByIp(select.getIp(), select);

            MachineHeartbeatLogs machineHeartbeatLogs = new MachineHeartbeatLogs();
            machineHeartbeatLogs.setMachineId(select.getId());
            machineHeartbeatLogs.setCpu(cpuUsed);
            machineHeartbeatLogs.setDiskSpace(distInfo);
            machineHeartbeatLogs.setMemory(memUsed);
            machineHeartbeatLogs.setHeartbeat(currentTime);
            machineHeartbeatLogs.setDate(currentTime.substring(0, 10));
            machineHeartbeatLogs.setCreatedTime(currentTime);
            machineHeartbeatLogs.setWorkType(workType);
            machineHeartbeatLogs.setNetWork(net_work);
            machineHeartbeatLogs.setTypes(types);
            machineHeartbeatLogs.setClientVersion(clientVersion);
            machineHeartbeatLogs.setClientFileVersion(clientFileVersion);
            machineHeartbeatLogs.setTracertIps(tracertIps);
            printUtils.printLog(machineHeartbeatLogs);
//            machineHeartbeatLogsService.insertMachineHeartbeatLogs(machineHeartbeatLogs);

        } catch (Exception e) {
            logger.error("heatBeatUpdate jsonObject:{} , msg:{}", jsonObject, e.getMessage());
        }
    }


    /**
     * 更新客户端版本
     *
     * @param selectJson
     * @param clientVersion
     * @param clientFileVersion
     * @param ip
     */
    private void updateClientVersion(JSONObject selectJson, String clientVersion, String clientFileVersion, String ip, Machine select) {


        List<Version> versionParentCache = initValidVersionCache.getVersionParentCache();
        List<Version> versionChildCache = initValidVersionCache.getVersionChildCache();
        Version childVersionByVersionId = versionService.getChildVersionByVersionId(clientVersion);
        if (childVersionByVersionId != null && "1".equals(childVersionByVersionId.getResetAll())) {
            versionChildCache = initValidVersionCache.getVersionAllCache();
        }


        Version versionChild = new Version();
        int resetAll = 0;
        if (versionChildCache != null && !versionChildCache.isEmpty()) {
            versionChild = versionChildCache.get(0);
            resetAll = Integer.parseInt(versionChild.getResetAll());
        }
        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();

        JSONObject jsonVersion = new JSONObject();
        jsonVersion.put("client_version", clientVersion);
        jsonVersion.put("client_file_version", clientFileVersion);
        jsonVersion.put("url", "");
        jsonVersion.put("reset_all", resetAll);
        jsonObject.put("updates", array);
        jsonVersion.put("files", jsonObject);
        selectJson.put("version", jsonVersion);

        if (versionParentCache == null || versionParentCache.isEmpty() || versionChildCache == null || versionChildCache.isEmpty()) {
            return;
        }
        Version version = new Version();
        if (versionParentCache != null && !versionParentCache.isEmpty()) {
            version = versionParentCache.get(0);
        }

        String versionClientVersion = version.getClientVersion();
        String versionClientFileVersion = versionChild.getClientFileVersion();

        boolean isUpdateChild = false;
        if (clientVersion.compareTo(versionClientVersion) < 0) {
            jsonVersion.put("url", "");
            parentVersionUpdate(versionParentCache, clientVersion, clientFileVersion, versionClientVersion, ip, array, select, jsonVersion);
        } else if (clientVersion.compareTo(versionClientVersion) == 0) {
            // 只要大版本相同 只要包含这个就移除
            String redisKeyPrefix = "clientVersionUpdate:";
            Object redisValue = redisUtils.get(redisKeyPrefix + ip);
            if (!StringUtils.isEmpty(redisValue)) {
                redisUtils.remove(redisKeyPrefix + ip);
            }
            if ("false".equals(clientFileVersion) || clientFileVersion.compareTo(versionClientFileVersion) < 0) {
                isUpdateChild = childVersionUpdate(versionChildCache, versionClientFileVersion, clientVersion, clientFileVersion, versionClientVersion, array, select);
            }
        }

        jsonVersion.put("client_version", versionClientVersion);
        if (isUpdateChild) {
            jsonVersion.put("client_file_version", versionClientFileVersion);
        } else {
            jsonVersion.put("client_file_version", clientFileVersion);
        }
        jsonObject.put("updates", array);
        jsonVersion.put("files", jsonObject);
        selectJson.put("version", jsonVersion);

    }


    /**
     * 子版本更新不受限流控制
     */
    private boolean childVersionUpdate(List<Version> versionChildCache, String versionClientFileVersion, String clientVersion, String clientFileVersion, String versionClientVersion, JSONArray array, Machine select) {
        Version versionChild = versionChildCache.get(0);
        //versionClientFileVersion=2.11 clientFileVersion=2.01 只更新小版本(更新客户端文件版本)
        Set<String> stringFiles = new HashSet<>();
        Map<String, String> pathMap = new HashMap<>();
        Map<String, String> fileNameMap = new HashMap<>();
        Map<String, String> fileNameUrl = new HashMap<>();
        List<VersionFile> versionChildVersionFile = versionChild.getVersionFile();
        if (versionChildVersionFile != null && !versionChildVersionFile.isEmpty()) {
            for (VersionFile file : versionChildVersionFile) {
                String path = versionChild.getClientVersion() + File.separator + versionChild.getClientFileVersion() + File.separator + file.getClientFilePath() + File.separator + file.getFileName();
                String pathUrl = versionChild.getClientVersion() + File.separator + versionChild.getClientFileVersion() + File.separator + file.getFileName();
                stringFiles.add(path);
                if (StringUtils.isEmpty(file.getClientFilePath())) {
                    pathMap.put(path, "");
                } else {
                    pathMap.put(path, checkClientPath(file.getClientFilePath()));
                }
                fileNameMap.put(path, file.getFileName());
                fileNameUrl.put(path, pathUrl);
            }
        }
        updatesJSonArray(stringFiles, versionClientVersion, array, select, pathMap, fileNameMap, fileNameUrl, true, true);
        return true;
    }


    private void parentVersionUpdate(List<Version> versionParentCache, String clientVersion, String clientFileVersion, String versionClientVersion, String ip, JSONArray array, Machine select, JSONObject jsonVersion) {
        String redisKeyPrefix = "clientVersionUpdate:";
        String redisNumKey = "clientVersionUpdate:num";
        if (versionClientVersion.compareTo(clientVersion) > 0) {
            //versionClientVersion=2.00  clientVersion=1.00 更新客户端版本,大版本更新不返回小版本文件
            Version version = versionParentCache.get(0);
            int updateLimit = version.getUpdateLimit();
            List<VersionFile> versionFile = version.getVersionFile();
            Set<String> strings = new HashSet<>();
            Map<String, String> pathMap = new HashMap<>();
            Map<String, String> fileNameMap = new HashMap<>();
            Map<String, String> fileNameMapUrl = new HashMap<>();
            if (versionFile != null && !versionFile.isEmpty()) {
                for (VersionFile file : versionFile) {
                    String path = version.getClientVersion() + File.separator + file.getFileName();
                    String pathUrl = version.getClientVersion() + File.separator + file.getFileName();
                    strings.add(path);
                    if (StringUtils.isEmpty(file.getClientFilePath())) {
                        pathMap.put(path, "");
                    } else {
                        pathMap.put(path, file.getClientFilePath());
                    }
                    fileNameMap.put(path, file.getFileName());
                    fileNameMapUrl.put(path, pathUrl);
                }
            }
            synchronized (ClientService.class) {
                JSONObject object = new JSONObject();
                object.put("client_version", versionClientVersion);
                object.put("client_file_version", clientFileVersion);
                List<Boolean> list = new ArrayList<>();
                int count = count(redisNumKey, redisKeyPrefix, ip, object, list, updateLimit);
                if (!list.isEmpty()) {
                    jsonVersion.put("url", updatesJSonArray(strings, versionClientVersion, array, select, pathMap, fileNameMap, fileNameMapUrl, false, false));
                }
                if (count >= updateLimit) {
                    return;
                }
            }
        } else if (versionClientVersion.compareTo(clientVersion) == 0) {
            // 只要大版本相同 只要包含这个就移除
            Object redisValue = redisUtils.get(redisKeyPrefix + ip);
            if (!StringUtils.isEmpty(redisValue)) {
                redisUtils.remove(redisKeyPrefix + ip);
                return;
            }
        }
    }

    private String checkClientPath(String clientPath) {
        if (clientPath.contains("\\")) {
            if (!clientPath.endsWith("\\")) {
                clientPath = clientPath + "\\";
            }
            clientPath = clientPath.replaceAll("\\\\", "/");
        } else if (clientPath.contains("/")) {
            if (!clientPath.endsWith("/")) {
                clientPath = clientPath + "/";
            }
        }
        return clientPath;
    }


    /**
     * 计算有效的正在升级客户端的数量
     *
     * @param redisNumKey
     * @param redisKeyPrefix
     * @return
     */
    private int count(String redisNumKey, String redisKeyPrefix, String ip, JSONObject object, List<Boolean> list, int updateLimit) {
        int count = 0;
        Object redisNumValue = redisUtils.get(redisNumKey);
        boolean isContain = false;
        if (!StringUtils.isEmpty(redisNumValue)) {
            String[] split = String.valueOf(redisNumValue).split(";");
            String newKey = "";
            for (String s : split) {
                Object o = redisUtils.get(redisKeyPrefix + s);
                if (!StringUtils.isEmpty(o)) {
                    count++;
                    if (!StringUtils.isEmpty(newKey)) {
                        newKey = newKey + ";";
                    }
                    newKey = newKey + s;
                    if (ip.equals(s)) {
                        isContain = true;
                    }
                }
            }

            if (!StringUtils.isEmpty(newKey)) {
                redisUtils.put(redisNumKey, newKey, Long.valueOf(3600 * 12));
            } else {
                redisUtils.remove(redisNumKey);
            }
        }
        if (count < updateLimit && !isContain) {
            list.add(true);
            updateRedisCache(redisKeyPrefix, redisNumKey, ip, object);
        }

        return count;
    }

    /**
     * 更新缓存
     *
     * @param redisKeyPrefix
     * @param redisNumKey
     * @param ip
     * @param object
     */
    private void updateRedisCache(String redisKeyPrefix, String redisNumKey, String ip, JSONObject object) {
        redisUtils.put(redisKeyPrefix + ip, object.toJSONString(), Long.valueOf(1800));
        Object redisNumValue = redisUtils.get(redisNumKey);
        if (StringUtils.isEmpty(redisNumValue)) {
            redisUtils.put(redisNumKey, ip, Long.valueOf(1800));
        } else {
            redisUtils.put(redisNumKey, String.valueOf(redisNumValue) + ";" + ip, Long.valueOf(1800));
        }
    }

    private String updatesJSonArray(Set<String> stringFiles, String versionClientVersion, JSONArray array, Machine select, Map<String, String> pathMap, Map<String, String> fileNameMap, Map<String, String> fileNameUrl, boolean flag, boolean isChild) {
        if (!stringFiles.isEmpty()) {
            String returnDownloadFileUrl = "";
            String ip = "";
            if (select.getMachineType() == MACHINE_TYPE_EXTRANET_VPS) {
                ip = ftpConfig.getDomainName();
            } else if (!isChild && select.getMachineType() == MACHINE_TYPE_CHONGQING_ACCOUNT_MACHINE) {
                return "http://10.30.179.5/release/operatingstation/new/";
            } else {
                if (ftpConfigInner.getPath().startsWith("/")) {
                    ip = ftpConfigInner.getHost() + ftpConfigInner.getPath();
                } else {
                    ip = ftpConfigInner.getHost() + File.separator + ftpConfigInner.getPath();
                }

            }
            for (String string : stringFiles) {
                JSONObject jsonObject1 = new JSONObject();
                String location = pathMap.get(string);
                String fileName = fileNameMap.get(string);
                String path = fileNameUrl.get(string);
                String locationName = Constant.VERSION_FILE + File.separator + path;
                String returnDownloadFileUrlTmp = "http://" + ip + File.separator + locationName.replace(fileName, "");
                jsonObject1.put("location_name", location + fileName);
                String downloadFileUrl = "http://" + ip + File.separator + locationName;
                jsonObject1.put("download_file_url", downloadFileUrl);
                jsonObject1.put("file_name", fileName);
                if (flag) {
                    array.add(jsonObject1);
                }
                if (fileName.endsWith("exe")) {
                    returnDownloadFileUrl = returnDownloadFileUrlTmp;
                }
            }
            return returnDownloadFileUrl;
        }
        return "";
    }

    /**
     * 设置代理IP请求数
     * @param jsonObject
     * @param ip
     * @return
     */
    public ApiResponseDomain setProxyInfo(JSONObject jsonObject,String ip) {
        String proxy = jsonObject.getString("proxy");
        String port = jsonObject.getString("port");
        String workType = jsonObject.getString("work_type");
        int usedNum = jsonObject.getInteger("use_num");
        int succeedNum = jsonObject.getInteger("success_num");
        int bannedNum = jsonObject.getInteger("banned_num");
        ApiResponse apiResponse = new ApiResponse();

        if (StringUtils.isEmpty(proxy) || StringUtils.isEmpty(port) || StringUtils.isEmpty(workType)) {
            return apiResponse.error(ResponseCode.ERROR_CODE, "请确认相关参数配置", jsonObject);
        }
        ProxyIp proxyIp = proxyIpService.getProxyIpByUniqueKey(proxy, Integer.parseInt(port));
        if (proxyIp != null){
            ProxyRequestLogs proxyRequestLogs = new ProxyRequestLogs();
            proxyRequestLogs.setProxyId(proxyIp.getId());
            proxyRequestLogs.setProxyIp(proxyIp.getIp());
            proxyRequestLogs.setPort(proxyIp.getPort());
            proxyRequestLogs.setWorkType(workType);
            proxyRequestLogs.setMachineIp(ip);
            proxyRequestLogs.setUsedNum(usedNum);
            proxyRequestLogs.setSucceedNum(succeedNum);
            proxyRequestLogs.setBannedNum(bannedNum);
            proxyRequestLogsService.insertRequestLogs(proxyRequestLogs);

//            proxyPoolStrategy(proxyIp,workType,usedNum,bannedNum);

        }else {
            return apiResponse.error(ResponseCode.ERROR_CODE, "相关代理IP查询不到", jsonObject);
        }

        return apiResponse.success("success", "");
    }

    /**
     * 配置控制策略，触发限制代理IP，redis淘汰策略触发恢复代理IP
     * @param proxyIp
     * @param workType
     * @param usedNum
     * @param bannedNum
     */
    private void proxyPoolStrategy(ProxyIp proxyIp,String workType,int usedNum,int bannedNum){
        Map<String,Integer> hashMap = new HashMap<>();
        String prefix = CacheKey.PROXY_POOL+workType+":"+proxyIp.getId() + ":";
        String delayKey = prefix + proxyIp.getUnitTime();

        String limitConfig = proxyIp.getLimitConfig();
        JSONObject limitConfigObject = JSONObject.parseObject(limitConfig,Feature.OrderedField);

        Iterator iter = limitConfigObject.entrySet().iterator();
        // 限制次数的单位时间是否与被禁率单位时间相同的标识，相同会同时存使用数和禁用数，
        // 单位时间不相同，限制次数的不存禁用数，节省redis空间
        boolean flag = false;

        boolean isOverMaxLimit = false;
        //是否超限的标识，只要循环中的一个为true就需要延迟，多个true，以最后一个true为准
        boolean isOverMaxLimitFlag = false;
        //标识之前是否已经判定超过次数了，后一次的超过了得覆盖前一次的
        boolean beforeOverMaxLimit = false;
        boolean isOverMaxBannedRate = false;
        long limitTimeStamp = 0;
        long delayTimeStamp = 0;
        long openTimestamp = 0;
        while (iter.hasNext()) {
            Map.Entry<String, JSONObject> entry = (Map.Entry<String, JSONObject>) iter.next();
            String limitKey = prefix + entry.getKey();

            synchronized (limitKey.intern()){
                if (redisUtils.exists(limitKey)){
                    Object value = redisUtils.get(limitKey);
                    JSONObject valueObject = JSONObject.parseObject(String.valueOf(value));
                    int beforeUsedNum = Integer.parseInt(String.valueOf(valueObject.get("used_num")));

                    Integer currentUsedNum = beforeUsedNum + usedNum;
                    hashMap.put("used_num",currentUsedNum);
                    //单位时间相同可放在同一个key
                    if (limitKey.equals(delayKey)){
                        flag = true;
                        int beforeBannedNum = Integer.parseInt(String.valueOf(valueObject.get("banned_num")));
                        Integer currentBannedNum = beforeBannedNum + bannedNum;
                        hashMap.put("banned_num",currentBannedNum);
                    }
                }else {
                    hashMap.put("used_num",usedNum);
                    if (limitKey.equals(delayKey)){
                        flag = true;
                        hashMap.put("banned_num",bannedNum);
                    }
                }

                beforeOverMaxLimit = isOverMaxLimit;
                int maxUsedTimes = entry.getValue().getIntValue("max_used_times");

                isOverMaxLimit = isOverMaxLimit(hashMap.get("used_num"),maxUsedTimes);
                limitTimeStamp = calculateLimitTimeStamp(Integer.parseInt(String.valueOf(entry.getKey())));
                //如果之前一个为true，后边一个也为true，则覆盖之前的，否则不变；如果之前为false，后边一个为true，则初始
                if (beforeOverMaxLimit){
                    if (isOverMaxLimit){
                        //覆盖之前的
                        openTimestamp = limitTimeStamp;
                    }
                }else {
                    //初始
                    if (isOverMaxLimit){
                        isOverMaxLimitFlag = true;
                        openTimestamp = limitTimeStamp;
                    }
                }

                if (hashMap.containsKey("banned_num")){
                    isOverMaxBannedRate = isOverMaxBannedRate(hashMap.get("used_num"),hashMap.get("banned_num"),proxyIp.getMaxBannedRate());
                    if (isOverMaxBannedRate){
                        delayTimeStamp = System.currentTimeMillis() + (long) proxyIp.getDelayTime() * 3600 *1000;
                    }
                }

                long ttl = (limitTimeStamp-System.currentTimeMillis()) / 1000;
                redisUtils.put(limitKey,JSON.toJSONString(hashMap),ttl);
            }
            hashMap.clear();
        }

        //不存在与限制次数相同的单位时间，另外去存禁用数与使用数
        if (!flag){
            synchronized (delayKey.intern()){
                if (redisUtils.exists(delayKey)){
                    Object value = redisUtils.get(delayKey);
                    JSONObject valueObject = JSONObject.parseObject(String.valueOf(value));
                    int beforeUsedNum = Integer.parseInt(String.valueOf(valueObject.get("used_num")));
                    int beforeBannedNum = Integer.parseInt(String.valueOf(valueObject.get("banned_num")));
                    Integer currentUsedNum = beforeUsedNum + usedNum;
                    Integer currentBannedNum = beforeBannedNum + bannedNum;
                    hashMap.put("used_num",currentUsedNum);
                    hashMap.put("banned_num",currentBannedNum);
                }else {
                    hashMap.put("used_num",usedNum);
                    hashMap.put("banned_num",bannedNum);
                }
                isOverMaxBannedRate = isOverMaxBannedRate(hashMap.get("used_num"),hashMap.get("banned_num"),proxyIp.getMaxBannedRate());
                if (isOverMaxBannedRate){
                    delayTimeStamp = System.currentTimeMillis() + (long) proxyIp.getDelayTime() * 3600 *1000;
                }

                limitTimeStamp = calculateLimitTimeStamp(proxyIp.getUnitTime());
                long ttl = (limitTimeStamp-System.currentTimeMillis()) / 1000;
                redisUtils.put(delayKey,JSON.toJSONString(hashMap),ttl);
            }
        }

        // 两个限制同时达成的逻辑，看延迟的时间是否大于限制截止的时间，
        // 若大于，以延迟时间的逻辑为准，
        // 若小于，以限制的逻辑为准
        if (isOverMaxLimitFlag && isOverMaxBannedRate){
            if (delayTimeStamp > openTimestamp){
                openTimestamp = delayTimeStamp;
            }
            delayTimeController(proxyIp.getId(),workType,openTimestamp);
        }else {
            if (isOverMaxLimitFlag){
                delayTimeController(proxyIp.getId(),workType,openTimestamp);
            }
            if (isOverMaxBannedRate){
                openTimestamp = delayTimeStamp;
                delayTimeController(proxyIp.getId(),workType,openTimestamp);
            }
        }
    }

    /**
     * 计算单位时间限制的截止时间-当前时刻/单位时间取整*单位时间+当前0点
     * @param unitTime
     * @return
     */
    private long calculateLimitTimeStamp(int unitTime){
        int a = DateUtils.getCurrentHour()/unitTime;
        return (long) (a + 1) * unitTime * 3600 * 1000 + DateUtils.getTodayStartTime();
    }

    /**
     * 超过限制次数返回true
     * @param currentTimes
     * @param maxTimes
     * @return
     */
    private boolean isOverMaxLimit(int currentTimes,int maxTimes){
        if (currentTimes > maxTimes){
            return true;
        }
        return false;
    }

    /**
     * 超过设置的最大禁用率返回true
     * @param usedNum
     * @param bannedNum
     * @param maxBannedRate
     * @return
     */
    private boolean isOverMaxBannedRate(int usedNum, int bannedNum, int maxBannedRate){
        float rate = (float)bannedNum / (float)usedNum;
        if (rate * 100 < maxBannedRate){
            return false;
        }
        return true;
    }

    /**
     * 将ip使用时间延迟
     * @param proxyId
     * @param workType
     * @param openTimeStamp
     */
    private void delayTimeController(int proxyId, String workType, long openTimeStamp){
        proxyIPPool.addQueue(workType,openTimeStamp+1000,proxyId);

        ProxyIpPlatform proxyIpPlatform = new ProxyIpPlatform();
        proxyIpPlatform.setProxyIpId(proxyId);
        proxyIpPlatform.setPlatform(workType);
        proxyIpPlatform.setStatus(STATUS_VALID);
        proxyIpPlatform.setBanPeriod(DateUtils.getCurrentDate());
        proxyIpPlatform.setOpenTimestamp(BigInteger.valueOf(openTimeStamp));
        proxyIpPlatformService.update(proxyIpPlatform);

        proxyTrendService.insertProxyTrend(proxyId,workType,0,DateUtils.convertTimestampToDate(BigInteger.valueOf(openTimeStamp)));
    }

    /**
     * 记录客户端http请求日志
     * @param jsonArray
     * @param machineIp
     */
    public void reportRequestLog(JSONArray jsonArray, String machineIp) throws ParseException {
        Iterator<Object> iterator = jsonArray.iterator();
        while (iterator.hasNext()){
            JSONObject jsonObject = (JSONObject) iterator.next();
            String logType = jsonObject.getString("log_type");
            //兼容未切换的
            if (StringUtils.isEmpty(logType)){
                printClientRequestLog(jsonObject,machineIp);
            }else {
                switch (logType){
                    case "request":
                        printClientRequestLog(jsonObject,machineIp);
                        break;
                    case "action":
                        printActionRequestLog(jsonObject,machineIp);
                        break;
                    case "task_source_log":
                        printClientJobLog(jsonObject,machineIp);
                        break;
                    default:
                        logger.error("不支持的客户端日志类型-{}",logType);
                        break;
                }
            }
        }
    }

    /**
     * 客户端请求日志
     * @param jsonObject
     * @param machineIp
     */
    private void printClientRequestLog(JSONObject jsonObject, String machineIp){
        String url = jsonObject.getString("url");
        int httpCode = jsonObject.getIntValue("http_code");
        boolean code = jsonObject.getBooleanValue("code");
        int status = jsonObject.getIntValue("status");
        String proxyIp = jsonObject.getString("proxy_ip");
        String outIpRecord = jsonObject.getString("out_ip_record");
        String error = jsonObject.getString("error");
        String workType = jsonObject.getString("work_type");
        String jobType = jsonObject.getString("job_type");
        long requestTime = jsonObject.getLongValue("request_time");
        boolean isBlocked = jsonObject.getBooleanValue("is_blocked");
        int weight = jsonObject.getIntValue("weight");
        String taskSourceId = jsonObject.getString("task_source_id");

        String[] split = String.valueOf(proxyIp).split(":");
        String shortProxyIp;
        if (split.length <= 2 ){
            shortProxyIp = proxyIp;
        }else {
            String[] split1 = split[1].split("-");
            shortProxyIp = split1[6] + ":" + split[3];
            String ip = split[0] + ":" + split[1] + ":" + split[2];

            makeProxyGray(ip, split[3],workType, isBlocked);
        }

        String outIp = CommonUtils.match(outIpRecord, "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");

        ClientRequestLog clientRequestLog = new ClientRequestLog();
        clientRequestLog.setUrl(url);
        clientRequestLog.setUrlCategory(parseUrlCategory(url));
        clientRequestLog.setHttpCode(httpCode);
        clientRequestLog.setCode(code);
        clientRequestLog.setStatus(status);
        clientRequestLog.setProxyIp(proxyIp);
        clientRequestLog.setShortProxyIp(shortProxyIp);
        clientRequestLog.setOutIpRecord(outIpRecord);
        clientRequestLog.setOutIp(outIp);
        clientRequestLog.setMachineIp(machineIp);
        clientRequestLog.setError(error);
        clientRequestLog.setWorkType(workType);
        clientRequestLog.setJobType(jobType);
        clientRequestLog.setRequestTime(requestTime);
        clientRequestLog.setBlocked(isBlocked);
        clientRequestLog.setWeight(weight);
        clientRequestLog.setTaskSourceId(taskSourceId);
        String pattern = "yyyy-MM-dd'T'HH:mm:ssZZ";
        clientRequestLog.setCreatedTime(DateFormatUtils.format(DateUtils.getCurrentDateToDate(), pattern));
        clientHttpRequestLogsPrintUtil.printLog(clientRequestLog);
    }

    /**
     * 客户端行为日志
     * @param jsonObject
     * @param machineIp
     * @throws ParseException
     */
    private void printActionRequestLog(JSONObject jsonObject, String machineIp) throws ParseException {
        long startTime = jsonObject.getLongValue("start_time");
        long endTime = jsonObject.getLongValue("end_time");
        long timeDiff = endTime - startTime;
        String recordTime = jsonObject.getString("record_time");
        String action = jsonObject.getString("action");
        String method = jsonObject.getString("method");

        ClientActionLog clientActionLog = new ClientActionLog();
        clientActionLog.setStartTime(startTime);
        clientActionLog.setEndTime(endTime);
        clientActionLog.setTimeDiff(timeDiff);
        clientActionLog.setRecordTime(DateUtils.convertISOTime(recordTime));
        clientActionLog.setMachineIp(machineIp);
        clientActionLog.setAction(action);
        clientActionLog.setMethod(method);

        clientActionLogsPrintUtil.printLog(clientActionLog);
    }

    /**
     * 客户端执行任务日志
     * @param jsonObject
     * @param machineIp
     */
    private void printClientJobLog(JSONObject jsonObject, String machineIp){
        String taskSourceId = jsonObject.getString("task_source_id");
        Boolean isSucceed = jsonObject.getBoolean("is_success");
        String error = jsonObject.getString("error");
        String method = jsonObject.getString("method");
        String jobType = jsonObject.getString("job_type");

        ClientJobLog clientJobLog = new ClientJobLog();
        clientJobLog.setTaskSourceId(taskSourceId);
        clientJobLog.setSucceed(isSucceed);
        clientJobLog.setError(error);
        clientJobLog.setMachineIp(machineIp);
        clientJobLog.setMethod(method);
        clientJobLog.setJobType(jobType);
        String pattern = "yyyy-MM-dd'T'HH:mm:ssZZ";
        clientJobLog.setCreatedTime(DateFormatUtils.format(DateUtils.getCurrentDateToDate(), pattern));
        clientJobLogsPrintUtil.printLog(clientJobLog);
    }

    private String parseUrlCategory(String url){
        String urlCategory = "other";
        if (matchUrl(url,".*validateCaptcha.*")){
            urlCategory = "captcha";
        }
        if (matchUrl(url,".*captcha.*")){
            urlCategory = "captcha";
        }
        if (matchUrl(url,".*Captcha.*")){
            urlCategory = "captcha";
        }
        if (matchUrl(url,".*amazon.com/dp/.*")){
            urlCategory = "detail";
        }
        if (matchUrl(url,".*amazon.com/gp/product/ajax/.*")){
            urlCategory = "get_offer_listing";
        }
        if (matchUrl(url,".*amazon.com/gp/product/handle-buy-box/.*")){
            urlCategory = "add_cart";
        }
        return urlCategory;
    }

    private boolean matchUrl(String url, String regex){
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(url);
        return m.find();
    }

    /**
     * 代理IP加灰度值
     * @param proxyIp 代理IP
     * @param port 端口号
     * @param workType 大类型
     */
    private void makeProxyGray(String proxyIp, String port, String workType, boolean isBlocked){
        ProxyIp proxyIpByUniqueKey = proxyIpService.getProxyIpByUniqueKey(proxyIp, Integer.parseInt(port));
        if (proxyIpByUniqueKey != null && isBlocked){
            String proxyPoolKey = CacheKey.PROXY_POOL+workType;
            Double score = redisUtils.score(proxyPoolKey, proxyIpByUniqueKey.getId());
            //一个禁用请求延迟一分钟
            delayTimeController(proxyIpByUniqueKey.getId(),workType, score.longValue()+60*1000L);
        }
    }
}
