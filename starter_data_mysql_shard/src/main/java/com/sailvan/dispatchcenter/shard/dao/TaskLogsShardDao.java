package com.sailvan.dispatchcenter.shard.dao;

import com.sailvan.dispatchcenter.common.domain.TaskLogs;
import com.sailvan.dispatchcenter.db.dao.automated.TaskLogsDao;
import com.sailvan.dispatchcenter.shard.config.MysqlShardMarkerConfiguration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Primary;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务流水分表
 *
 * @author mh
 * @date 21-10
 */
@Primary
@Mapper
@ConditionalOnMissingBean(name = "esMarker")
@ConditionalOnBean(MysqlShardMarkerConfiguration.MysqlShardMarker.class)
public interface TaskLogsShardDao extends TaskLogsDao {

    @Override
    int insertTaskLogs(TaskLogs taskLogs);

    @Override
    List<TaskLogs> getTaskLogsByTaskLogs(@Param("taskLogs") TaskLogs taskLogs, @Param("startTime") String startTime, @Param("endTime") String endTime);

    @Override
    List<TaskLogs> getTaskLogsById(@Param("id") Integer id, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("offset") int offset, @Param("limit") Integer limit);

    @Override
    int getTaskLogsCountById(@Param("id") int id, @Param("startTime") String startTime, @Param("endTime") String endTime);

    @Override
    List<TaskLogs> getTaskLogsByTaskSourceId(@Param("taskSourceId") String taskSourceId, @Param("startTime") String startTime, @Param("endTime") String endTime);


    /**
     * 小任务类型 时间段内(通常是一天)创建(event=1)总数
     *
     * @param start
     * @param end
     * @return
     */
    @Override
    List<Map> getTaskNameTotalCount(@Param("start") String start, @Param("end") String end);


    /**
     * 获取时间段内(第一天)创建(event=1)的hashKey集合
     *
     * @param start
     * @param end
     * @return
     */
    @Override
    List<String> getHashKeysList(@Param("start") String start, @Param("end") String end);


    /**
     * 时间段内统计强制失败 start为第一/二天00:00 end为第一/二天23:59
     *
     * @param start
     * @param hashKeyList
     * @return 跨天统计强制失败 必须分为两天 因为group concat 两表聚合会失败
     */
    @Override
    List<Map> getTaskNameForceFailedCount(@Param("start") String start, @Param("end") String end, @Param("hashKeyList") String hashKeyList);


    /**
     * 时间段内统计成功 通常start为第一天00:00 end为第二天23:59
     *
     * @param start
     * @param end
     * @param hashKeyList
     * @return
     */
    @Override
    List<Map> getTaskNameSuccessCount(@Param("start") String start, @Param("end") String end, @Param("hashKeyList") String hashKeyList);


    /**
     * 有包含文件的任务数量
     *
     * @param start
     * @param end
     * @param hashKeyList
     * @return
     */
    @Override
    List<Map> getTaskNameFileCount(@Param("start") String start, @Param("end") String end, @Param("hashKeyList") String hashKeyList);

    /**
     * 从时间段内(可能是第一天也可能是第二天)找hashKey集合中每条的最晚记录(最大id)
     *
     * @param start
     * @param end
     * @param hashKeyList
     * @return
     */
    @Override
    List<String> getMaxIdOfHashKey(@Param("start") String start, @Param("end") String end, @Param("hashKeyList") String hashKeyList);


    /**
     * 从时间段(第二天)出现过的第一天创建的hashKey
     *
     * @param start
     * @param end
     * @param hashKeyList
     * @return
     */
    @Override
    List<String> getHashKeyFromSecondDay(@Param("start") String start, @Param("end") String end, @Param("hashKeyList") String hashKeyList);


    /**
     * 跨天
     *
     * @param start
     * @param end
     * @param idList
     * @return
     */
    @Override
    List<Map> getTaskNameFailedCount(@Param("start") String start, @Param("end") String end, @Param("idList") String idList);

    /**
     * 重试>=retryTimes 账号-站点-小类型-参数-失败原因（多条）
     *
     * @param start
     * @param end
     * @param retryTimes
     * @param splitSign
     * @return
     */
    @Override
    List<Map> tooMuchFailureReasonsStat(@Param("start") String start, @Param("end") String end, @Param("retryTimes") int retryTimes, @Param("splitSign") String splitSign);


    @Override
    List<Map> getTaskNameTotalSuccessCount(@Param("start") String start);

    /**
     * 以continent account分组 得到每组的任务小类型集合
     *
     * @param start
     * @param end
     * @return
     */
    @Override
    List<Map> getTaskMameListByContinentsAccount(@Param("start") String start, @Param("end") String end);

    /**
     * 通过taskSourceId查询出对应任务的状态
     *
     * @param taskSourceId
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    List<TaskLogs> getEventById(@Param("taskSourceId") String taskSourceId, @Param("startTime") String startTime, @Param("endTime") String endTime);



    @Override
    List<TaskLogs> getTaskLogsListInOneHour(@Param("startId") int startId, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("offset") int offset, @Param("limit") int limit);

    @Override
    List<Map> getTaskNameOutPoolCount(String oneDayStart, String oneDayEnd);

    @Override
    List<Map> getTaskNameInPoolCount(String oneDayStart, String oneDayEnd);
}
