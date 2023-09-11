package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.domain.AwsTaskMap;
import com.sailvan.dispatchcenter.common.domain.LambdaUserMap;
import com.sailvan.dispatchcenter.common.domain.Task;
import com.sailvan.dispatchcenter.common.pipe.LambdaUserMapService;
import com.sailvan.dispatchcenter.common.pipe.LambdaUserService;
import com.sailvan.dispatchcenter.common.pipe.TaskService;
import com.sailvan.dispatchcenter.db.dao.automated.AwsTaskMapDao;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class AwsTaskMapService implements com.sailvan.dispatchcenter.common.pipe.AwsTaskMapService {

    @Autowired
    AwsTaskMapDao awsTaskMapDao;

    @Autowired
    TaskService taskService;

    @Autowired
    LambdaUserMapService lambdaUserMapService;

    @Override
    public void addTaskMap(Task task) {
        Task taskByTaskName = taskService.getTaskByTaskName(task.getTaskName());
        String awsUserRegionFunctions = task.getAwsUserRegionFunctions();
        if (!awsUserRegionFunctions.equals("")){
            String[] split = awsUserRegionFunctions.split("/|_");

            LambdaUserMap mapByLambdaUserMap = new LambdaUserMap();
            if (split.length>0){
                LambdaUserMap lambdaUserMap = new LambdaUserMap();
                lambdaUserMap.setAccountName(split[0]);
                lambdaUserMap.setRegion(split[1]);
                lambdaUserMap.setFunctionName(split[2]);
                mapByLambdaUserMap = lambdaUserMapService.getMapByLambdaUserMap(lambdaUserMap);
            }
            if (taskByTaskName!=null && mapByLambdaUserMap != null){
                AwsTaskMap awsTaskMap = new AwsTaskMap();
                awsTaskMap.setTaskId(taskByTaskName.getId());
                awsTaskMap.setAwsLambdaMapId(mapByLambdaUserMap.getId());
                awsTaskMap.setTaskName(taskByTaskName.getTaskName());
                awsTaskMapDao.addTaskMap(awsTaskMap);
            }
        }
    }

    @Override
    public AwsTaskMap getTaskMapByTaskId( int taskId) {
        return awsTaskMapDao.getTaskMapByTaskId(taskId);
    }

	@Override
    public HashMap getRelationMap(int taskId) {
        return awsTaskMapDao.getRelationMap(taskId);
    }

    @Override
    public void updateTaskMap(AwsTaskMap awsTaskMap) {
        awsTaskMapDao.updateTaskMap(awsTaskMap);
    }

    @Override
    public int deleteByTaskId(Integer id) {
        return awsTaskMapDao.deleteByTaskId(id);
    }
}
