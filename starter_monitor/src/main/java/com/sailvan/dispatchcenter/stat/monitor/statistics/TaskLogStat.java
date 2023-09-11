package com.sailvan.dispatchcenter.stat.monitor.statistics;

import com.sailvan.dispatchcenter.common.domain.TaskStatDomain;
import com.sailvan.dispatchcenter.common.util.ExcelUtils;
import com.sailvan.dispatchcenter.db.dao.automated.TaskLogsDao;
import com.sailvan.dispatchcenter.es.config.EsMarkerConfiguration;
import com.sailvan.dispatchcenter.stat.monitor.util.SplitBigListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @program: automated_task_center
 * @description:
 * @author: Wu Xingjian
 * @create: 2021-11-15 18:56
 **/
@Component
public class TaskLogStat {

    @Autowired
    TaskLogsDao taskLogsDao;


    public void taskFailureReasonStat(String oneDayStart, String oneDayEnd, String filePath) throws IOException, InterruptedException {

        String splitSign = "@_@";
        ArrayList<String[]> infoList = new ArrayList<>();
        List<Map> maps = taskLogsDao.tooMuchFailureReasonsStat(oneDayStart, oneDayEnd, 3, splitSign);

        for (Map map : maps) {
            infoList.add(new String[]{map.get("hash_key").toString(), map.get("task_name").toString(), map.get("account").toString(), map.get("continent").toString(), map.get("center_params").toString(), map.get("client_params").toString(), map.get("retry_times").toString(), map.get("explain").toString()});
        }

        String[] stringArr = new String[8];
        stringArr[0] = "任务唯一标志";
        stringArr[1] = "小类型";
        stringArr[2] = "帐号";
        stringArr[3] = "站点";
        stringArr[4] = "中心端参数";
        stringArr[5] = "客户端参数";
        stringArr[6] = "重试次数";
        stringArr[7] = "失败原因";

        ExcelUtils.createExcelFile3(filePath, stringArr, infoList);
    }


    public void taskSuccessStat(String oneDayStart, String oneDayEnd, String secondDayStart, String secondDayEnd, String filePath) throws IOException, InterruptedException {
        Map<String, TaskStatDomain> res = getTaskSuccessMap(oneDayStart, oneDayEnd, secondDayStart, secondDayEnd);

        ArrayList<String[]> infoList = new ArrayList<>();
        for (Map.Entry<String, TaskStatDomain> entry : res.entrySet()) {
            DecimalFormat df = new DecimalFormat("0.00%");
            String percent = df.format((double) entry.getValue().getSuccessCount() / entry.getValue().getTotalCount());
            infoList.add(new String[]{entry.getKey(), String.valueOf(entry.getValue().getTotalCount()), String.valueOf(entry.getValue().getSuccessCount()),
                    String.valueOf(entry.getValue().getForceFailedCount()), String.valueOf(entry.getValue().getFailedCount()), percent,
                    entry.getValue().getForceFailedList()
            });
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().toString());
        }

        String[] stringArr = new String[7];
        stringArr[0] = "小类型";
        stringArr[1] = "总量";
        stringArr[2] = "成功量";
        stringArr[3] = "未执行量";
        stringArr[4] = "失败量";
        stringArr[5] = "成功率";
        stringArr[6] = "未执行任务库ID";

