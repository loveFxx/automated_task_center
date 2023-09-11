package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.TaskSourceList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskSourceListDao {


    int insertTaskSourceList(TaskSourceList taskSourceList);

    List<TaskSourceList> getTaskSrcByTaskSrc(@Param("taskSourceList") TaskSourceList taskSourceList
            , @Param("startTime") String startTime, @Param("endTime") String endTime
            , @Param("systemIds") String systemIds, @Param("taskIds") String taskIds
            , @Param("isSingle") int isSingle, @Param("params") String params
            , @Param("endId") int endId,@Param("expectedTime")String expectedTime
            , @Param("startId") int startId,@Param("createdTime")String createdTime);


    int update(TaskSourceList taskSourceList);

    int delete(int id,int isSingle);

    TaskSourceList getTaskSourceListByUniqueIdAndIsSingle(int id, int uniqueId, int isSingle);

    TaskSourceList getTaskSourceListByUniqueIdAndIsSingleAndRefreshTime(int id, int uniqueId, int isSingle, String refreshTime);

    int updateLastResultTimeById(int id, String lastResultTime, int isSingle,String taskState);

    int updateJobNameById(String jobName, String expectedTime, int id,int isSingle);

    List<TaskSourceList> listTaskSourcesByJobName(String jobName, int isSingle, int id);

    int bulkUpdateTimeByIds(List<Integer> lists, String expectedTime, String lastCreateTime, int isSingle);

    int updateExpectedTimeByJobName(String expectedTime, String jobName, int id, int isSingle);

    TaskSourceList findTaskSourceById(int id, int isSingle);

    //默认查询
    List<TaskSourceList> getDefaultTaskById(int id ,int isSingle,int endId,String expectedTime);

    //账号洲 任务id查询
    List<TaskSourceList> queryTaskSource(List workTypes, List taskIds, int id,int isSingle);

    int batchInsertTaskSource(List<TaskSourceList> taskSourceLists);

    int batchUpdateJobNameById(String jobName, String expectedTime, List<Integer> ids, int isSingle);

    int countTaskSourceByTaskId(int taskId);

    List<Integer> listTaskSourceByTaskId(int taskId,int id,int limit);

    List<TaskSourceList> groupByTaskSources(List<Integer> ids);

    List<Integer> listIds(int taskId);

    List<TaskSourceList> listTaskSourceByParams(int taskId,String params);

    int batchDeleteById(int taskId, List<Integer> ids);
}
