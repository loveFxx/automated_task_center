package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.MachineExeTask;
import com.sailvan.dispatchcenter.common.domain.MachineWorkType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mh
 * @date 21-12
 *  客户端机器时间执行任务
 */
@Mapper
public interface MachineExeTaskDao {



    List<MachineExeTask> getMachineExeTaskByIpTaskNamePeriod(@Param("ip") String ip, @Param("taskType") String taskType, @Param("period") String period);



    List<Map<String,String>> getMachineExeTaskByMachineExeTask(MachineExeTask machineExeTask);


    /**
     *  根据指定个别参数更新
     * @param machineExeTask
     * @return
     */
    int updateMachineExeTask(MachineExeTask machineExeTask);


    int insertMachineExeTask(MachineExeTask machineExeTask);



}
