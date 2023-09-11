package com.sailvan.dispatchcenter.db.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sailvan.dispatchcenter.common.domain.TaskInPoolMetric;
import com.sailvan.dispatchcenter.common.domain.TaskSourceList;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.db.dao.automated.TaskInPoolMetricDao;

import javax.annotation.Resource;
import java.util.List;

public class TaskInPoolMetricService implements com.sailvan.dispatchcenter.common.pipe.TaskInPoolMetricService{

    @Resource
    TaskInPoolMetricDao taskInPoolMetricDao;

    @Override
    public int insertTaskInPoolMetric(TaskInPoolMetric taskInPoolMetric){
        return taskInPoolMetricDao.insertTaskInPoolMetric(taskInPoolMetric);
    }

    @Override
    public TaskInPoolMetric findTaskInPoolMetric(String taskType, String date, int hour){
        return taskInPoolMetricDao.findTaskInPoolMetric(taskType,date,hour);
    }

    @Override
    public int updateTaskInPoolMetricById(TaskInPoolMetric taskInPoolMetric){
        return taskInPoolMetricDao.updateTaskInPoolMetricById(taskInPoolMetric);
    }

    @Override

    public PageDataResult getAllTaskInPoolMetric() {

        List<TaskInPoolMetric> taskInPoolMetrics = taskInPoolMetricDao.getAllTaskInPoolMetric();
        PageDataResult pageDataResult = new PageDataResult();
        if (taskInPoolMetrics.size() != 0) {
            PageInfo<TaskInPoolMetric> pageInfo = new PageInfo<>(taskInPoolMetrics);
            pageDataResult.setList(taskInPoolMetrics);
            pageDataResult.setTotals((int) pageInfo.getTotal());

        }
        return pageDataResult;
    }


    @Override
    public TaskInPoolMetric sumTaskInPoolMetric(String date, int hour){
        return taskInPoolMetricDao.sumTaskInPoolMetric(date,hour);

    }
}
