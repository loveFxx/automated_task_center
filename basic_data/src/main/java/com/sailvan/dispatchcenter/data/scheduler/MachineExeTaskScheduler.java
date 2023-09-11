package com.sailvan.dispatchcenter.data.scheduler;

import com.sailvan.dispatchcenter.common.constant.CacheKey;
import com.sailvan.dispatchcenter.common.constant.Event;
import com.sailvan.dispatchcenter.common.domain.MachineExeTask;
import com.sailvan.dispatchcenter.common.domain.TaskLogs;
import com.sailvan.dispatchcenter.common.pipe.MachineExeTaskService;
import com.sailvan.dispatchcenter.common.pipe.TaskLogsService;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.common.util.TaskUtil;
import com.sailvan.dispatchcenter.common.util.WriteToFileUtil;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 机器每小时执行任务
 *
 * @author mh
 * @date 2021-12
 */
@Component
public class MachineExeTaskScheduler {

    private static Logger logger = LoggerFactory.getLogger(MachineExeTaskScheduler.class);

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    TaskLogsService taskLogsService;

    @Autowired
    TaskUtil taskUtil;

    @Autowired
    MachineExeTaskService  machineExeTaskService;

    @Scheduled(cron = "0 1 */1 * * ?")
    public void persistence() {
        refreshMachineExeTask(0);
    }

    /**
     *  hour=0 是统计当前时间的前一个小时的数据
     * @param hour
     */
    @SneakyThrows
    public void refreshMachineExeTask(int hour) {
        boolean isSkipDay = false;
        //当天时间
        String currentTimeHour = DateUtils.getHourBeforeDate(hour).substring(0, 13);
        String currentTimeDay = currentTimeHour.substring(0, 10);

        //一小时前
        String lastTimeHour = DateUtils.getHourBeforeDate(hour+1).substring(0, 13);
        String lastTimeDay = lastTimeHour.substring(0, 10);

        //三小时前
        String lastTimeThreeHour = DateUtils.getHourBeforeDate(hour+3).substring(0, 13);
        String lastTimeThreeDay = lastTimeThreeHour.substring(0, 10);
        int taskLogCacheId = taskUtil.getIdCache("task_log_min_id", "./machine_exe_task_cache/");
        if (!lastTimeDay.equals(currentTimeDay)) {
            isSkipDay = true;
            taskLogCacheId = 0;
        }
        String start;
        if (!lastTimeDay.equals(lastTimeThreeDay)) {
            start = lastTimeThreeDay + " 00:00:00";
        } else {
            start = lastTimeThreeHour + ":00:00";
        }

        String startTime;
        String endTime;
        if (isSkipDay) {
            startTime = lastTimeDay + " 23:00:00";
            endTime = lastTimeDay + " 23:59:59";
        } else {
            startTime = lastTimeHour + ":00:00";
            endTime = lastTimeHour + ":59:59";
        }
        int limit = 1000;
        String period = lastTimeHour.replaceAll(" ", "-");
        int taskLogsByIdCount = taskLogsService.getTaskLogsCountById(taskLogCacheId, startTime, endTime);
        int page = taskLogsByIdCount/limit;
        Map<String, MachineExeTask> map = new HashMap<>();
        int lastId = 0;
        for (int i = 0; i <= page; i++) {
            List<TaskLogs> taskLogsById = taskLogsService.getTaskLogsById(taskLogCacheId, startTime, endTime, i*limit, limit);
            int lastIdTmp = setMachineExeTask( taskLogsById,  map,  period, start, endTime);
            if(lastIdTmp != 0){
                lastId = lastIdTmp;
            }
            System.out.println("i="+i+" startId="+(taskLogCacheId+i*limit)+" endId="+(taskLogCacheId+i*limit+limit)+"  lastId="+lastId);
        }
        System.out.println(map);
        for (Map.Entry<String, MachineExeTask> stringMachineExeTaskEntry : map.entrySet()) {
            String key = stringMachineExeTaskEntry.getKey();
            MachineExeTask value = stringMachineExeTaskEntry.getValue();
            String ip = key.split("###")[0];
            String taskName = key.split("###")[1];

            List<MachineExeTask> machineExeTaskByIpTaskNamePeriod = machineExeTaskService.getMachineExeTaskByIpTaskNamePeriod(ip, taskName, period);
            if(machineExeTaskByIpTaskNamePeriod == null || machineExeTaskByIpTaskNamePeriod.isEmpty()){
//                value.setTaskSuccess(value.getTaskSuccess()+value.getTaskFail());
                machineExeTaskService.insertMachineExeTask(value);
            }else {
                machineExeTaskService.updateMachineExeTask(value);
            }
        }
        WriteToFileUtil.writeIdPath("task_log_min_id",""+lastId,"./machine_exe_task_cache/");

    }

