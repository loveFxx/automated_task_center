package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.MachineWorkType;
import com.sailvan.dispatchcenter.common.domain.MachineWorkTypeTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author mh
 * @date 21-06
 *
 *  客户端机器可执行任务类型
 */
@Mapper
public interface MachineWorkTypeTaskDao {


    /**
     *  根据workTypeId
     * @param workTypeId
     * @return
     */
    List<MachineWorkTypeTask> getMachineWorkTypeTaskByWorkTypeId(@Param("workTypeId") int workTypeId);

    List<MachineWorkTypeTask> getMachineWorkTypeTaskByMachineIdAndWorkTypeId(@Param("machineId") int machineId, @Param("workTypeId") int workTypeId);
    List<MachineWorkTypeTask> getMachineWorkTypeTaskByMachineIdAndWorkTypeIdAndTaskId(@Param("machineId") int machineId, @Param("workTypeId") int workTypeId, @Param("taskId") int taskId);

    List<MachineWorkTypeTask> getMachineWorkTypeTaskByWorkTypeIdTaskId(@Param("workTypeId") int workTypeId, @Param("taskId") int taskId);

    /**
     *  搜索
     * @param workTypeId
     * @param status
     * @return
     */
    List<MachineWorkTypeTask> getMachineWorkTypeTaskByWorkTypeIdStatus(@Param("workTypeId") int workTypeId,@Param("status") int status);


    /**
     *  根据指定个别参数更新
     * @param machine
     * @return
     */
    int updateMachineWorkTypeTask(MachineWorkTypeTask machine);

    /**
     *  更新
     * @param workTypeId
     * @param taskId
     * @param status
     * @return
     */
    int updateMachineWorkTypeTaskStatusByWorkTypeIdAndTaskId(@Param("workTypeId") int workTypeId,@Param("taskId") int taskId,@Param("status") int status,@Param("isUpdate") int isUpdate);

    /**
     *  更新机器任务类型状态
     * @param machine
     * @return
     */
    int updateMachineWorkTypeTaskStatus(MachineWorkTypeTask machine);



    /**
     *  插入
     * @param machine
     * @return
     */
    int insertMachineWorkTypeTask(MachineWorkTypeTask machine);


    List<Integer> getMachineTaskTypeByTaskId(@Param(value="taskIdList") String taskIdList);

}
