package com.sailvan.dispatchcenter.stat.monitor.statistics;

import com.sailvan.dispatchcenter.common.domain.TaskStatDomain;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * @description: es
 * @author: mh
 * @create: 2022-03
 **/
@Component
public class TaskLogStatEs extends TaskLogStat{

    public Map<String, TaskStatDomain> getTaskSuccessMap(String oneDayStart, String oneDayEnd, String secondDayStart, String secondDayEnd) throws IOException, InterruptedException {
        //返回值
        Map<String, TaskStatDomain> res = new HashMap<>();
        //每个小类型总数 是按照任务首字母排序的
        List<Map> TaskNameTotalCount = taskLogsDao.getTaskNameTotalCount(oneDayStart, oneDayEnd);
        for (Map curMap : TaskNameTotalCount) {

            TaskStatDomain taskStatDomain = new TaskStatDomain();
            int totalCount = (int) (long) curMap.get("count(*)");

            taskStatDomain.setTotalCount(totalCount);
            res.put(curMap.get("task_name").toString(), taskStatDomain);
        }


        //当天和跨天统计成功
        List<Map> eachTaskNameSuccessCount = taskLogsDao.getTaskNameSuccessCount(oneDayStart, secondDayEnd, null);
        for (Map curMap : eachTaskNameSuccessCount) {
            String taskName = curMap.get("task_name").toString();
            if (res.containsKey(taskName)) {
                TaskStatDomain taskStatDomain = res.get(taskName);
                taskStatDomain.setSuccessCount(taskStatDomain.getSuccessCount() + (int) (long) curMap.get("count(*)"));
            }
        }

        //有包含文件的任务数
        List<Map> eachTaskNameFileCount = taskLogsDao.getTaskNameFileCount(oneDayStart, secondDayEnd, null);
        for (Map curMap : eachTaskNameFileCount) {
            String taskName = curMap.get("task_name").toString();
            if (res.containsKey(taskName)) {
                TaskStatDomain taskStatDomain = res.get(taskName);
                taskStatDomain.setFileCount(taskStatDomain.getFileCount() + (int) (long) curMap.get("count(*)"));
            }
        }

        //跨天统计强制失败 必须分为两天 因为group concat 两表聚合会失败
        List<Map> eachTaskNameForceFailedCount1 = taskLogsDao.getTaskNameForceFailedCount(oneDayStart, oneDayEnd, null);
        for (Map curMap : eachTaskNameForceFailedCount1) {
            String taskName = curMap.get("task_name").toString();
            if (res.containsKey(taskName)) {
                TaskStatDomain taskStatDomain = res.get(taskName);
                taskStatDomain.setForceFailedCount(taskStatDomain.getForceFailedCount() + (int) (long) curMap.get("count(*)"));
            }
        }


        //最后一次event出现在第二天(第二天才结束) 分批统计一下最后event是否为失败
        List<Map> taskNameFailedCount = taskLogsDao.getTaskNameFailedCount(oneDayStart, oneDayEnd, null);
        for (Map curMap : taskNameFailedCount) {
            String taskName = curMap.get("task_name").toString();
            if (res.containsKey(taskName)) {
                TaskStatDomain taskStatDomain = res.get(taskName);
                taskStatDomain.setFailedCount(taskStatDomain.getFailedCount() + (int) (long) curMap.get("count(*)"));
            }
        }

        //获取入池数与出池数 以计算出任务未执行数
        List<Map> taskNameInPoolCount = taskLogsDao.getTaskNameInPoolCount(oneDayStart, oneDayEnd);
        for (Map curMap : taskNameInPoolCount) {
            String taskName = curMap.get("task_name").toString();
            if (res.containsKey(taskName)) {
                TaskStatDomain taskStatDomain = res.get(taskName);
                taskStatDomain.setInPoolCount(taskStatDomain.getInPoolCount()+(int) (long) curMap.get("count(*)"));
            }
        }

        List<Map> taskNameOutPoolCount = taskLogsDao.getTaskNameOutPoolCount(oneDayStart, oneDayEnd);
        for (Map curMap : taskNameOutPoolCount) {
            String taskName = curMap.get("task_name").toString();
            if (res.containsKey(taskName)) {
                TaskStatDomain taskStatDomain = res.get(taskName);
                int unExeCount =  taskStatDomain.getInPoolCount()- (int) (long) curMap.get("count(*)");
                if (unExeCount < 0){
                    unExeCount = 0;
                }
                taskStatDomain.setUnExecutedCount(unExeCount);
            }
        }

        return sortMapByKey(res);
    }
    public static Map<String, TaskStatDomain> sortMapByKey(Map<String, TaskStatDomain> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<String, TaskStatDomain> sortMap = new TreeMap<String, TaskStatDomain>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);//升序排序
            }
        });
        sortMap.putAll(map);
        return sortMap;
    }
}
