package com.sailvan.dispatchcenter.stat.monitor.scheduler;


import com.sailvan.dispatchcenter.common.domain.TaskMetric;
import com.sailvan.dispatchcenter.common.domain.TaskStatDomain;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.ExcelUtils;
import com.sailvan.dispatchcenter.db.dao.automated.TaskLogsDao;
import com.sailvan.dispatchcenter.db.dao.automated.TaskMetricDao;
import com.sailvan.dispatchcenter.stat.monitor.config.WeChatRobotTokenConfig;
import com.sailvan.dispatchcenter.stat.monitor.statistics.TaskLogStatEs;
import com.sailvan.dispatchcenter.stat.monitor.util.WeChatRobotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @program: automated_task_center
 * @description: 任务成功率每日统计
 * @author: Wu Xingjian
 * @create: 2021-10-08 11:07
 **/
@Component
public class TaskSuccessStatScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TaskSuccessStatScheduler.class);

    String firstDayStart = "2021-11-12 00:00:00";
    String firstDayEnd = "2021-11-12 23:59:59";


    String secondDayStart = "2021-11-13 00:00:00";
    String secondDayEnd = "2021-11-13 23:59:59";

    @Autowired
    TaskLogsDao taskLogsDao;


    @Autowired
    WeChatRobotTokenConfig wechatRobotTokenConfig;

    @Resource
    TaskLogStatEs taskLogStatEs;


    @Autowired
    private ExcelUtils excelUtils;

    @Autowired
    TaskMetricDao taskMetricDao;


    @Scheduled(cron = "0 0 9,15,23 * * ?")
    public void sendTaskStatToWechatGroup() throws IOException, InterruptedException, ParseException {
        Calendar cal = Calendar.getInstance();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());

        Map<String, String> startEndOfDay = DateUtils.getStartEndOfDay(today);
        String oneDayStart = startEndOfDay.get("oneDayStart");
        String oneDayEnd = startEndOfDay.get("oneDayEnd");
        String secondDayStart = startEndOfDay.get("secondDayStart");
        String secondDayEnd = startEndOfDay.get("secondDayEnd");

        Map<String, TaskStatDomain> res = taskLogStatEs.getTaskSuccessMap(oneDayStart, oneDayEnd, secondDayStart, secondDayEnd);
        WeChatRobotUtils weChatRobotUtils = new WeChatRobotUtils(wechatRobotTokenConfig.getWechatRobotToken());

        StringBuilder md = new StringBuilder();
        md.append("<font color=\"comment\">");
        md.append(today);
        md.append("</font>**任务成功率**\n");

        md.append(weChatRobotUtils.appendSpace("taskName",60));
        md.append(weChatRobotUtils.appendSpace("total",20));
        md.append(weChatRobotUtils.appendSpace("success",20));
        md.append(weChatRobotUtils.appendSpace("forcedFailure",20));
        md.append(weChatRobotUtils.appendSpace("failure",20));
        md.append(weChatRobotUtils.appendSpace("unExecuted",20));
        md.append(weChatRobotUtils.appendSpace("successRate",20)).append("\n");
        int count = 0;
        for (Map.Entry<String, TaskStatDomain> entry : res.entrySet()) {
            double value = (double) entry.getValue().getSuccessCount() / entry.getValue().getTotalCount();
            DecimalFormat df = new DecimalFormat("0.00%");
            String percent = df.format(value);
            if (value<0.5){
                md.append("<font color=\"warning\">");
            }
            if (value >= 0.5 && value < 0.9){
                md.append("<font color=\"info\">");
            }

            md.append(weChatRobotUtils.appendSpace(entry.getKey(),60));
            md.append(weChatRobotUtils.appendSpace(String.valueOf(entry.getValue().getTotalCount()),20));
            md.append(weChatRobotUtils.appendSpace(String.valueOf(entry.getValue().getSuccessCount()),20));
            md.append(weChatRobotUtils.appendSpace(String.valueOf(entry.getValue().getForceFailedCount()),20));
            md.append(weChatRobotUtils.appendSpace(String.valueOf(entry.getValue().getFailedCount()),20));
            md.append(weChatRobotUtils.appendSpace(String.valueOf(entry.getValue().getUnExecutedCount()),20));

            md.append(weChatRobotUtils.appendSpace(percent,20));
            if (value < 0.9){
                md.append("</font>");
            }
            md.append("\n");
            count++;
            if (count %15==0){
                weChatRobotUtils.sendMarkdown(md.toString());
                md = new StringBuilder();
            }
        }

        if (md.length()>0){
            weChatRobotUtils.sendMarkdown(md.toString());
        }

        logger.info("sendTaskStatToWechatGroup over...");
    }

    @Scheduled(cron = "0 0 */1 * * ?")
    public void taskStatistics() throws ParseException, IOException, InterruptedException {
        logger.info("taskStatistics start...");
        String date = DateUtils.getDate();
        Map<String, String> startEndOfDay = DateUtils.getStartEndOfDay(date);
        String oneDayStart = startEndOfDay.get("oneDayStart");
        String oneDayEnd = startEndOfDay.get("oneDayEnd");
        String secondDayStart = startEndOfDay.get("secondDayStart");
        String secondDayEnd = startEndOfDay.get("secondDayEnd");

        int hour = DateUtils.getCurrentHour();

        Map<String, TaskStatDomain> res = taskLogStatEs.getTaskSuccessMap(oneDayStart, oneDayEnd, secondDayStart, secondDayEnd);

        for (Map.Entry<String, TaskStatDomain> entry : res.entrySet()) {
            TaskMetric taskMetric = new TaskMetric();
            taskMetric.setTaskType(entry.getKey());
            taskMetric.setGeneratedNum(entry.getValue().getTotalCount());
            taskMetric.setTotalSucceedNum(entry.getValue().getTotalSuccessCount());
            taskMetric.setSucceedNum(entry.getValue().getSuccessCount());
            taskMetric.setFileNum(entry.getValue().getFileCount());
            taskMetric.setFailedNum(entry.getValue().getFailedCount() + entry.getValue().getForceFailedCount());
            taskMetric.setUnExecutedNum(entry.getValue().getUnExecutedCount());
            taskMetric.setDate(date);
            taskMetric.setHour(hour);
            TaskMetric taskMetricInfo = taskMetricDao.findTaskMetric(entry.getKey(), date, hour);
            if (taskMetricInfo == null) {
                taskMetricDao.insertTaskMetric(taskMetric);
            } else {
                taskMetric.setId(taskMetricInfo.getId());
                taskMetricDao.updateTaskMetric(taskMetric);
            }
        }
        logger.info("taskStatistics start...");
    }

}

