package com.sailvan.dispatchcenter.core.scheduler;

import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.Task;
import com.sailvan.dispatchcenter.common.domain.TaskExecutedException;
import com.sailvan.dispatchcenter.common.domain.TaskIOMetric;
import com.sailvan.dispatchcenter.common.domain.TaskInPoolMetric;
import com.sailvan.dispatchcenter.common.pipe.*;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.TaskUtil;
import com.sailvan.dispatchcenter.core.service.TaskBufferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TaskInPoolMetricScheduler {

    @Autowired
    TaskBufferService taskBufferService;

    @Autowired
    TaskService taskService;

    @Autowired
    TaskInPoolMetricService taskInPoolMetricService;

    @Autowired
    TaskResultService taskResultService;

    @Autowired
    TaskUtil taskUtil;

    @Autowired
    TaskExecutedExceptionService taskExecutedExceptionService;

    @Autowired
    TaskIOMetricService taskIOMetricService;

    @Scheduled(cron = "0 10 */1 * * ?")
    public void pool(){
        Date currentDate = DateUtils.getCurrentDateToDate();
        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");
        String timeShort = dateFormat2.format(currentDate);
        StringTokenizer st = new StringTokenizer(timeShort, ":");
        List<String> inTime = new ArrayList<String>();
        while (st.hasMoreElements()) {
            inTime.add(st.nextToken());
        }
        int hour = Integer.parseInt(inTime.get(0));
        String date = dateFormat1.format(currentDate);
        //
        String oneHourBefore = DateUtils.getHourBeforeDate(1);

        String threeHourBefore = DateUtils.getHourBeforeDate(3);

        String fiveHourBefore = DateUtils.getHourBeforeDate(5);

        String tenHourBefore = DateUtils.getHourBeforeDate(10);

        String oneDayBefore = DateUtils.getHourBeforeDate(24);

        List<Task> tasks = taskService.listTask();
        for (Task task : tasks) {
            if (task.getStatus() == 0){
                continue;
            }

            int bufferNum = taskBufferService.countInBufferTask(task.getTaskName());

            int oneHourNum = taskBufferService.countInPoolTasksBefore(task.getTaskName(),oneHourBefore);

            int oneToThreeNum = taskBufferService.countInPoolTasks(task.getTaskName(),threeHourBefore,oneHourBefore);

            int threeToFiveNum = taskBufferService.countInPoolTasks(task.getTaskName(),fiveHourBefore,threeHourBefore);

            int fiveToTenNum = taskBufferService.countInPoolTasks(task.getTaskName(),tenHourBefore,fiveHourBefore);

            int tenToOneDayNum = taskBufferService.countInPoolTasks(task.getTaskName(),oneDayBefore,tenHourBefore);

            int overOneDayNum = taskBufferService.countInPoolTasksAfter(task.getTaskName(),oneDayBefore);
            TaskInPoolMetric taskInPoolMetric = new TaskInPoolMetric();
            taskInPoolMetric.setTaskType(task.getTaskName());
            taskInPoolMetric.setBufferNum(bufferNum);
            taskInPoolMetric.setOneHourNum(oneHourNum);
            taskInPoolMetric.setOneToThreeNum(oneToThreeNum);
            taskInPoolMetric.setThreeToFiveNum(threeToFiveNum);
            taskInPoolMetric.setFiveToTenNum(fiveToTenNum);
            taskInPoolMetric.setTenToOneDayNum(tenToOneDayNum);
            taskInPoolMetric.setOverOneDayNum(overOneDayNum);
            taskInPoolMetric.setDate(date);
            taskInPoolMetric.setHour(hour);
            TaskInPoolMetric taskInPoolMetricInfo = taskInPoolMetricService.findTaskInPoolMetric(task.getTaskName(), date, hour);
            if (taskInPoolMetricInfo == null){
                taskInPoolMetricService.insertTaskInPoolMetric(taskInPoolMetric);
            }else {
                taskInPoolMetric.setId(taskInPoolMetricInfo.getId());
                taskInPoolMetricService.updateTaskInPoolMetricById(taskInPoolMetric);
            }
        }
        TaskInPoolMetric taskInPoolMetric = taskInPoolMetricService.sumTaskInPoolMetric(date, hour);
        taskInPoolMetric.setTaskType("sum");
        TaskInPoolMetric taskInPoolMetricInfo = taskInPoolMetricService.findTaskInPoolMetric("sum", date, hour);
        if (taskInPoolMetricInfo == null){
            taskInPoolMetricService.insertTaskInPoolMetric(taskInPoolMetric);
        }else {
            taskInPoolMetric.setId(taskInPoolMetricInfo.getId());
            taskInPoolMetricService.updateTaskInPoolMetricById(taskInPoolMetric);
        }
    }

//    @Scheduled(cron = "0 5 */1 * * ?")
//    public void error(){
//
//        String date = DateUtils.getDate();
//        int hour = DateUtils.getCurrentHour();
//        List<Task> tasks = taskService.listTask();
//        for (Task task : tasks) {
//            if (task.getStatus() == 0){
//                continue;
//            }
//            int id = taskUtil.getTaskResultSearchSmallestId();
//            List<Map<String, Object>> maps = taskResultService.countDistinctErrorResult(date, task.getTaskName(),id);
//
//            for (Map<String, Object> map : maps) {
//                TaskExecutedException taskExecutedException = new TaskExecutedException();
//                taskExecutedException.setTaskType(task.getTaskName());
//                taskExecutedException.setError(String.valueOf(map.get("client_error")));
//                taskExecutedException.setNum(Integer.parseInt(String.valueOf(map.get("num"))));
//                taskExecutedException.setDate(date);
//                taskExecutedException.setHour(hour);
//                taskExecutedExceptionService.insertTaskExecutedException(taskExecutedException);
//            }
//        }
//    }

    /**
     * 清除任务失败记录一周前的数据
     */
    @Scheduled(cron = "0 30 1 * * ?")
    public void deleteTaskExecutedException(){
        Date date = new Date();
        Date date1 = DateUtils.minusDay(7,date);
        String formatTime1 = DateUtils.getFormatTime(date1, "yyyy-MM-dd");
        taskExecutedExceptionService.deleteTaskExecutedException(formatTime1);
    }
}
