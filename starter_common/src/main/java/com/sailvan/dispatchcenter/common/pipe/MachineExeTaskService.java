package com.sailvan.dispatchcenter.common.pipe;


import com.sailvan.dispatchcenter.common.domain.Machine;
import com.sailvan.dispatchcenter.common.domain.MachineExeTask;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @author mh
 * @date 2021-12
 */
public interface MachineExeTaskService {


    List<MachineExeTask> getMachineExeTaskByIpTaskNamePeriod(@Param("ip") String ip, @Param("taskType") String taskType, @Param("period") String period);


    /**
     *  根据指定个别参数更新
     * @param machineExeTask
     * @return
     */
    int updateMachineExeTask(MachineExeTask machineExeTask);


    int insertMachineExeTask(MachineExeTask machineExeTask);

}
