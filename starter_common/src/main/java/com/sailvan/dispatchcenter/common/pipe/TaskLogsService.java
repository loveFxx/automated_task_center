package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.TaskLogs;
import com.sailvan.dispatchcenter.common.domain.TaskMetadata;
import com.sailvan.dispatchcenter.common.domain.TaskResult;
import com.sailvan.dispatchcenter.common.domain.TaskSourceList;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

import java.util.ArrayList;
import java.util.List;

public interface TaskLogsService {

    public int insertTaskLogs(TaskLogs taskLogs);

    public PageDataResult getTaskLogsList(TaskLogs taskLogs, Integer pageNum, Integer pageSize, String startTime, String endTime) ;

    public List<TaskLogs> getTaskLogsById(int id, String startTime, String endTime, int offset, int limit);

    public int getTaskLogsCountById(int id, String startTime,String endTime);

    public List<TaskLogs> getTaskLogsByTaskSourceId(String taskSourceId, String startTime, String endTime);

    List<TaskLogs> getTaskLogsListInOneHour(int startId, String startTime, String endTime, int offset, int limit);

}
