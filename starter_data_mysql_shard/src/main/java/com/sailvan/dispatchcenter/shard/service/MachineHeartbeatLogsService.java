package com.sailvan.dispatchcenter.shard.service;

import com.sailvan.dispatchcenter.common.domain.MachineHeartbeatLogs;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.github.pagehelper.PageInfo;
import com.sailvan.dispatchcenter.shard.dao.MachineHeartbeatLogsShardDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * @author mh
 * @date 2021-04
 */

public class MachineHeartbeatLogsService implements com.sailvan.dispatchcenter.common.pipe.MachineHeartbeatLogsService {

    @Autowired
    MachineHeartbeatLogsShardDao machineHeartbeatLogsShardDao;

    @Override
    public PageDataResult getMachineHeartbeatLogsByMachineIdList(int machineId)  {

        MachineHeartbeatLogs machineHeartbeatLogSearch = new MachineHeartbeatLogs();
        machineHeartbeatLogSearch.setMachineId(machineId);
        machineHeartbeatLogSearch.setCreatedTime(DateUtils.getCurrentDateStart());

        List<MachineHeartbeatLogs> machineHeartbeatLogs = machineHeartbeatLogsShardDao.getMachineHeartbeatLogsByMachineId(machineHeartbeatLogSearch);
        PageInfo<MachineHeartbeatLogs> pageInfoOld = new PageInfo<>(machineHeartbeatLogs);

        PageDataResult pageDataResult = new PageDataResult();
        if(machineHeartbeatLogs.size() != 0){
            pageDataResult.setList(machineHeartbeatLogs);
            pageDataResult.setTotals((int) pageInfoOld.getTotal());
        }

        return pageDataResult;
    }


    @Override
    public int insertMachineHeartbeatLogs(MachineHeartbeatLogs machineHeartbeatLogs){
        return machineHeartbeatLogsShardDao.insertMachineHeartbeatLogs(machineHeartbeatLogs);
    }

    @Override
    public ArrayList<String[]> getLatestMachineHeartbeatLogsAll() {
        return null;
    }

    @Override
    public List<String> getLatestIds(String start, String end){
        return machineHeartbeatLogsShardDao.getLatestIds(start, end);
    }

    @Override
    public List<MachineHeartbeatLogs> getLatestHeartByIds(String start,String end, String idList){
        return machineHeartbeatLogsShardDao.getLatestHeartByIds(start, end, idList);
    }

}
