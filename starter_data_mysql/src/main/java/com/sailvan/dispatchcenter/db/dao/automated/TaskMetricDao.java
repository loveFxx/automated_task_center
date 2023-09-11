package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.TaskExecutedException;
import com.sailvan.dispatchcenter.common.domain.TaskMetric;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TaskMetricDao {

    int insertTaskMetric(TaskMetric taskMetric);

    int updateTaskMetric(TaskMetric taskMetric);

    TaskMetric findTaskMetric(String taskType, String date, int hour);

    List<TaskExecutedException> getTaskMetric(String date);
}
