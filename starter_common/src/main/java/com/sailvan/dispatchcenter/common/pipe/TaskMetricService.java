package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.TaskMetric;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

public interface TaskMetricService {

    int insertTaskMetric(TaskMetric taskMetric);

    int updateTaskMetric(TaskMetric taskMetric);

    TaskMetric findTaskMetric(String taskType, String date, int hour);

    PageDataResult getTaskMetric();
}
