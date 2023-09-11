package com.sailvan.dispatchcenter.common.pipe;

import com.alibaba.fastjson.JSONArray;
import com.sailvan.dispatchcenter.common.domain.TaskExecutedException;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

public interface TaskExecutedExceptionService {

    int insertTaskExecutedException(TaskExecutedException taskExecutedException);

    TaskExecutedException findTaskExecutedException(String taskType, String date, int hour);

    int updateTaskExecutedException(TaskExecutedException taskExecutedException);

    PageDataResult getTaskExecutedException(int pageNum,int pageSize,String taskType);

    JSONArray getFailTaskType();

    int deleteTaskExecutedException(String datetime);
}
