package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.AwsTaskMap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;

@Mapper
public interface AwsTaskMapDao {

    void addTaskMap(AwsTaskMap awsTaskMap);

    AwsTaskMap getTaskMapByTaskId(@Param("taskId") int taskId);

	HashMap getRelationMap(int taskId);

    void updateTaskMap(AwsTaskMap awsTaskMap);

    int deleteByTaskId(@Param("taskId")Integer taskId);
}
