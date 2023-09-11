package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.MachineHeartbeatLogs;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

import java.util.ArrayList;
import java.util.List;


/**
 * @author mh
 * @date 2021-04
 */
public interface MachineHeartbeatLogsService {


    public PageDataResult getMachineHeartbeatLogsByMachineIdList(int machineId);


    public int insertMachineHeartbeatLogs(MachineHeartbeatLogs machineHeartbeatLogs);

    public ArrayList<String[]> getLatestMachineHeartbeatLogsAll();

    List<String> getLatestIds(String start, String end);

    List<MachineHeartbeatLogs> getLatestHeartByIds(String start,String end, String idList);

}
