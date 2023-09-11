package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.domain.MachineWorkTypeTask;
import com.sailvan.dispatchcenter.db.dao.automated.MachineWorkTypeTaskDao;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author mh
 * @date 2021-06
 */
@Service
public class MachineWorkTypeTaskService implements com.sailvan.dispatchcenter.common.pipe.MachineWorkTypeTaskService {

    private static Logger logger = LoggerFactory.getLogger(MachineWorkTypeTaskService.class);

    @Autowired
    private MachineWorkTypeTaskDao machineWorkTypeTaskDao;



    @Override
    public List<MachineWorkTypeTask> getMachineWorkTypeTaskByWorkTypeId(int workTypeId) {
        return machineWorkTypeTaskDao.getMachineWorkTypeTaskByWorkTypeId(workTypeId);
    }

    @Override
    public List<MachineWorkTypeTask> getMachineWorkTypeTaskByMachineIdAndWorkTypeId(int machineId, int workTypeId) {
        return machineWorkTypeTaskDao.getMachineWorkTypeTaskByMachineIdAndWorkTypeId(machineId, workTypeId);
    }

    @Override
    public List<MachineWorkTypeTask> getMachineWorkTypeTaskByWorkTypeIdTaskId(int workTypeId, int taskId) {
        return machineWorkTypeTaskDao.getMachineWorkTypeTaskByWorkTypeIdTaskId(workTypeId, taskId);
    }
    @Override
    public List<MachineWorkTypeTask> getMachineWorkTypeTaskByWorkTypeIdStatus(int workTypeId,int status) {
        return machineWorkTypeTaskDao.getMachineWorkTypeTaskByWorkTypeIdStatus(workTypeId,status);
    }

    @Override
    public int updateMachineWorkTypeTask(MachineWorkTypeTask machine) {
        return machineWorkTypeTaskDao.updateMachineWorkTypeTask(machine);
    }

    @Override
    public int updateMachineWorkTypeTaskStatusByWorkTypeIdAndTaskId( int workTypeId,  int taskId,  int status, int isUpdate) {
        return machineWorkTypeTaskDao.updateMachineWorkTypeTaskStatusByWorkTypeIdAndTaskId(workTypeId, taskId, status, isUpdate);
    }

    @Override
    public int updateMachineWorkTypeTaskStatus(MachineWorkTypeTask machine) {
        return machineWorkTypeTaskDao.updateMachineWorkTypeTaskStatus(machine);
    }

    @Override
    public int insertMachineWorkTypeTask(MachineWorkTypeTask machine) {
        return machineWorkTypeTaskDao.insertMachineWorkTypeTask(machine);
    }
}
