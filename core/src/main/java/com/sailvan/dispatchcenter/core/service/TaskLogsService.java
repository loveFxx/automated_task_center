package com.sailvan.dispatchcenter.core.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.*;
import com.sailvan.dispatchcenter.common.domain.StoreAccount;
import com.sailvan.dispatchcenter.common.domain.Task;
import com.sailvan.dispatchcenter.common.domain.TaskLogs;
import com.sailvan.dispatchcenter.common.domain.TaskResult;
import com.sailvan.dispatchcenter.common.pipe.ProxyIpService;
import com.sailvan.dispatchcenter.common.pipe.TaskResultService;
import com.sailvan.dispatchcenter.common.pipe.TaskSourceListService;
import com.sailvan.dispatchcenter.common.util.CommonUtils;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.TaskUtil;
import com.sailvan.dispatchcenter.core.domain.TaskBuffer;
import com.sailvan.dispatchcenter.core.log.TaskLogsPrintUtil;
import com.sailvan.dispatchcenter.db.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.Map;


public class TaskLogsService{

    @Autowired
    TaskResultService taskResultService;

    @Autowired
    TaskUtil taskUtil;

    @Autowired
    TaskSourceListService taskSourceListService;

    @Resource
    ProxyIpService proxyIpService;

    @Autowired
    TaskLogsPrintUtil taskLogsPrintUtil;

    @Autowired
    TaskService taskService;

