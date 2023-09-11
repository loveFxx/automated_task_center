package com.sailvan.dispatchcenter.stat.monitor.scheduler;

import com.sailvan.dispatchcenter.common.constant.Event;
import com.sailvan.dispatchcenter.common.domain.AccountExeTask;
import com.sailvan.dispatchcenter.common.domain.TaskLogs;
import com.sailvan.dispatchcenter.common.pipe.EveryDayMaxSingleIdService;
import com.sailvan.dispatchcenter.common.pipe.TaskLogsService;
import com.sailvan.dispatchcenter.common.pipe.TaskSourceListService;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.TaskUtil;
import com.sailvan.dispatchcenter.common.util.WriteToFileUtil;
import com.sailvan.dispatchcenter.db.service.AccountExeTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 店铺每小时执行任务
 *
 * @author yyj
 * @date 2021-12
 */
@Component
public class AccountExeTaskScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ProxyIPMonitorScheduler.class);

    @Autowired
    TaskLogsService taskLogsService;

    @Autowired
    TaskUtil taskUtil;


    @Autowired
    TaskSourceListService taskSourceListService;

    @Autowired
    AccountExeTaskService accountExeTaskService;

    @Autowired
    EveryDayMaxSingleIdService everyDayMaxSingleIdService;



    @Scheduled(cron = "0 0 * * * ?")
    public  void recordEveryHourAccountTask() throws Exception{
        logger.info("recordEveryHourAccountTask start...");
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfHour = new SimpleDateFormat("yyyy-MM-dd-HH");
        //Date date = sd.parse("2022-03-05 12:00:00");
        String currentDate = sd.format(date);
        String startTime = "";
        String endTime = "";
        int startId = taskUtil.getIdCache("task_log_min_id","./machine_exe_task_cache/");
        String period ="";
        //若时间是今天0点 修改开始时间为昨天23点 结束时间为今天0点 设置开始ID为0
        if (currentDate.contains("00:00:00")){
            Date yesterday = DateUtils.minusDay(1, date);
            startTime = sdf.format(yesterday);
            period = startTime +"-23";
            startTime = startTime + " 23:00:00";
            endTime = currentDate;
        }else {
            //若时间不是今天0点 修改开始时间为当前时间一个小时之前 结束时间为当前时间
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(date);
            rightNow.add(Calendar.HOUR, -1);
            Date dt1=rightNow.getTime();
            period = sdfHour.format(dt1);
            startTime = sd.format(dt1);
            endTime = currentDate;
            if (currentDate.contains("01:00:00")){
                startId = 0;
            }
        }
        //查询
        int limit = 1000;
        int taskLogsByIdCount = taskLogsService.getTaskLogsCountById(startId, startTime, endTime);
        int page = taskLogsByIdCount/limit;
        int lastId = 0;
        Map<String, AccountExeTask> map = new HashMap<>();
        for (int i = 0; i <= page; i++) {
            List<TaskLogs> taskLogsById = taskLogsService.getTaskLogsListInOneHour(startId, startTime, endTime, i*limit, limit);
            int lastIdTmp = setAccountExeTask(taskLogsById, map, period);
            if(lastIdTmp != 0){
                lastId = lastIdTmp;
            }
        }
        //存入前一小时最大ID
        WriteToFileUtil.writeIdPath("task_log_min_id",""+lastId,"./machine_exe_task_cache/");
        for (Map.Entry<String, AccountExeTask> accountExeTaskEntry : map.entrySet()) {
            String key = accountExeTaskEntry.getKey();
            AccountExeTask value = accountExeTaskEntry.getValue();
            accountExeTaskService.insertAccountExeTask(value);
        }
        logger.info("recordEveryHourAccountTask over...");

    }


    private int setAccountExeTask(List<TaskLogs> taskLogsById, Map<String, AccountExeTask> map, String period){
        int lastId = 0;
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createdTime = sd.format(new Date());
        for (TaskLogs taskLog : taskLogsById) {
            lastId =taskLog.getId();
            String taskName = taskLog.getTaskName();
            String account = taskLog.getAccount();
            String continent = taskLog.getContinent();
            String key = account+"-"+continent+"-"+taskName;
            AccountExeTask accountExeTask = new AccountExeTask();
            if (map.containsKey(key)) {
                accountExeTask = map.get(key);
            } else {
                accountExeTask = new AccountExeTask();
                accountExeTask.setAccount(account);
                accountExeTask.setContinent(continent);
                accountExeTask.setTaskType(taskName);
                accountExeTask.setPeriod(period);
                accountExeTask.setCreatedTime(createdTime);
            }
            int event = taskLog.getEvent();
            if (Event.IN_POOL == event || Event.MANUAL_IN_POOL == event){
                int taskInPool = accountExeTask.getTaskInPool();
                accountExeTask.setTaskInPool(taskInPool+1);
            }else if (Event.OUT_POOL == event){
                int taskGet = accountExeTask.getTaskGet();
                accountExeTask.setTaskGet(taskGet+1);
            }else if (Event.RETURN_RESULT_SUCCESS == event){
                int taskSuccess = accountExeTask.getTaskSuccess();
                accountExeTask.setTaskSuccess(taskSuccess+1);
            }else if (Event.RETURN_RESULT_FAILURE == event || Event.TIMEOUT_FAILURE == event){
                int taskFail = accountExeTask.getTaskFail();
                accountExeTask.setTaskFail(taskFail+1);
            }
            map.put(key, accountExeTask);
        }
        return lastId;
    }


}
