package com.sailvan.dispatchcenter.data.controller;

import com.sailvan.dispatchcenter.common.constant.MonitorConstant;
import com.sailvan.dispatchcenter.common.domain.Machine;
import com.sailvan.dispatchcenter.common.domain.MachineExeTask;
import com.sailvan.dispatchcenter.common.domain.MachineTypeCountDTO;
import com.sailvan.dispatchcenter.common.domain.MachineVersionStatDTO;
import com.sailvan.dispatchcenter.common.util.HttpDownloadUtils;
import com.sailvan.dispatchcenter.common.util.MachineXmlUtil;
import com.sailvan.dispatchcenter.data.statistics.MachineStat;
import com.sailvan.dispatchcenter.db.dao.automated.MachineExeTaskDao;
import com.sailvan.dispatchcenter.db.service.MachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @program: automated_task_center2
 * @description:
 * @author: Wu Xingjian
 * @create: 2021-12-27 16:58
 **/

@RestController
public class MachineMonitorController {

    @Autowired
    private MachineExeTaskDao machineExeTaskDao;

    @Autowired
    private MachineService machineService;

    /**
     * LinkedHashMap<String,TreeMap<String,HashMap<String,Integer>>>
     * LinkedHashMap<任务类型,TreeMap<2022-01-01-01,HashMap<totalCount,10>>>
     * LinkedHashMap 保证任务按照从dao层返回的首字母顺序 TreeMap保证按照时间升序
     */
    @RequestMapping(value = "/machineExeTaskStat", method = RequestMethod.POST)
    public  LinkedHashMap<String,TreeMap<String,HashMap<String,Integer>>> machineExeTaskStat(String ip,String period)  {


        LinkedHashMap<String,TreeMap<String,HashMap<String,Integer>>> res = new LinkedHashMap<>();
        TreeMap<String,HashMap<String,Integer>> globalTreeMap = new TreeMap<>(Comparator.reverseOrder());


        MachineExeTask machineExeTask = new MachineExeTask();
        machineExeTask.setIp(ip);
        machineExeTask.setPeriod(period);
        List<Map<String,String>> list = machineExeTaskDao.getMachineExeTaskByMachineExeTask(machineExeTask);
        for (Map<String, String>  map : list) {
            TreeMap<String,HashMap<String,Integer>> treeMap = new TreeMap<>(Comparator.reverseOrder());
            String taskType=map.get("task_type");
            String[] statArr = map.get("stat").split(",");
            for (String stat : statArr) {
                String statPeriod = stat.split(":")[0];
                String statValue = stat.split(":")[1];
                    String[] statValueMap = statValue.split("-");
                    HashMap <String,Integer>map2=new HashMap<>();
                    map2.put("totalCount",Integer.parseInt(statValueMap[0]));
                    map2.put("successCount",Integer.parseInt(statValueMap[1]));
                    map2.put("failureCount",Integer.parseInt(statValueMap[2]));
                    treeMap.put(statPeriod,map2);
                    if (globalTreeMap.containsKey(statPeriod)) {
                        HashMap <String,Integer> curMap = (HashMap<String,Integer>) globalTreeMap.get(statPeriod);
                        HashMap <String,Integer>map3=new HashMap<>();
                        map3.put("totalCount", curMap.get("totalCount")+Integer.parseInt(statValueMap[0]));
                        map3.put("successCount",curMap.get("successCount")+Integer.parseInt(statValueMap[1]));
                        map3.put("failureCount",curMap.get("failureCount")+Integer.parseInt(statValueMap[2]));
                        globalTreeMap.put(statPeriod,map3);

                    }else{
                        globalTreeMap.put(statPeriod,map2);
                    }
            }
            res.put(taskType,treeMap);
        }
        if (globalTreeMap.size()!=0) {
            res.put("global",globalTreeMap);
        }
        return res;

    }

    @RequestMapping(value = "/CountMachineWithHeartBeatTimeout")
    public int CountMachineWithHeartBeatTimeout() throws ParseException {
        return getMachineWithHeartBeatTimeout().size();
    }

    public List<Machine> getMachineWithHeartBeatTimeout() {
        int interval = 10;
        List<Machine> machineByStandard = machineService.getMachineByStandard(interval);
        return machineByStandard;
    }

    @RequestMapping(value = {"/getXMLForMachineMonitor/{key}/{value}", "/getXMLForMachineMonitor/{key}"})
    public String getXMLForMachineMonitor(HttpServletResponse response, @PathVariable String key, @PathVariable(required = false) String value) {
        List<Machine> machineList = new ArrayList<>();

        if (value == null) {

            switch (key) {
                case "HeartBeatTimeout":
                    machineList = getMachineWithHeartBeatTimeout();
                    break;
                case "NetWorkInvalid":
                    machineList = getMachineWithoutNetWork();
                    break;
                case "lackDiskSpace":
                    machineList = getMachineLackingDiskspace();
                    break;
                case "lackMemory":
                    machineList = getMachineLackingMemory();
                    break;
                case "BigTimeDiff":
                    machineList = getMachineWithBigTimeDiff();
                    break;
                default:
            }


        } else {
            if ("machineType".equals(key)) {
                machineList = machineService.getMachineByType(value);

            } else if ("fatherVersion".equals(key)) {
                machineList = machineService.getMachineByFatherVersion(value);

            } else if ("sonVersion".equals(key)) {
                machineList = machineService.getMachineBySonVersion(value);
            }
        }


        if (machineList.size() == 0) {
            return "0条数据";
        }
        File file = MachineXmlUtil.createXml(machineList);
        if (!file.exists()) {
            return "下载文件不存在";
        }
        return HttpDownloadUtils.httpDownload(response,file);
    }

