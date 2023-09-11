package com.sailvan.dispatchcenter.stat.monitor.statistics;

import com.sailvan.dispatchcenter.common.util.ExcelUtils;
import com.sailvan.dispatchcenter.db.dao.automated.MachineWorkTypeDao;
import com.sailvan.dispatchcenter.db.dao.automated.TaskLogsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @program: automated_task_center
 * @description:
 * @author: Wu Xingjian
 * @create: 2021-11-27 11:48
 **/

@Component
public class ContinentAccountStat {

    @Autowired
    MachineWorkTypeDao machineWorkTypeDao;

    @Autowired
    TaskLogsDao taskLogsDao;


    /**
     * machineWorkTypeDao中查出有效的continent account组合;taskLogsShardDao中按每个组合分组得到taskNamesList
     *
     * @param oneDayStart
     * @param oneDayEnd
     * @throws IOException
     * @throws InterruptedException
     */
    public void continentAccountStat(String oneDayStart, String oneDayEnd, String filePath) throws IOException, InterruptedException {

        List<String> continentsAccountByStatusAndType = machineWorkTypeDao.getContinentsAccountByStatusAndType();
        List<Map> maps = taskLogsDao.getTaskMameListByContinentsAccount(oneDayStart, oneDayEnd);

        ArrayList<String[]> infoList = new ArrayList<>();
        for (final Map map : maps) {
            if (continentsAccountByStatusAndType.contains(map.get("concat(continent,',',account)"))) {
                String continentAccount = map.get("concat(continent,',',account)").toString();
                String[] split = continentAccount.split(",");
                String continent = split[0];
                String account = split[1];
                infoList.add(new String[]{continent, account, map.get("group_concat(task_name)").toString()});
            }
        }

        String[] stringArr = new String[3];
        stringArr[0] = "continent";
        stringArr[1] = "account";
        stringArr[2] = "taskNames";

        ExcelUtils.createExcelFile(filePath, stringArr, infoList);
    }

}
