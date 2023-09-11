package com.sailvan.dispatchcenter.shard.dao;

import com.sailvan.dispatchcenter.common.domain.TaskResult;
import com.sailvan.dispatchcenter.db.dao.automated.TaskResultDao;
import com.sailvan.dispatchcenter.shard.config.MysqlShardMarkerConfiguration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * @author mh
 * @date 21-10
 *
 */
@Primary
@Mapper
@ConditionalOnBean(MysqlShardMarkerConfiguration.MysqlShardMarker.class)
public interface TaskResultShardDao extends TaskResultDao {

    @Override
    List<TaskResult> listTaskResultByTaskSourceId(String taskSourceId, int id);

    @Override
    List<TaskResult> listTaskResultByUniqueIdOrderByCreatedTime(int uniqueId, int id);

    @Override
    List<TaskResult> listTaskResultByResultHashKey(int resultHashKey,int id);

    @Override
    int insertTaskResult(TaskResult taskResult);

    @Override
    TaskResult getTaskResultByTaskSourceIdAndResultHashKeyAndRefreshTime(@Param("id") int id,@Param("taskSourceId") String taskSourceId, @Param("resultHashKey") int resultHashKey, @Param("refreshTime") String refreshTime);

    @Override
    int updateTaskResult(TaskResult taskResult);

    @Override
    TaskResult findById(int id);

    @Override
    List<TaskResult> listTaskResultByTaskSourceIdRange(String taskSourceId,int smallestId,int biggestId);

    @Override
    List<TaskResult> listTaskResultByUniqueIdRange(int uniqueId,int smallestId,int biggestId);


    /**
     *  用了 FORCE INDEX
     */
    @Override
    List<TaskResult> getFirstTaskResultByTaskResult(int maxId, @Param("taskResult") TaskResult taskResult);

    /**
     *
     * 当前页30～21 跳下一页20～21加order by id desc
     * select * from atc_task_result_1 where id <21  order by id desc limit 10
     *  用了 FORCE INDEX
     */
    @Override
    List<TaskResult> getNextTaskResultByTaskResult(int curPageMinId, @Param("taskResult") TaskResult taskResult);


    /**
     *
     * 当前页30～21 跳上一页40～31不加order by id desc
     * select * from atc_task_result_1 where id >30 limit 10
     × 用了 FORCE INDEX
     */
    @Override
    List<TaskResult> getLastTaskResultByTaskResult(int curPageMaxId, @Param("taskResult") TaskResult taskResult);

    @Override
    int getTaskResultCount(int maxId,@Param("taskResult") TaskResult taskResult);

    @Override
    int countErrorResult(String createdTime, String taskType);

    @Override
    int countTaskResultByType(String type,int minResultId,int maxResultId);

    @Override
    List<Integer> listResultIds(String type,int minResultId,int maxResultId,String oldTaskSourceId);

    @Override
    int fixTaskResult(@Param("ids") List<Integer> ids,int uniqueId,String newTaskSourceId);

    @Override
    List<TaskResult> listTaskResultByTime(String type,int minResultId,int maxResultId,String minTime, String maxTime);
}