        ExcelUtils.createExcelFile(filePath, stringArr, infoList);
    }


    public Map<String, TaskStatDomain> getTaskSuccessMap(String oneDayStart, String oneDayEnd, String secondDayStart, String secondDayEnd) throws IOException, InterruptedException {
        //返回值
        LinkedHashMap<String, TaskStatDomain> res = new LinkedHashMap<>();
        //每个小类型总数 是按照任务首字母排序的
        List<Map> TaskNameTotalCount = taskLogsDao.getTaskNameTotalCount(oneDayStart, oneDayEnd);
        for (Map curMap : TaskNameTotalCount) {
            TaskStatDomain taskStatDomain = new TaskStatDomain();
            //HashMap<String, Integer> map = new HashMap<>();
            int totalCount = (int) (long) curMap.get("count(*)");
            taskStatDomain.setTotalCount(totalCount);
            //map.put(TOTAL, totalCount);
            res.put(curMap.get("task_name").toString(), taskStatDomain);
        }

        // 第一天创建的总量hashKey集合 下面 成功/失败/强制失败 都要用到
        List<String> TotalHashKeysList = taskLogsDao.getHashKeysList(oneDayStart, oneDayEnd);

        //拆分成小String
        ArrayList<String> firstDayCreatedHashKeyList = SplitBigListUtils.BigListToSmallStringLists(TotalHashKeysList, 100000);

        List<Map> taskNameTotalSuccessCount = taskLogsDao.getTaskNameTotalSuccessCount(oneDayStart);
        for (Map curMap : taskNameTotalSuccessCount) {
            String taskName = curMap.get("task_name").toString();
            if (res.containsKey(taskName)) {
                TaskStatDomain taskStatDomain = res.get(taskName);
                taskStatDomain.setTotalSuccessCount((int) (long) curMap.get("count"));
            } else {
                TaskStatDomain taskStatDomain = new TaskStatDomain();
                taskStatDomain.setTotalSuccessCount((int) (long) curMap.get("count"));
                res.put(taskName, taskStatDomain);
            }
        }

        //第一天创建的大hashKey集合分批
        for (String s : firstDayCreatedHashKeyList) {
            //当天和跨天统计成功
            List<Map> eachTaskNameSuccessCount = taskLogsDao.getTaskNameSuccessCount(oneDayStart, secondDayEnd, s);
            for (Map curMap : eachTaskNameSuccessCount) {
                String taskName = curMap.get("task_name").toString();
                if (res.containsKey(taskName)) {
                    TaskStatDomain taskStatDomain = res.get(taskName);
                    taskStatDomain.setSuccessCount(taskStatDomain.getSuccessCount() + (int) (long) curMap.get("count(*)"));
                }
            }

            //有包含文件的任务数
            List<Map> eachTaskNameFileCount = taskLogsDao.getTaskNameFileCount(oneDayStart, secondDayEnd, s);
            for (Map curMap : eachTaskNameFileCount) {
                String taskName = curMap.get("task_name").toString();
                if (res.containsKey(taskName)) {
                    TaskStatDomain taskStatDomain = res.get(taskName);
                    taskStatDomain.setFileCount(taskStatDomain.getFileCount() + (int) (long) curMap.get("count(*)"));
                }
            }


            //跨天统计强制失败 必须分为两天 因为group concat 两表聚合会失败
            List<Map> eachTaskNameForceFailedCount1 = taskLogsDao.getTaskNameForceFailedCount(oneDayStart, oneDayEnd, s);
            for (Map curMap : eachTaskNameForceFailedCount1) {
                String taskName = curMap.get("task_name").toString();
                if (res.containsKey(taskName)) {
                    TaskStatDomain taskStatDomain = res.get(taskName);
                    taskStatDomain.setForceFailedCount(taskStatDomain.getForceFailedCount() + (int) (long) curMap.get("count(*)"));
                    taskStatDomain.setForceFailedList(taskStatDomain.getForceFailedList() + curMap.get("task_name").toString() + ",");
                }
            }

            List<Map> eachTaskNameForceFailedCount2 = taskLogsDao.getTaskNameForceFailedCount(secondDayStart, secondDayEnd, s);
            for (Map curMap : eachTaskNameForceFailedCount2) {
                String taskName = curMap.get("task_name").toString();
                if (res.containsKey(taskName)) {
                    TaskStatDomain taskStatDomain = res.get(taskName);
                    taskStatDomain.setForceFailedCount(taskStatDomain.getForceFailedCount() + (int) (long) curMap.get("count(*)"));
                    taskStatDomain.setForceFailedList(taskStatDomain.getForceFailedList() + curMap.get("task_name").toString() + ",");


                }
            }
        }

        //最后一次event出现在第二天(第二天才结束)的那些记录
        ArrayList<String> MaxIdOfHashKeyEndedInSecondDay = new ArrayList<>();
        ArrayList<String> HashKeyFromSecondDay = new ArrayList<>();
        //第一天创建的大hashKey集合分批
        for (String s : firstDayCreatedHashKeyList) {
//            通过hashKey拿到第一天内的最后event
            MaxIdOfHashKeyEndedInSecondDay.addAll(taskLogsDao.getMaxIdOfHashKey(secondDayStart, secondDayEnd, s));
            HashKeyFromSecondDay.addAll(taskLogsDao.getHashKeyFromSecondDay(secondDayStart, secondDayEnd, s));
        }

        //最后一次event出现在第二天(第二天才结束) 从第一天hashKey集合删除
        HashSet<String> totalHashSet = new HashSet();
        HashSet<String> secondDayHashSet = new HashSet();
        for (final String s : TotalHashKeysList) {
            totalHashSet.add(s);
        }
        for (final String s : HashKeyFromSecondDay) {
            secondDayHashSet.add(s);
        }
        totalHashSet.removeAll(secondDayHashSet);
        TotalHashKeysList.clear();
        for (final String s : totalHashSet) {
            TotalHashKeysList.add(s);
        }

        //最后一次event出现在第二天(第二天才结束) 分批统计一下最后event是否为失败
        ArrayList<String> strings1 = SplitBigListUtils.BigListToSmallStringLists(MaxIdOfHashKeyEndedInSecondDay, 100000);
        for (String s : strings1) {
            List<Map> taskNameFailedCount = taskLogsDao.getTaskNameFailedCount(secondDayStart, secondDayEnd, s);
            for (Map curMap : taskNameFailedCount) {
                String taskName = curMap.get("task_name").toString();
                if (res.containsKey(taskName)) {
                    TaskStatDomain taskStatDomain = res.get(taskName);
                    taskStatDomain.setFailedCount(taskStatDomain.getFailedCount() + (int) (long) curMap.get("count(*)"));
                }
            }
        }

        //最后一次event出现在第一天(去除掉第二天才结束的) 分批统计一下最后event是否为失败
        ArrayList<String> firstDayCreatedAndEndedHashKeyList = SplitBigListUtils.BigListToSmallStringLists(TotalHashKeysList, 10000);

        ArrayList<String> MaxIdOfHashKeyEndedInFirstDay = new ArrayList<>();

        for (String s : firstDayCreatedAndEndedHashKeyList) {
            //通过hashKey拿到第一天内的最后event
            MaxIdOfHashKeyEndedInFirstDay.addAll(taskLogsDao.getMaxIdOfHashKey(oneDayStart, oneDayEnd, s));
        }

        ArrayList<String> strings = SplitBigListUtils.BigListToSmallStringLists(MaxIdOfHashKeyEndedInFirstDay, 100000);
        for (String string : strings) {

            List<Map> taskNameFailedCount = taskLogsDao.getTaskNameFailedCount(oneDayStart, oneDayEnd, string);
            for (Map curMap : taskNameFailedCount) {
                String taskName = curMap.get("task_name").toString();
                if (res.containsKey(taskName)) {
                    TaskStatDomain taskStatDomain = res.get(taskName);
                    taskStatDomain.setFailedCount(taskStatDomain.getFailedCount() + (int) (long) curMap.get("count(*)"));
                }
            }
        }
        return res;
    }
}
