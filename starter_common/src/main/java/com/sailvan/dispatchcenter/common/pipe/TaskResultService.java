package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.TaskResult;
import com.sailvan.dispatchcenter.common.domain.TaskSourceList;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public interface TaskResultService {


    /**
     * 获取任务结果
     * @param pageNum 页数
     * @param pageSize 每页大小
     * @param taskSourceId 任务库id
     * @return 任务结果分页列表
     */
    public Object listTaskResultByTaskSourceId(Integer pageNum, Integer pageSize, String taskSourceId, Object pageDataResult,String startDate,String endDate) ;


    /**
     * 返回任务结果判重的
     * @param uniqueId
     * @param centerParams 中心端生成参数
     * @return 任务结果判重hash key
     */
    public int parseResultHashKey(int uniqueId, LinkedHashMap centerParams);

    public List<TaskResult> listTaskResultByResultHashKey(int resultHashKey, int id);

    public boolean isRepeatResult(int resultHashKey, String refreshTime) throws ParseException ;

    public int insertTaskResult(TaskResult taskResult);

    public TaskResult getTaskResultByTaskSourceIdAndResultHashKeyAndRefreshTime(int id, String taskSourceId, int resultHashKey, String refreshTime);

    public int updateTaskResult(TaskResult taskResult);

    public TaskResult findById(int id);

    List<TaskResult> listTaskResultByTaskSourceIdRange(String taskSourceId,int smallestId,int biggestId);

    List<TaskResult> listTaskResultByUniqueIdRange(int uniqueId,int smallestId,int biggestId);



    /**
     * taskResultList和总数分别返回 避免每次翻页重新查询总数
     * @param taskResult
     * @return
     */
    public int getTaskResultCount(TaskResult taskResult);

    /**
     * 获取错误结果
     * @param createdTime
     * @param taskType
     * @return
     */
    List<TaskResult> listErrorResult(String createdTime, String taskType, int offset, int limit);

    int countErrorResult(String createdTime, String taskType);

    List<Map<String,Object>> countDistinctErrorResult(String createdTime, String taskType,int id);

    int countTaskResultByType(String type,int minResultId,int maxResultId);

    List<Integer> listResultIds(String type,int minResultId,int maxResultId,String oldTaskSourceId);

    int fixTaskResult(List<Integer> ids,int uniqueId,String newTaskSourceId);

    List<TaskResult> listTaskResultByTime(String type,int minResultId,int maxResultId,String minTime, String maxTime);
}
