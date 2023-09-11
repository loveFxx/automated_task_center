package com.sailvan.dispatchcenter.data.statistics;

import com.sailvan.dispatchcenter.common.domain.MachineHeartbeatLogs;
import com.sailvan.dispatchcenter.common.pipe.MachineHeartbeatLogsService;
import com.sailvan.dispatchcenter.common.util.ExcelUtils;
import com.sailvan.dispatchcenter.common.util.SplitBigListUtils;
import com.sailvan.dispatchcenter.shard.dao.MachineHeartbeatLogsShardDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: automated_task_center
 * @description:
 * @author: Wu Xingjian
 * @create: 2021-11-15 20:57
 **/
@Component
public class MachineHeartBeatLogStat {

    @Autowired
    MachineHeartbeatLogsService machineHeartbeatLogsService;

    public void heartBeatStat(String oneDayStart, String oneDayEnd, String filePath) throws IOException, InterruptedException {

        List<String> latestIds = machineHeartbeatLogsService.getLatestIds(oneDayStart, oneDayEnd);
        ArrayList<String> strings = SplitBigListUtils.BigListToSmallStringLists(latestIds, 500);
        ArrayList<MachineHeartbeatLogs> latestHeartByIds = new ArrayList<>();
        for (String string : strings) {
            System.out.println("string = " + string);
            latestHeartByIds.addAll(machineHeartbeatLogsService.getLatestHeartByIds(oneDayStart, oneDayEnd, string));
        }
        ArrayList<String[]> infoList = new ArrayList<>();
        for (MachineHeartbeatLogs curLog : latestHeartByIds) {
            infoList.add(new String[]{String.valueOf(curLog.getMachineId()), curLog.getWorkType(),
                    curLog.getTypes(), curLog.getCpu(), curLog.getMemory(), curLog.getDiskSpace(), curLog.getHeartbeat()});

        }

        String[] stringArr = new String[7];
        stringArr[0] = "机器id";
        stringArr[1] = "workType";
        stringArr[2] = "type";
        stringArr[3] = "cpu";
        stringArr[4] = "内存";
        stringArr[5] = "磁盘";
        stringArr[6] = "心跳时间";
        ExcelUtils.createExcelFile(filePath, stringArr, infoList);
    }


}
