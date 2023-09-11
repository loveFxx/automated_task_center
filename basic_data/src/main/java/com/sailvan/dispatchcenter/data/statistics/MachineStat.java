package com.sailvan.dispatchcenter.data.statistics;

import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.domain.Machine;
import com.sailvan.dispatchcenter.common.util.DateUtils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: automated_task_center
 * @description:
 * @author: Wu Xingjian
 * @create: 2021-12-16 14:43
 **/
public class MachineStat {

    /**
     * 检查机器C盘空间是否不足 不足返回ture
     * 如果freeSpace小于size*(1-limit)了 说明空间不足了
     */

    public static boolean checkDiskSpace(Machine machine, double limit) {
        String diskSpace = machine.getDiskSpace();
        Map<String, Long> cFromDiskSpace = getCFromDiskSpace(diskSpace);
        if (cFromDiskSpace != null) {
            Long size = cFromDiskSpace.get("size");
            Long freeSpace = cFromDiskSpace.get("free_space");
            if (size != null && freeSpace != null) {
                long minFreeSpace = (long) (size * (1 - limit));
                if (freeSpace < minFreeSpace) {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * @param machine
     * @param interval
     * @return
     */
    public static boolean checkHeartBeatTimeout(Machine machine, int interval) {
        String last_heartbeat = machine.getLastHeartbeat();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = df.format(System.currentTimeMillis());
        if (last_heartbeat != null) {
            if (DateUtils.calTime(now, last_heartbeat) > interval) {
                return true;
            }
        }
        return false;
    }


    /**
     * 不可用返回true
     *
     * @param machine
     * @param
     * @return
     */
    public static boolean checkNetWorkInvalid(Machine machine) {
        return machine.getNetWork() == 0 ? true : false;
    }

    public static boolean checkMachineLackingMemory(Machine machine, double limit) {
        if (machine.getMemory() != null && !machine.getMemory().equals("null")) {
            Double memory = Double.valueOf(machine.getMemory());
            return memory > limit ? true : false;
        }
        return false;
    }

    public static boolean checkMachineWithBigTimeDiff(Machine machine, int interval) {

        String last_heartbeat = machine.getLastHeartbeat();
        String machineLocalTime = machine.getMachineLocalTime();
        if (machineLocalTime != null && last_heartbeat != null) {
            if (Math.abs(DateUtils.calTime(machineLocalTime, last_heartbeat)) > interval) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从{"F:":{"size":"130730721280","free_space":"128772509696"},"C:":{"size":"107375226880","free_space":"24236666880"}}
     *
     * @param diskSpace
     * @return 解析出C盘 “107375226880 24236666880”
     */
    public static Map<String, Long> getCFromDiskSpace(String diskSpace) {
        Map<String, Long> res = new HashMap<>();
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(diskSpace);
        } catch (Exception e) {
            System.out.println("getCFromDiskSpace: diskSpace=" + diskSpace + "-----" + e.getMessage());
            return null;
        }

        if (jsonObject != null) {
            JSONObject spaceJson = (JSONObject) jsonObject.get("C:");
            if (spaceJson != null) {
                long size = Long.parseLong((((String) spaceJson.get("size"))));
                long freeSpace = 0;
                String free_space = String.valueOf(spaceJson.get("free_space"));
                try {
                    freeSpace = Long.parseLong(free_space);
                } catch (Exception e) {
                    System.out.println("getCFromDiskSpace: diskSpace=" + diskSpace + "-----" + e.getMessage());
                }
                res.put("size", size);
                res.put("free_space", freeSpace);
                return res;
            }
        }
        return res;
    }

}
