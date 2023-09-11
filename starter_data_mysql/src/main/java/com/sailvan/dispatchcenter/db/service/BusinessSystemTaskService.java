package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.domain.BusinessSystemTask;
import com.sailvan.dispatchcenter.db.dao.automated.BusinessSystemTaskDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author mh
 * @date 2021-11
 */
@Service
public class BusinessSystemTaskService implements com.sailvan.dispatchcenter.common.pipe.BusinessSystemTaskService {

    private static Logger logger = LoggerFactory.getLogger(BusinessSystemTaskService.class);

    @Autowired
    private BusinessSystemTaskDao businessSystemTaskDao;


    @Override
    public List<BusinessSystemTask> getBusinessSystemTaskAll() {
        return businessSystemTaskDao.getBusinessSystemTaskAll();
    }

    @Override
    public List<BusinessSystemTask> getBusinessSystemTaskBySystemId(int systemId) {
        return businessSystemTaskDao.getBusinessSystemTaskBySystemId(systemId);
    }

    @Override
    public List<BusinessSystemTask> getBusinessSystemTaskBySystemIdAndStatus(int systemId, int status) {
        return businessSystemTaskDao.getBusinessSystemTaskBySystemIdAndStatus(systemId, status);
    }

    @Override
    public List<BusinessSystemTask> getBusinessSystemTaskBySystemIdAndTaskIdAndStatus(int systemId, int taskId, int status) {
        return businessSystemTaskDao.getBusinessSystemTaskBySystemIdAndTaskIdAndStatus(systemId, taskId, status);
    }

    @Override
    public List<BusinessSystemTask> getBusinessSystemTaskBySystemIdAndTaskName(int systemId, String taskName, int status) {
        return businessSystemTaskDao.getBusinessSystemTaskBySystemIdAndTaskName(systemId, taskName, status);
    }

    @Override
    public int updateBusinessSystemTask(BusinessSystemTask businessSystemTask) {
        return businessSystemTaskDao.updateBusinessSystemTask(businessSystemTask);
    }

    @Override
    public int updateBusinessSystemTaskStatus(BusinessSystemTask businessSystemTask) {
        return businessSystemTaskDao.updateBusinessSystemTaskStatus(businessSystemTask);
    }

    @Override
    public int insertBusinessSystemTask(BusinessSystemTask businessSystemTask) {
        return businessSystemTaskDao.insertBusinessSystemTask(businessSystemTask);
    }

    public List<BusinessSystemTask> getbstById(int systemId) {
        return businessSystemTaskDao.getBusinessSystemTaskBySystemId(systemId);
    }
}
