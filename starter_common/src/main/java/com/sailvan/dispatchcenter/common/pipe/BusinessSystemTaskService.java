package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.BusinessSystem;
import com.sailvan.dispatchcenter.common.domain.BusinessSystemTask;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @author mh
 * @date 2021-06
 */
public interface BusinessSystemTaskService {


    public List<BusinessSystemTask> getBusinessSystemTaskAll();

    public List<BusinessSystemTask> getBusinessSystemTaskBySystemId(int systemId);
    public List<BusinessSystemTask> getBusinessSystemTaskBySystemIdAndStatus(int systemId, int status);
    public List<BusinessSystemTask> getBusinessSystemTaskBySystemIdAndTaskIdAndStatus(int systemId,int taskId, int status);

    public List<BusinessSystemTask> getBusinessSystemTaskBySystemIdAndTaskName(int systemId, String taskName, int status);

    public int updateBusinessSystemTask(BusinessSystemTask businessSystemTask);

    public int updateBusinessSystemTaskStatus(BusinessSystemTask businessSystemTask);

    public int insertBusinessSystemTask(BusinessSystemTask businessSystemTask);

}
