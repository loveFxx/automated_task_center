package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.TaskInPoolMetric;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

public interface TaskInPoolMetricService {

    int insertTaskInPoolMetric(TaskInPoolMetric taskInPoolMetric);

    TaskInPoolMetric findTaskInPoolMetric(String taskType, String date, int hour);

    int updateTaskInPoolMetricById(TaskInPoolMetric taskInPoolMetric);


    PageDataResult getAllTaskInPoolMetric();

    TaskInPoolMetric sumTaskInPoolMetric(String date, int hour);

}