    public void addTaskEvent(TaskResult taskResult, TaskBuffer taskBuffer,String ip, int runMode) {
        Task task = taskService.findTaskByName(taskBuffer.getType());

        //组合任务需拆分
        if (task.getIsCombo()==1){
            //有返回结果的需拆分里面返回的具体结果，无返回结果为系统认定强制失败的结果，直接根据数量拆分
            JSONObject object = JSON.parseObject(taskResult.getClientResult());
            if (object!=null && !object.isEmpty()){
                Iterator iter = object.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String,JSONObject> entry = (Map.Entry<String,JSONObject>) iter.next();
                    int code = entry.getValue().getInteger("code");

                    insertEventLogs(taskResult,taskBuffer,ip,runMode,code,entry);
                }
            }else {
                JSONObject jsonObject = JSONObject.parseObject(taskBuffer.getClient_params());
                JSONArray lists = (JSONArray)jsonObject.get("lists");
                for (int i = 0;i< lists.size();i++){
                    insertEventLogs(taskResult,taskBuffer,ip,runMode,taskResult.getClientCode(),null);
                }
            }

        }else {
            int code = taskResult.getClientCode();
            insertEventLogs(taskResult,taskBuffer,ip,runMode,code,null);
        }
    }

    private void insertEventLogs(TaskResult taskResult, TaskBuffer taskBuffer,String ip, int runMode,int code,Map.Entry<String,JSONObject> entry){

        TaskLogs taskLogs = new TaskLogs();

        taskLogs.setTaskSourceId(taskBuffer.getTask_source_id());
        taskLogs.setResultHashKey(taskBuffer.getResult_hash_key());
        taskLogs.setHashKey(parseHashKey(taskBuffer.getResult_hash_key(),taskBuffer.getRefresh_time()));
        taskLogs.setTaskName(taskBuffer.getType());
        taskLogs.setRetryTimes(taskResult.getRetryTimes());

        if (code == ResponseCode.SUCCESS_CODE){
            taskLogs.setEvent(Event.RETURN_RESULT_SUCCESS);
            taskLogs.setExplain("成功");
            JSONObject object = JSON.parseObject(taskResult.getClientResult());
            if (object != null && object.containsKey("file")){
                taskLogs.setHasFile(1);
            }
        }else if (code == ResponseCode.CLIENT_SERVER_TIMEOUT){
            taskLogs.setEvent(Event.TIMEOUT_FAILURE);
            taskLogs.setExplain(taskResult.getClientError());
        }else {
            taskLogs.setEvent(Event.RETURN_RESULT_FAILURE);
            if (entry != null){
                taskLogs.setExplain(entry.getValue().getString("error"));
            }else {
                taskLogs.setExplain(taskResult.getClientError());
            }
        }

        String[] s = taskBuffer.getWork_type().split("_");
        if (s.length == 2){
            taskLogs.setAccount(s[0]);
            taskLogs.setContinent(s[1]);
            StoreAccount data = (StoreAccount)proxyIpService.getProxyIp(s[0], s[1], null);
            taskLogs.setProxyIp(data.getProxyIp());
        }
        if (s.length == 1){
            taskLogs.setPlatform(s[0]);
            taskLogs.setProxyIp("");
        }

        taskLogs.setRemoteIp(ip);
        if (entry != null){
            taskLogs.setClientParams(entry.getKey());
        }else {
            taskLogs.setClientParams(taskBuffer.getClient_params());
        }

        taskLogs.setClientCode(code);
        taskLogs.setCenterParams(taskBuffer.getCenter_params());
        taskLogs.setRefreshTime(taskBuffer.getRefresh_time());
        taskLogs.setCreatedTime(DateUtils.getCurrentDate());
        taskLogs.setDate(taskLogs.getCreatedTime().substring(0,10));
        taskLogs.setRunMode(runMode);
        insertTaskLogs(taskLogs);
    }

    public void insertTaskLogs(TaskLogs taskLogs) {
        taskLogsPrintUtil.printLog(taskLogs);
    }

    public void addTaskLogs(TaskBuffer taskBuffer, int event, String ip,int runMode){
        Task task = taskService.findTaskByName(taskBuffer.getType());

        String tslId = taskBuffer.getTask_source_id();
        String[] taskslId = tslId.split("_");
        int isSingle = 0;
        if (CacheKey.SINGLE.equals(taskslId[0])){
            isSingle = 1;
        }

        if (event == Event.OUT_POOL){
            taskSourceListService.updateLastResultTimeById(Integer.parseInt(taskslId[1]),null,isSingle, TaskStateKey.TASK_STATE_INEXECUTED);
        }
        if (event == Event.CREATE_TASK || event == Event.MANUAL_CREATE_TASK){
            taskSourceListService.updateLastResultTimeById(Integer.parseInt(taskslId[1]),null,isSingle, TaskStateKey.TASK_STATE_UNEXECUTED);
        }
        if (task.getIsCombo() == 1){
            divideCombo(taskBuffer, event, ip, runMode);
        }else {
            addTaskLogs(taskBuffer,event,ip,runMode,"");
        }
    }

    private void divideCombo(TaskBuffer taskBuffer, int event, String ip,int runMode){
        JSONObject jsonObject = JSONObject.parseObject(taskBuffer.getClient_params());
        JSONArray lists = (JSONArray)jsonObject.get("lists");
        if (lists != null && !lists.isEmpty()){
            for (Object o : lists) {
                JSONObject list = (JSONObject) o;
                String clientParams = list.toJSONString();
                addTaskLogs(taskBuffer, event, ip, runMode, clientParams);
            }
        }
    }

    private int parseHashKey(int resultHashKey, String refreshTime){
        return CommonUtils.hashCode("ABGH",resultHashKey+refreshTime.trim());
    }

    private void addTaskLogs(TaskBuffer taskBuffer, int event, String ip,int runMode,String clientParams){
        TaskLogs taskLogs = new TaskLogs();

        taskLogs.setTaskSourceId(taskBuffer.getTask_source_id());
        taskLogs.setResultHashKey(taskBuffer.getResult_hash_key());
        taskLogs.setHashKey(parseHashKey(taskBuffer.getResult_hash_key(),taskBuffer.getRefresh_time()));
        taskLogs.setTaskName(taskBuffer.getType());

        int smallestId = taskUtil.getTaskResultSearchSmallestId();
        TaskResult taskResultInfo = taskResultService.getTaskResultByTaskSourceIdAndResultHashKeyAndRefreshTime(smallestId, taskBuffer.getTask_source_id(),taskBuffer.getResult_hash_key(),taskBuffer.getRefresh_time());

        int retryTimes = 0;
        if (taskResultInfo != null) {
            retryTimes = taskResultInfo.getRetryTimes();
        }
        taskLogs.setRetryTimes(retryTimes);
        taskLogs.setEvent(event);
        taskLogs.setExplain("成功");
        String[] s = taskBuffer.getWork_type().split("_");
        if (s.length == 2){
            taskLogs.setAccount(s[0]);
            taskLogs.setContinent(s[1]);

            StoreAccount data = (StoreAccount)proxyIpService.getProxyIp(s[0], s[1], null);
            taskLogs.setProxyIp(data.getProxyIp());
        }
        if (s.length == 1){
            taskLogs.setPlatform(s[0]);
            taskLogs.setProxyIp("");
        }
        taskLogs.setCenterParams(taskBuffer.getCenter_params());

        if (!StringUtils.isEmpty(clientParams)){
            taskLogs.setClientParams(clientParams);
        }else {
            taskLogs.setClientParams(taskBuffer.getClient_params());
        }

        taskLogs.setRefreshTime(taskBuffer.getRefresh_time());
        taskLogs.setCreatedTime(DateUtils.getCurrentDate());
        taskLogs.setRemoteIp(ip);
        taskLogs.setDate(taskLogs.getCreatedTime().substring(0,10));
        taskLogs.setRunMode(runMode);
        insertTaskLogs(taskLogs);
    }

}