    private int setMachineExeTask(List<TaskLogs> taskLogsById, Map<String, MachineExeTask> map, String period,String start,String endTime){
        int lastId = 0;
        for (TaskLogs taskLogs : taskLogsById) {
            String remoteIp = taskLogs.getRemoteIp();
            lastId = taskLogs.getId();
            if (StringUtils.isEmpty(remoteIp)) {
                continue;
            }
            String taskName = taskLogs.getTaskName();
            String key = remoteIp + "###" + taskName;
            MachineExeTask machineExeTask = new MachineExeTask();
            if (map.containsKey(key)) {
                machineExeTask = map.get(key);
            } else {
                machineExeTask = new MachineExeTask();
                machineExeTask.setIp(remoteIp);
                machineExeTask.setTaskType(taskName);
                machineExeTask.setPeriod(period);
            }

            int event = taskLogs.getEvent();
            String createdTime = taskLogs.getCreatedTime();
            if (Event.RETURN_RESULT_SUCCESS == event) {
                int taskSuccess = machineExeTask.getTaskSuccess();
                if (taskSuccess == 0) {
                    machineExeTask.setTaskSuccess(1);
                } else {
                    machineExeTask.setTaskSuccess(taskSuccess + 1);
                }
                String taskSourceId = taskLogs.getTaskSourceId();
                List<TaskLogs> taskLogsByTaskSourceId = taskLogsService.getTaskLogsByTaskSourceId(taskSourceId, start, endTime);
                String startCreate = "";
                for (TaskLogs logs : taskLogsByTaskSourceId) {
                    if (Event.OUT_POOL == logs.getEvent() && logs.getId() < taskLogs.getId()) {
                        startCreate = logs.getCreatedTime();
                    }
                }
                if(!StringUtils.isEmpty(startCreate)){
                    long l = DateUtils.calTime(createdTime, startCreate);
                    if (machineExeTask.getTaskTotal() == 0) {
                        machineExeTask.setTotalTime(Integer.parseInt(String.valueOf(l)));
                        machineExeTask.setAvgTime(Integer.parseInt(String.valueOf(l)));
                    }else {
                        int time = machineExeTask.getTaskTotal()+Integer.parseInt(String.valueOf(l));
                        machineExeTask.setTotalTime(time);
                        machineExeTask.setAvgTime(time/machineExeTask.getTaskSuccess());
                    }
                }
            } else if (Event.RETURN_RESULT_FAILURE == event || Event.TIMEOUT_FAILURE == event) {
                int taskFail = machineExeTask.getTaskFail();
                if (taskFail == 0) {
                    machineExeTask.setTaskFail(1);
                } else {
                    machineExeTask.setTaskFail(taskFail + 1);
                }
            } else if (Event.OUT_POOL == event ) {
                int taskTotal = machineExeTask.getTaskTotal();
                if (taskTotal == 0) {
                    machineExeTask.setTaskTotal(1);
                } else {
                    machineExeTask.setTaskTotal(taskTotal + 1);
                }
            }
            map.put(key, machineExeTask);
        }
        return lastId;
    }

}
