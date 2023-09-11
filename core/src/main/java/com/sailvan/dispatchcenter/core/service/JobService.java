package com.sailvan.dispatchcenter.core.service;

import com.sailvan.dispatchcenter.core.util.QuartzUtils;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobService {

    private final Logger logger = LoggerFactory.getLogger(JobService.class);

    //注入任务调度
    @Autowired
    Scheduler scheduler;

    public void createJob(String jobName, int isSingle, String cronExpression){
        try {
            QuartzUtils.createScheduleJob(scheduler,jobName,isSingle,cronExpression);
            logger.info("创建任务[{}]成功",jobName);
        } catch (Exception e) {
            logger.error("创建任务[{}]失败---异常原因：{}",jobName,e.getMessage());
        }
    }

    /**
     * 停止任务调度
     * @param jobName
     */
    public void pauseJob(String jobName)  {
        try {
            QuartzUtils.pauseScheduleJob (scheduler,jobName);
            logger.info("暂停任务[{}]成功",jobName);
        } catch (Exception e) {
            logger.error("暂停任务[{}]失败---异常原因：{}",jobName,e.getMessage());
        }
    }

    public String  runOnce()  {
        try {
            QuartzUtils.runOnce (scheduler,"test1");
        } catch (Exception e) {
            return "运行一次失败";
        }
        return "运行一次成功";
    }

    /**
     * 恢复任务
     * @param jobName
     */
    public void resume(String jobName)  {
        try {
            QuartzUtils.resumeScheduleJob(scheduler,jobName);
            logger.info("启动任务[{}]成功",jobName);
        } catch (Exception e) {
            logger.error("启动任务[{}]失败---异常原因：{}",jobName,e.getMessage());
        }
    }

    public void update(String jobName, String cron)  {
        try {
            QuartzUtils.updateScheduleJob(scheduler,jobName,cron);
            logger.info("更新任务[{}]成功",jobName);
        }catch (Exception e) {
            createJob(jobName,0,cron);
            logger.error("更新任务[{}]失败",jobName);
        }

    }

    /**
     * 从任务调度器删除该任务
     * @param jobName 任务名
     */
    public void delete(String jobName)  {
        try {
            QuartzUtils.deleteScheduleJob(scheduler,jobName);
            logger.info("删除任务[{}]成功",jobName);
        } catch (Exception e) {
            logger.error("删除任务[{}]失败---异常原因：{}",jobName,e.getMessage());
        }
    }
}
