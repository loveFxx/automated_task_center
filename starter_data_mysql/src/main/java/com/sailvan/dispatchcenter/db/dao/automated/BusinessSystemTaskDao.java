package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.BusinessSystem;
import com.sailvan.dispatchcenter.common.domain.BusinessSystemTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author mh
 * @date 21-11
 */
@Mapper
public interface BusinessSystemTaskDao {

    /**
     * 搜索所有
     * @return
     */
    List<BusinessSystemTask> getBusinessSystemTaskAll();



    /**
     *  根据systemId获取
     * @param systemId
     * @return
     */
    List<BusinessSystemTask> getBusinessSystemTaskBySystemId(@Param("systemId") int systemId);

    /**
     *  获取
     * @param systemId
     * @param status
     * @return
     */
    List<BusinessSystemTask> getBusinessSystemTaskBySystemIdAndStatus(@Param("systemId") int systemId, @Param("status") int status);

    /**
     * 获取
     * @param systemId
     * @param taskId
     * @param status
     * @return
     */
    List<BusinessSystemTask> getBusinessSystemTaskBySystemIdAndTaskIdAndStatus(@Param("systemId") int systemId,@Param("taskId") int taskId, @Param("status") int status);

    /**
     *  根据任务名和系统id
     * @param systemId
     * @param taskName
     * @param status
     * @return
     */
    List<BusinessSystemTask> getBusinessSystemTaskBySystemIdAndTaskName(@Param("systemId") int systemId, @Param("taskName") String taskName, @Param("status") int status);



    /**
     *  更新
     * @param businessSystemTask
     * @return
     */
    int updateBusinessSystemTask(BusinessSystemTask businessSystemTask);

    /**
     *  更新状态
     * @param businessSystemTask
     * @return
     */
    int updateBusinessSystemTaskStatus(BusinessSystemTask businessSystemTask);


    /**
     *  插入
     * @param businessSystemTask
     * @return
     */
    int insertBusinessSystemTask(BusinessSystemTask businessSystemTask);


}
