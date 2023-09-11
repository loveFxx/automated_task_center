package com.sailvan.dispatchcenter.db.service;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sailvan.dispatchcenter.common.domain.TaskExecutedException;
import com.sailvan.dispatchcenter.common.domain.TaskInPoolMetric;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.db.dao.automated.TaskExecutedExceptionDao;
import com.sailvan.dispatchcenter.db.dao.automated.TaskInPoolMetricDao;

import javax.annotation.Resource;
import java.util.*;

public class TaskExecutedExceptionService implements com.sailvan.dispatchcenter.common.pipe.TaskExecutedExceptionService{

    @Resource
    TaskExecutedExceptionDao taskExecutedExceptionDao;

    @Override
    public TaskExecutedException findTaskExecutedException(String taskType, String date, int hour) {
        return taskExecutedExceptionDao.findTaskExecutedException(taskType,date,hour);
    }

    @Override
    public int insertTaskExecutedException(TaskExecutedException taskExecutedException) {
        return taskExecutedExceptionDao.insertTaskExecutedException(taskExecutedException);

    }

    @Override
    public int updateTaskExecutedException(TaskExecutedException taskExecutedException){
        return taskExecutedExceptionDao.updateTaskExecutedException(taskExecutedException);
    }

    @Override
    public PageDataResult getTaskExecutedException(int pageNum,int pageSize,String taskType) {
        String date = DateUtils.getDate();

//        List<String> taskTypeList = new ArrayList<>();
//        if (taskTypes != null && taskTypes != ""){
//            String[] split = taskTypes.split(",");
//            for (int i = 0; i <split.length ; i++) {
//                taskTypeList.add(split[i]);
//            }
//        }

        PageHelper.startPage(pageNum,pageSize);
        List<TaskExecutedException> taskExecutedExceptions
                = taskExecutedExceptionDao.getTaskExecutedException(date,taskType);
        PageDataResult pageDataResult = new PageDataResult();
        if (taskExecutedExceptions.size() != 0) {
            PageInfo<TaskExecutedException> pageInfo = new PageInfo<>(taskExecutedExceptions);
            pageDataResult.setList(taskExecutedExceptions);
            pageDataResult.setTotals((int) pageInfo.getTotal());
        }
        return pageDataResult;
    }

    @Override
    public JSONArray getFailTaskType() {
        List<Map> taskTypeMapList = new ArrayList<>();

        JSONArray jsonArray =new JSONArray();
        taskTypeMapList = taskExecutedExceptionDao.getFailTaskType();
        for (int i = 0; i <taskTypeMapList.size() ; i++) {
            Map map = taskTypeMapList.get(i);
            map.put("value",map.get("name"));
            Object o = JSONArray.toJSON(map);
            jsonArray.add(o);
        }



        return jsonArray;
    }

    public int deleteTaskExecutedException(String datetime){
        return taskExecutedExceptionDao.deleteTaskExecutedException(datetime);
    }

}
