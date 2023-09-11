package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.TaskSourceList;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

import java.text.ParseException;
import java.util.*;


public interface TaskSourceListService {


    public TaskSourceList getTaskSourceListByUniqueIdAndIsSingle(int id, int uniqueId, int isSingle);

    public PageDataResult getTaskSourceList(TaskSourceList taskSourceList, Integer pageNum, Integer pageSize
            , String startTime, String endTime, String systemIds, String taskIds,String expectedTime) ;


    public int update(TaskSourceList taskSourceList);

    public int insert(TaskSourceList taskSourceList);

    public int delete(String id,int isSingle);

    public TaskSourceList getTaskSourceListByUniqueIdAndIsSingleAndRefreshTime(int id, int uniqueId, int isSingle,String refreshTime);

    public TaskSourceList findTaskSourceById(int id, int isSingle);

    public int updateLastResultTimeById(int id, String lastResultTime,int isSingle,String taskState);

    public int updateJobNameById(String jobName, String expectedTime, int id, int isSingle);

    public List<TaskSourceList> listTaskSourcesByJobName(String jobName, int isSingle, int id);

    public int bulkUpdateTimeByIds(List<Integer> lists, String expectedTime, String lastCreateTime, int isSingle);

    int updateExpectedTimeByJobName(String expectedTime, String jobName, int id, int isSingle);

    public List<TaskSourceList> queryTaskSource(List workTypes, List taskIds, int id,int isSingle);

    public int batchUpdateJobNameById(String jobName, String expectedTime, List<Integer> ids, int isSingle);

    public int countTaskSourceByTaskId(int taskId);

    public List<Integer> listTaskSourceByTaskId(int taskId,int id,int limit);

    public List<TaskSourceList> groupByTaskSources(List<Integer> ids);

    List<Integer> listIds(int taskId);

    public List<TaskSourceList> listTaskSourceByParams(int taskId,String params);

    int batchDeleteById(int taskId, List<Integer> ids);

    int batchInsertTaskSource(List<TaskSourceList> taskSourceLists);

    String generateRefreshTimeByIntervalTime(int intervalType, int intervalTimes, String time) throws ParseException;

    int parseUniqueId(String taskName,String workType, String param);
}
