package com.sailvan.dispatchcenter.db.service;

import com.github.pagehelper.PageInfo;
import com.sailvan.dispatchcenter.common.domain.TaskExecutedException;
import com.sailvan.dispatchcenter.common.domain.TaskMetric;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.db.dao.automated.TaskMetricDao;

import javax.annotation.Resource;
import java.util.List;

public class TaskMetricService implements com.sailvan.dispatchcenter.common.pipe.TaskMetricService{

    @Resource
    TaskMetricDao taskMetricDao;

    @Override
    public int insertTaskMetric(TaskMetric taskMetric) {
        return taskMetricDao.insertTaskMetric(taskMetric);
    }

    @Override
    public int updateTaskMetric(TaskMetric taskMetric) {
        return taskMetricDao.updateTaskMetric(taskMetric);
    }

    @Override
    public TaskMetric findTaskMetric(String taskType, String date, int hour) {
        return taskMetricDao.findTaskMetric(taskType, date, hour);
    }

    @Override
    public PageDataResult getTaskMetric() {

        String date = DateUtils.getDate();
        List<TaskExecutedException> taskExecutedExceptions
                = taskMetricDao.getTaskMetric(date);
        PageDataResult pageDataResult = new PageDataResult();
        if (taskExecutedExceptions.size() != 0) {
            pageDataResult.setList(taskExecutedExceptions);
        }
        return pageDataResult;

    }
}
