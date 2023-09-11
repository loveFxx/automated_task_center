package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.AwsTaskMap;
import com.sailvan.dispatchcenter.common.domain.Task;

import java.util.HashMap;

public interface AwsTaskMapService {

    void addTaskMap(Task task);

    AwsTaskMap getTaskMapByTaskId(int taskId);

    HashMap getRelationMap(int taskId);

    void updateTaskMap(AwsTaskMap awsTaskMap);

    int deleteByTaskId(Integer id);
}
