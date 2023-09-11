package com.sailvan.dispatchcenter.db.dao.automated;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.domain.TaskExecutedException;
import com.sailvan.dispatchcenter.common.domain.TaskInPoolMetric;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface TaskExecutedExceptionDao {

    int insertTaskExecutedException(TaskExecutedException taskExecutedException);

    TaskExecutedException findTaskExecutedException(String taskType, String date, int hour);

    int updateTaskExecutedException(TaskExecutedException taskExecutedException);

    List<TaskExecutedException> getTaskExecutedException(String date,String taskType);

    List<Map> getFailTaskType();

    int deleteTaskExecutedException(String datetime);
}