    @RequestMapping(value = "/CountMachineWithoutNetWork")
    public int CountMachineWithoutNetWork() throws ParseException {
        return getMachineWithoutNetWork().size();
    }

    public List<Machine> getMachineWithoutNetWork() {
        List<Machine> machines = machineService.countMachineWithoutNetWork();
        return machines;
    }

    @RequestMapping(value = "/CountMachineLackingDiskspace")
    public int CountMachineLackingDiskspace() {
        return getMachineLackingDiskspace().size();
    }

    public List<Machine> getMachineLackingDiskspace() {
        List<Machine> res = new ArrayList<>();
        List<Machine> machineAll = machineService.getMachineStatusOn();
        for (Machine machine : machineAll) {
            if (MachineStat.checkDiskSpace(machine, MonitorConstant.MACHINE_DISKSPACE_THRESHOLD)) {
                res.add(machine);
            }
        }
        return res;
    }

    @RequestMapping(value = "/CountMachineLackingMemory")
    public int CountMachineLackingMemory() {
        return getMachineLackingMemory().size();
    }

    public List<Machine> getMachineLackingMemory() {
        List<Machine> machineWithBigTimeDiff = machineService.getMachineLackingMemory(MonitorConstant.MACHINE_CPU_THRESHOLD);
        return machineWithBigTimeDiff;
    }

    @RequestMapping(value = "/CountMachineWithBigTimeDiff")
    public int CountMachineWithBigTimeDiff() {
        return getMachineWithBigTimeDiff().size();
    }

    public List<Machine> getMachineWithBigTimeDiff() {
        String Diff = "5";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Machine> machineWithBigTimeDiff = machineService.getMachineWithBigTimeDiff(Diff);
        return machineWithBigTimeDiff;
    }


    @RequestMapping(value = "/getMachineTypeStat")
    public List<MachineTypeCountDTO> getMachineTypeStat() {
        List<MachineTypeCountDTO> machineTypeCount = machineService.getMachineTypeCount("30");
        return machineTypeCount;
    }


    @RequestMapping(value = "/getMachineFatherVersionStat")
    public List<MachineVersionStatDTO> getMachineFatherVersionStat() {
        List<MachineVersionStatDTO> machineFatherVersionStat = machineService.getMachineFatherVersionStat();
        return machineFatherVersionStat;
    }


    @RequestMapping(value = "/getMachineSonVersionStat")
    public List<MachineVersionStatDTO> getMachineSonVersionStat() {
        List<MachineVersionStatDTO> machineSonVersionStat = machineService.getMachineSonVersionStat();
        return machineSonVersionStat;
    }

    /**
     * 汇总机器的各种问题 心跳超时...网络不可用...
     *
     * @return
     */
    public List<Machine> summaryMachine() {

        List<Machine> machineStatusOn = machineService.getMachineStatusOn();
        ArrayList<Machine> res = new ArrayList<>();
        for (Machine machine : machineStatusOn) {
            Machine machineInExcel = new Machine();
            //检查到有问题
            boolean checkedSth = false;
            machineInExcel.setIp(machine.getIp());
            //网络初始是-1 无效是0
            machineInExcel.setNetWork(-1);
            if (MachineStat.checkHeartBeatTimeout(machine, 1800)) {
                machineInExcel.setLastHeartbeat(machine.getLastHeartbeat());
                checkedSth = true;
            }
            if (MachineStat.checkNetWorkInvalid(machine)) {
                machineInExcel.setNetWork(machine.getNetWork());
                checkedSth = true;
            }
            if (MachineStat.checkDiskSpace(machine, MonitorConstant.MACHINE_DISKSPACE_THRESHOLD)) {
                //FIXME 有一些机器磁盘空间字段为no find无法解析
                Map<String, Long> cFromDiskSpace = MachineStat.getCFromDiskSpace(machine.getDiskSpace());
                //不需要担心下面size为null checkDiskSpace为true的必定不会为null
                if(cFromDiskSpace != null){
                    Long size = cFromDiskSpace.get("size");
                    Long freeSpace = cFromDiskSpace.get("free_space");
                    Long usedSpace = size - freeSpace;
                    machineInExcel.setDiskSpace(usedSpace + " / " + size);
                    checkedSth = true;
                }

            }
            if (MachineStat.checkMachineLackingMemory(machine, 90)) {
                machineInExcel.setMemory(machine.getMemory());
                checkedSth = true;
            }
            if (MachineStat.checkMachineWithBigTimeDiff(machine, 300)) {
                machineInExcel.setMachineLocalTime(machine.getMachineLocalTime() + " " + machine.getLastHeartbeat());
                checkedSth = true;
            }
            if (checkedSth) {
                res.add(machineInExcel);
            }
        }
        return res;
    }





}
