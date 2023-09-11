package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.TaskFunnel;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigInteger;
import java.util.List;

@Mapper
public interface TaskFunnelDao {


    /**
     * 按id倒叙获取第一个
     * @param taskId
     * @return
     */
    TaskFunnel findByTaskId(int taskId);

    int insertTaskFunnel(TaskFunnel taskFunnel);

    int updateTaskNumById(int taskNum, int id);

    TaskFunnel findByTaskName(String taskName);

    int updateNextFireTimeById(BigInteger nextFireTime, int id);

    List<TaskFunnel> listByTaskId(int taskId);

    TaskFunnel findMainFunnel(int taskId);

    int deleteByTaskId(int taskId);

    int deleteById(int id);
}
