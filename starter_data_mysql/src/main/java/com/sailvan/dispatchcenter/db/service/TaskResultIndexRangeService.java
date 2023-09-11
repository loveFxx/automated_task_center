package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.domain.TaskResultIndexRange;
import com.sailvan.dispatchcenter.db.dao.automated.TaskResultIndexRangeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author menghui
 * @date 21-04
 */
@Service
public class TaskResultIndexRangeService implements com.sailvan.dispatchcenter.common.pipe.TaskResultIndexRangeService {

    private static Logger logger = LoggerFactory.getLogger(TaskResultIndexRangeService.class);

    @Autowired
    TaskResultIndexRangeDao taskResultIndexRangeDao;

    @Override
    public int insertTaskResultIndexRange(TaskResultIndexRange taskResultIndexRange){
        return taskResultIndexRangeDao.insertTaskResultIndexRange(taskResultIndexRange);
    }

    @Override
    public TaskResultIndexRange getSmallestRangeIndex(String date){
        return taskResultIndexRangeDao.getSmallestRangeIndex(date);
    }

    @Override
    public TaskResultIndexRange getBiggestRangeIndex(String date){
        return taskResultIndexRangeDao.getBiggestRangeIndex(date);
    }
}
