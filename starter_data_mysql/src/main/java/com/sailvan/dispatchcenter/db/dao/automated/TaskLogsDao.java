package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.TaskLogs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface TaskLogsDao {

    int insertTaskLogs(TaskLogs taskLogs);

    List<TaskLogs> getTaskLogsByTaskLogs(@Param("taskLogs")TaskLogs taskLogs, @Param("startTime")String startTime, @Param("endTime")String endTime);

    int getTaskLogsCountById(@Param("id")int id, @Param("startTime")String startTime, @Param("endTime")String endTime);

    List<TaskLogs> getTaskLogsById(@Param("id") Integer id, @Param("startTime")String startTime, @Param("endTime")String endTime,@Param("offset") int offset,@Param("limit") Integer limit);

    List<TaskLogs> getTaskLogsByTaskSourceId(@Param("taskSourceId") String taskSourceId, @Param("startTime")String startTime, @Param("endTime")String endTime);




    /**
     * 小任务类型 时间段内(通常是一天)创建(event=1)总数
     * @param start
     * @param end
     * @return
     */
    List<Map> getTaskNameTotalCount(String start, String end);


    /**
     * 获取时间段内(第一天)创建(event=1)的hashKey集合
     * @param start
     * @param end
     * @return
     */
    List<String> getHashKeysList(String start,String end);


    /**
     * 时间段内统计强制失败 start为第一/二天00:00 end为第一/二天23:59
     * @param start
     * @param hashKeyList
     * @return 跨天统计强制失败 必须分为两天 因为group concat 两表聚合会失败
     */
    List<Map> getTaskNameForceFailedCount(String start,String end,String hashKeyList);


    /**
     * 时间段内统计成功 通常start为第一天00:00 end为第二天23:59
     * @param start
     * @param end
     * @param hashKeyList
     * @return
     */
    List<Map> getTaskNameSuccessCount(String start,String end,String hashKeyList);


    /**
     * 有包含文件的任务数量
     * @param start
     * @param end
     * @param hashKeyList
     * @return
     */
    List<Map> getTaskNameFileCount(String start,String end,String hashKeyList);

    /**
     * 从时间段内(可能是第一天也可能是第二天)找hashKey集合中每条的最晚记录(最大id)
     * @param start
     * @param end
     * @param hashKeyList
     * @return
     */
    List<String> getMaxIdOfHashKey(String start,String end,String hashKeyList);


    /**
     * 从时间段(第二天)出现过的第一天创建的hashKey
     * @param start
     * @param end
     * @param hashKeyList
     * @return
     */
    List<String> getHashKeyFromSecondDay(String start,String end,String hashKeyList);



    /**
     * 跨天
     * @param start
     * @param end
     * @param idList
     * @return
     */
    List<Map> getTaskNameFailedCount(String start,String end,String idList);

    /**
     * 重试>=retryTimes 账号-站点-小类型-参数-失败原因（多条）
     * @param start
     * @param end
     * @param retryTimes
     * @param splitSign
     * @return
     */
    List<Map>tooMuchFailureReasonsStat(String start,String end,int retryTimes ,String splitSign);


    List<Map> getTaskNameTotalSuccessCount(String start);
    /**
     * 以continent account分组 得到每组的任务小类型集合
     * @param start
     * @param end
     * @return
     */
    List<Map> getTaskMameListByContinentsAccount( String start,String end);

    /**
     * 通过taskSourceId查询出对应任务的状态
     * @param taskSourceId
     * @param startTime
     * @param endTime
     * @return
     */
    List<TaskLogs> getEventById(String taskSourceId,String startTime,String endTime);


    List<TaskLogs> getTaskLogsListInOneHour(int startId, String startTime, String endTime, int offset, int limit);

    List<Map> getTaskNameOutPoolCount(String oneDayStart, String oneDayEnd);

    List<Map> getTaskNameInPoolCount(String oneDayStart, String oneDayEnd);
}
