package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.TaskInPoolMetric;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TaskInPoolMetricDao {

    int insertTaskInPoolMetric(TaskInPoolMetric taskInPoolMetric);

    TaskInPoolMetric findTaskInPoolMetric(String taskType, String date, int hour);

    int updateTaskInPoolMetricById(TaskInPoolMetric taskInPoolMetric);

    List<TaskInPoolMetric> getAllTaskInPoolMetric();

    TaskInPoolMetric sumTaskInPoolMetric(String date, int hour);

}
