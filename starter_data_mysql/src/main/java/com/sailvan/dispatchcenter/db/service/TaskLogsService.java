package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.db.dao.automated.TaskLogsDao;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class TaskLogsService implements com.sailvan.dispatchcenter.common.pipe.TaskLogsService {

    @Autowired
    private TaskLogsDao taskLogsDao;

    @Override
    public int insertTaskLogs(TaskLogs taskLogs){
        return taskLogsDao.insertTaskLogs(taskLogs);
    }

    @Override
    public PageDataResult getTaskLogsList(TaskLogs taskLogs, Integer pageNum, Integer pageSize,String startTime,String endTime) {

        PageHelper.startPage(pageNum, pageSize);
        if(StringUtils.isEmpty(startTime)){
            startTime = DateUtils.getCurrentDateStart();
        }
        List<TaskLogs> taskLogsList = taskLogsDao.getTaskLogsByTaskLogs(taskLogs,startTime,endTime);
        //获得分页后的deviceIdList
        if (taskLogsList.size() <= pageSize){
            PageDataResult pageDataResult = new PageDataResult();
            if(taskLogsList.size() != 0){
                pageDataResult.setList(taskLogsList);
                pageDataResult.setTotals(taskLogsList.size());
                pageDataResult.setPageNum(pageNum);
            }

            return pageDataResult;
        }
        int totalNum = taskLogsList.size()/pageSize;
        List<TaskLogs> list = new ArrayList<>();
        if (pageNum == totalNum+1){
             list = taskLogsList.subList(pageNum*pageSize-pageSize, taskLogsList.size());
        }else {
            list = taskLogsList.subList(pageNum*pageSize-pageSize, pageNum*pageSize);
        }

        PageDataResult pageDataResult = new PageDataResult();
        if(list.size() != 0){
            pageDataResult.setList(list);
            pageDataResult.setTotals(taskLogsList.size());
            pageDataResult.setPageNum(pageNum);
        }

        return pageDataResult;
    }

    @Override
    public List<TaskLogs> getTaskLogsById(int id, String startTime,String endTime,int offset, int limit){
        return taskLogsDao.getTaskLogsById(id,startTime,endTime,offset,limit);
    }

    @Override
    public int getTaskLogsCountById(int id, String startTime,String endTime){
        return taskLogsDao.getTaskLogsCountById(id,startTime,endTime);
    }

    @Override
    public List<TaskLogs> getTaskLogsByTaskSourceId(String taskSourceId, String startTime, String endTime) {
        return taskLogsDao.getTaskLogsByTaskSourceId(taskSourceId, startTime, endTime);

    }

    @Override
    public List<TaskLogs> getTaskLogsListInOneHour(int startId, String startTime, String endTime, int offset, int limit) {
        return taskLogsDao.getTaskLogsListInOneHour(startId,startTime,endTime,offset,limit);
    }
}
