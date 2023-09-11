package com.sailvan.dispatchcenter.db.service;


import com.sailvan.dispatchcenter.common.domain.TaskFunnel;
import com.sailvan.dispatchcenter.db.dao.automated.TaskFunnelDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;


@Service
public class TaskFunnelService implements com.sailvan.dispatchcenter.common.pipe.TaskFunnelService {

    private static Logger logger = LoggerFactory.getLogger(TaskFunnelService.class);

    @Autowired
    private TaskFunnelDao taskFunnelDao;

    @Override
    public TaskFunnel findByTaskId(int id){
        return taskFunnelDao.findByTaskId(id);
    }

    @Override
    public int insertTaskFunnel(TaskFunnel taskFunnel){
        return taskFunnelDao.insertTaskFunnel(taskFunnel);
    }

    @Override
    public int updateTaskNumById(int taskNum, int id){
        return taskFunnelDao.updateTaskNumById(taskNum,id);
    }

    @Override
    public TaskFunnel findByTaskName(String taskName){
        return taskFunnelDao.findByTaskName(taskName);
    }

    @Override
    public int updateNextFireTimeById(BigInteger nextFireTime, int id){
        return taskFunnelDao.updateNextFireTimeById(nextFireTime,id);
    }

    @Override
    public List<TaskFunnel> listByTaskId(int taskId){
        return taskFunnelDao.listByTaskId(taskId);
    }

    @Override
    public TaskFunnel findMainFunnel(int taskId){
        return taskFunnelDao.findMainFunnel(taskId);
    }

    @Override
    public int deleteByTaskId(int taskId){
        return taskFunnelDao.deleteByTaskId(taskId);
    }

    @Override
    public int deleteById(int id){
        return taskFunnelDao.deleteById(id);
    }
}
