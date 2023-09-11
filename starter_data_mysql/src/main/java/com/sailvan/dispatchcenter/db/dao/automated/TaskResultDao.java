package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.TaskResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface TaskResultDao {

    List<TaskResult> listTaskResultByTaskSourceId(String taskSourceId,int id);

    List<TaskResult> listTaskResultByUniqueIdOrderByCreatedTime(int uniqueId,int id);

    List<TaskResult> listTaskResultByResultHashKey(int resultHashKey,int id);

    int insertTaskResult(TaskResult taskResult);

    TaskResult getTaskResultByTaskSourceIdAndResultHashKeyAndRefreshTime(int id, String taskSourceId, int resultHashKey, String refreshTime);

    int updateTaskResult(TaskResult taskResult);

    TaskResult findById(int id);

    List<TaskResult> listTaskResultByTaskSourceIdRange(String taskSourceId,int smallestId,int biggestId);

    List<TaskResult> listTaskResultByUniqueIdRange(int uniqueId,int smallestId,int biggestId);


    List<TaskResult> getFirstTaskResultByTaskResult(int maxId, @Param("taskResult") TaskResult taskResult);


    List<TaskResult> getNextTaskResultByTaskResult(int curPageMinId, @Param("taskResult") TaskResult taskResult);


    List<TaskResult> getLastTaskResultByTaskResult(int curPageMaxId, @Param("taskResult") TaskResult taskResult);

    int getTaskResultCount(int maxId,@Param("taskResult") TaskResult taskResult);

    List<TaskResult> listErrorResult(String createdTime, String taskType, int offset, int limit);

    int countErrorResult(String createdTime, String taskType);

    List<Map<String,Object>> countDistinctErrorResult(String createdTime, String taskType, int id);

    int countTaskResultByType(String type,int minResultId,int maxResultId);

    List<Integer> listResultIds(String type,int minResultId,int maxResultId,String oldTaskSourceId);

    int fixTaskResult(List<Integer> ids,int uniqueId,String newTaskSourceId);

    List<TaskResult> listTaskResultByTime(String type,int minResultId,int maxResultId,String minTime, String maxTime);
}
