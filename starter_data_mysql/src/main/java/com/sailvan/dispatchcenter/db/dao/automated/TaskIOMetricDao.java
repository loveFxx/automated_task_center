package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.TaskExecutedException;
import com.sailvan.dispatchcenter.common.domain.TaskIOMetric;
import com.sailvan.dispatchcenter.common.domain.TaskInPoolMetric;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TaskIOMetricDao {

    int insertTaskIOMetric(TaskIOMetric taskIOMetric);

    TaskIOMetric findTaskIOMetric(String taskType, String date, int hour);

    int updateTaskIOMetricById(TaskIOMetric taskIOMetric);

    TaskIOMetric sumTaskIOMetric(String date, int hour);

    List<TaskExecutedException> getTaskIoMetric(String date);
}
