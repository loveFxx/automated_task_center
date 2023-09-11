package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.TaskFunnel;

import java.math.BigInteger;
import java.util.List;

public interface TaskFunnelService {


    public TaskFunnel findByTaskId(int taskId);

    int insertTaskFunnel(TaskFunnel taskFunnel);

    int updateTaskNumById(int taskNum, int id);

    TaskFunnel findByTaskName(String taskName);

    int updateNextFireTimeById(BigInteger nextFireTime, int id);

    List<TaskFunnel> listByTaskId(int taskId);

    TaskFunnel findMainFunnel(int taskId);

    int deleteByTaskId(int taskId);

    int deleteById(int id);
}
