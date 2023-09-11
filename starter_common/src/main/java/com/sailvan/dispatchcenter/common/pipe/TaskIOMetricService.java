package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.TaskIOMetric;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

public interface TaskIOMetricService {

    int insertTaskIOMetric(TaskIOMetric taskIOMetric);

    TaskIOMetric findTaskIOMetric(String taskType, String date, int hour);

    int updateTaskIOMetricById(TaskIOMetric taskIOMetric);

    TaskIOMetric sumTaskIOMetric(String date, int hour);

    PageDataResult getTaskIoMetric();
}
