package com.sailvan.dispatchcenter.db.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sailvan.dispatchcenter.common.domain.TaskExecutedException;
import com.sailvan.dispatchcenter.common.domain.TaskIOMetric;
import com.sailvan.dispatchcenter.common.domain.TaskInPoolMetric;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.db.dao.automated.TaskIOMetricDao;

import javax.annotation.Resource;
import java.util.List;

public class TaskIOMetricService implements com.sailvan.dispatchcenter.common.pipe.TaskIOMetricService{

    @Resource
    TaskIOMetricDao taskIOMetricDao;

    @Override
    public int insertTaskIOMetric(TaskIOMetric taskIOMetric){
        return taskIOMetricDao.insertTaskIOMetric(taskIOMetric);
    }

    @Override
    public TaskIOMetric findTaskIOMetric(String taskType, String date, int hour){
        return taskIOMetricDao.findTaskIOMetric(taskType,date,hour);
    }

    @Override
    public int updateTaskIOMetricById(TaskIOMetric taskIOMetric){
        return taskIOMetricDao.updateTaskIOMetricById(taskIOMetric);
    }

    @Override
    public TaskIOMetric sumTaskIOMetric(String date, int hour){
        return taskIOMetricDao.sumTaskIOMetric(date,hour);
    }

    @Override
    public PageDataResult getTaskIoMetric() {
        String date = DateUtils.getDate();
        List<TaskExecutedException> taskExecutedExceptions
                = taskIOMetricDao.getTaskIoMetric(date);
        PageDataResult pageDataResult = new PageDataResult();
        if (taskExecutedExceptions.size() != 0) {
            PageInfo<TaskExecutedException> pageInfo = new PageInfo<>(taskExecutedExceptions);
            pageDataResult.setList(taskExecutedExceptions);
        }
        return pageDataResult;
    }
}
