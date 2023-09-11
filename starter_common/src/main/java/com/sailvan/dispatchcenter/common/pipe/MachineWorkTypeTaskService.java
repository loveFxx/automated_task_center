package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.MachineWorkType;
import com.sailvan.dispatchcenter.common.domain.MachineWorkTypeTask;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author mh
 * @date 2021-06
 */
public interface MachineWorkTypeTaskService {


    public List<MachineWorkTypeTask> getMachineWorkTypeTaskByWorkTypeId(@Param("workTypeId") int workTypeId);
    public List<MachineWorkTypeTask> getMachineWorkTypeTaskByMachineIdAndWorkTypeId(@Param("machineId") int machineId,@Param("workTypeId") int workTypeId);
    public List<MachineWorkTypeTask> getMachineWorkTypeTaskByWorkTypeIdTaskId(@Param("workTypeId") int workTypeId, @Param("taskId") int taskId);
    public List<MachineWorkTypeTask> getMachineWorkTypeTaskByWorkTypeIdStatus(@Param("workTypeId") int workTypeId, @Param("status") int status);


    public int updateMachineWorkTypeTask(MachineWorkTypeTask machine);


    public int updateMachineWorkTypeTaskStatus(MachineWorkTypeTask machine);

    public int updateMachineWorkTypeTaskStatusByWorkTypeIdAndTaskId( int workTypeId,  int taskId,  int status, int isUpdate);


    public int insertMachineWorkTypeTask(MachineWorkTypeTask machine);


}
