package com.sailvan.dispatchcenter.shard.dao;

import com.sailvan.dispatchcenter.common.domain.TaskSourceList;
import com.sailvan.dispatchcenter.db.dao.automated.TaskSourceListDao;
import com.sailvan.dispatchcenter.shard.config.MysqlShardMarkerConfiguration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author mh
 * @date 21-10
 */
@Primary
@Mapper
@ConditionalOnBean(MysqlShardMarkerConfiguration.MysqlShardMarker.class)
public interface TaskSourceListShardDao extends TaskSourceListDao {

    @Override
    int insertTaskSourceList(TaskSourceList taskSourceList);

    @Override
    List<TaskSourceList> getTaskSrcByTaskSrc(@Param("taskSourceList") TaskSourceList taskSourceList
            , @Param("startTime") String startTime, @Param("endTime") String endTime
            , @Param("systemIds") String systemIds, @Param("taskIds") String taskIds
            , @Param("isSingle") int isSingle, @Param("params") String params
            , @Param("endId") int endId,@Param("expectedTime")String expectedTime
            , @Param("startId") int startId,@Param("createdTime")String createdTime);

    @Override
    int update(TaskSourceList taskSourceList);

    @Override
    int delete(int id, int isSingle);

    @Override
    TaskSourceList getTaskSourceListByUniqueIdAndIsSingle(int id, int uniqueId, int isSingle);

    @Override
    TaskSourceList getTaskSourceListByUniqueIdAndIsSingleAndRefreshTime(int id, int uniqueId, int isSingle, String refreshTime);

    @Override
    int updateLastResultTimeById(int id, String lastResultTime, int isSingle,String taskState);

    @Override
    int updateJobNameById(String jobName, String expectedTime, int id,int isSingle);

    @Override
    List<TaskSourceList> listTaskSourcesByJobName(String jobName, int isSingle, int id);

    @Override
    int bulkUpdateTimeByIds(List<Integer> lists, String expectedTime, String lastCreateTime, int isSingle);

    @Override
    int updateExpectedTimeByJobName(String expectedTime, String jobName, int id, int isSingle);

    @Override
    TaskSourceList findTaskSourceById(int id, int isSingle);


    @Override
    List<TaskSourceList> getDefaultTaskById(int id,int isSingle,int endId,String expectedTime);

    @Override
    List<TaskSourceList> queryTaskSource(List workTypes, List taskIds, int id,int isSingle);


    HashSet<String> getAccountSiteAll();


    HashSet<String> getAccountAll();


    /**
     * 没有可用机器但却有任务库(work_type为帐号_大洲)的店铺
     * @return 店铺的work_type
     */
    List<String> getAccountHavingNoMachineButTask();


    int countAccountSite();

    @Override
    int batchInsertTaskSource(List<TaskSourceList> taskSourceLists);

    @Override
    int batchUpdateJobNameById(String jobName, String expectedTime, List<Integer> ids, int isSingle);

    @Override
    int countTaskSourceByTaskId(int taskId);

    @Override
    List<Integer> listTaskSourceByTaskId(int taskId,int id,int limit);

    @Override
    List<TaskSourceList> groupByTaskSources(@Param("ids") List<Integer> ids);

    @Override
    List<Integer> listIds(int taskId);

    @Override
    List<TaskSourceList> listTaskSourceByParams(int taskId,String params);

    @Override
    int batchDeleteById(int taskId, List<Integer> ids);
}
