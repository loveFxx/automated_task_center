package com.sailvan.dispatchcenter.common.cache.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sailvan.dispatchcenter.common.cache.InitAccountCache;
import com.sailvan.dispatchcenter.common.cache.InitMachineCache;
import com.sailvan.dispatchcenter.common.cache.InitPlatformCache;
import com.sailvan.dispatchcenter.common.cache.InitTaskCache;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.pipe.*;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import lombok.SneakyThrows;
import org.apache.lucene.util.RamUsageEstimator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 初始化账号机与店铺的关系
 *
 * @author menghui
 * @date 2021-06
 */
public class InitBaseMachineCache implements InitMachineCache {


    @Autowired
    private MachineService machineService;

    @Autowired
    MachineWorkTypeService machineWorkTypeService;

    @Autowired
    MachineWorkTypeTaskService machineWorkTypeTaskService;
    
    @Autowired
    MachineCrawlPlatformService machineCrawlPlatformService;

    @Autowired
    StoreAccountService storeAccountService;

    @Autowired
    ApplicationContext context;

    @Autowired
    InitTaskCache initTaskCache;

    @Autowired
    TaskService taskService;

    @Autowired
    PlatformService platformService;

    @Autowired
    InitPlatformCache initPlatformCache;

    private static volatile  Map<String, Machine> machineCacheMap = new ConcurrentHashMap<>();

    @PostConstruct
    @Override
    public void init() {

    }

    public void updateMachineCache(Machine machine){
        updateMachineWorkType(machine);
        if(!StringUtils.isEmpty(machine.getIp())){
            updateMachineCacheMapCacheByIp(machine.getIp(), machine);
        }
    }

    public void updateMachineWorkType(Machine machine){
        List<MachineWorkType> machineWorkTypeByMachineIdStatus = machineWorkTypeService.getMachineWorkTypeByMachineIdStatus(machine.getId(), Constant.STATUS_VALID);
        for (MachineWorkType workTypeByMachineIdStatus : machineWorkTypeByMachineIdStatus) {
            List<MachineWorkTypeTask> machineWorkTypeTaskByWorkTypeIdStatus = machineWorkTypeTaskService.getMachineWorkTypeTaskByMachineIdAndWorkTypeId(workTypeByMachineIdStatus.getMachineId(), workTypeByMachineIdStatus.getId());
            List<String> list = new ArrayList<>();
            for (MachineWorkTypeTask workTypeTaskByWorkTypeIdStatus : machineWorkTypeTaskByWorkTypeIdStatus) {
                if(workTypeTaskByWorkTypeIdStatus.getStatus() == Constant.STATUS_INVALID){
                    continue;
                }
                String taskName = workTypeTaskByWorkTypeIdStatus.getTaskName();
                if(!list.contains(taskName)){
                    list.add(taskName);
                }
            }
            workTypeByMachineIdStatus.setWorkTypeTaskName(String.join(",",list));
        }
        machine.setMachineWorkTypeList(machineWorkTypeByMachineIdStatus);
    }


    /**
     *  更新机器缓存
     *  1、初始化
     *  2、Machine更新
     *  3、MachineWorkType
     *  4、Task更新
     * @param object
     */
    @Override
    public void updateMachineCacheMap(Object object){
        if(object == null){
            machineCacheMap.clear();
            getMachineCacheMapCache();
        }else if(object instanceof Machine){
            // 更新了机器表的可爬取平台
            String ip = ((Machine) object).getIp();
            if(!StringUtils.isEmpty(ip)){
                synchronized (ip.intern()){
                    updateMachineCacheByMachine((Machine)object);
                }
            }else {
                updateMachineCacheByMachine((Machine)object);
            }
            if(((Machine)object).getUpdateMachineStatus() == Constant.STATUS_VALID){
                //更新了机器状态 开启或禁用
                updateAccountHaveMachineByMachine((Machine) object);
            }


        } else if(object instanceof MachineWorkType){
            // 更新了机器任务类型表的 状态或可执行类型
            int id = ((MachineWorkType) object).getId();
            MachineWorkType machineTaskTypeById = machineWorkTypeService.getMachineWorkTypeById(id);
            int machineId = machineTaskTypeById.getMachineId();
            Machine machineById = machineService.getMachineById(machineId);
            String ip = machineById.getIp();
            if(!StringUtils.isEmpty(ip)){
                synchronized (ip.intern()){
                    updateMachineCacheByMachineWorkType(machineTaskTypeById);
                }
            }else {
                updateMachineCacheByMachineWorkType(machineTaskTypeById);
            }
            updateAccountHaveMachineByMachine(machineById);
        }else if(object instanceof Task){
            updateMachineCacheByTask((Task)object);
        }else if(object instanceof Platform){
            updateMachineCacheByPlatform((Platform)object);
        }
    }

    private void updateAccountHaveMachineByMachine(Machine machine) {
        Machine machineById = machineService.getMachineById(machine.getId());
        List<MachineWorkType> machineWorkTypeList = machineWorkTypeService.getMachineWorkTypeByIp(machineById.getIp());
        if(machineWorkTypeList == null || machineWorkTypeList.isEmpty()){
            return;
        }
        for (MachineWorkType machineWorkType : machineWorkTypeList) {
            if(machineWorkType.getPlatformType() == Constant.LARGE_TASK_TYPE_CRAWL_PLATFORM){
                continue;
            }
            StoreAccount storeAccount = new StoreAccount();
            storeAccount.setAccount(machineWorkType.getAccount());
            storeAccount.setPlatform(machineWorkType.getPlatform());
            storeAccount.setContinents(machineWorkType.getContinents());
            List<StoreAccount> storeAccountByAccountPlatformContinents = storeAccountService.getStoreAccountByAccountPlatformContinents(storeAccount);
            if(storeAccountByAccountPlatformContinents == null || storeAccountByAccountPlatformContinents.isEmpty()){
                continue;
            }
            for (StoreAccount account : storeAccountByAccountPlatformContinents) {
                int haveMachine = storeAccountService.checkIsHaveMachineByAccountContinents(account.getAccount(), account.getContinents());
                account.setHaveMachine(haveMachine);
                storeAccountService.updateHaveMachine(account);
            }

        }


    }


    /**
     *  机器更新处理逻辑
     * @param machine
     */
    private void updateMachineCacheByMachine(Machine machine) {

        int id = machine.getId();
        String machineTaskTypeLists = machine.getMachineTaskTypeLists();

        try {
            if(!StringUtils.isEmpty(machineTaskTypeLists)){
                //jackson对象
                ObjectMapper mapper = new ObjectMapper();
                //使用jackson将json转为List<User>
                JavaType jt = mapper.getTypeFactory().constructParametricType(ArrayList.class, MachineWorkType.class);
                List<MachineWorkType> machineWorkTypes =  (List<MachineWorkType>)mapper.readValue(machineTaskTypeLists, jt);
                if(machineWorkTypes != null && !machineWorkTypes.isEmpty()){
                    for (MachineWorkType machineTaskType : machineWorkTypes) {
                        updateMachineWorkTypeTaskList(machineTaskType);
                    }
                }
            }
            updateMachineWorkTypeList(machine);
            updateMachineCrawlPlatformList(machine);
            resetMachineCache(id);
        }catch (Exception e){
             e.printStackTrace();
        }
    }


    /**
     *  机器的大任务类型更新
     *  1、更新机器可爬取平台的状态，顺便更新机器有效的可爬取平台
     *  2、更新可执行的任务类型
     * @param machineWorkType
     */
    private void updateMachineCacheByMachineWorkType(MachineWorkType machineWorkType ) {

        if (machineWorkType.getPlatformType()== Constant.LARGE_TASK_TYPE_ACCOUNT_PLATFORM) {
            //账号平台 直接返回
            resetMachineCache( machineWorkType.getMachineId());
            return;
        }

        // 同步更新机器表的可爬取平台
        updateMachineCrawlPlatformListByMachineWorkType(machineWorkType);

        List<String> list = new ArrayList<>();
        List<MachineWorkTypeTask> machineWorkTypeTaskByWorkTypeIdStatus = machineWorkTypeTaskService.getMachineWorkTypeTaskByWorkTypeIdStatus(machineWorkType.getId(), Constant.STATUS_VALID);
        for (MachineWorkTypeTask machineWorkTypeTask : machineWorkTypeTaskByWorkTypeIdStatus) {
            if (!list.contains(String.valueOf(machineWorkTypeTask.getTaskId()))) {
                list.add(String.valueOf(machineWorkTypeTask.getTaskId()));
            }
        }
        machineWorkType.setTaskTypeName(String.join(",",list));

        updateMachineWorkTypeTaskList(machineWorkType);

        // 重置machineShopMap缓存 只有一个
        resetMachineCache( machineWorkType.getMachineId());
    }


    /**
     *  更新任务 影响到机器的可执行任务类型的变更
     *  1、修改任务状态 要是修改为无效 需要把所有机器的当前可执行类型移除
     *  2、修改任务可执行大类型 也要对应修改可执行任务
     * @param task
     */
    @SneakyThrows
    private void updateMachineCacheByTask(Task task){
        Task taskByUniqueId = taskService.getTaskByUniqueId(task.getId());
        Map<String, Machine> machineCacheMapCache = getMachineCacheMapCache();
        for (Machine machine : machineCacheMapCache.values()) {
            boolean isUpdate = false;
            List<MachineWorkType> machineTaskTypeByMachineId = machineWorkTypeService.getMachineWorkTypeByMachineId(machine.getId());
            for (MachineWorkType machineWorkType : machineTaskTypeByMachineId) {
                if(updateTaskTypeNameByTaskStatus(taskByUniqueId,  machineWorkType)){
                    isUpdate = true;
                }
            }
            if(isUpdate){
                updateMachineCache(machine);
            }
        }
    }

    @SneakyThrows
    private void updateMachineCacheByPlatform(Platform platform){
        List<MachineWorkType> machineWorkTypeList = machineWorkTypeService.getMachineWorkTypeByPlatFormIdAndPlatformType(platform.getId(), Constant.LARGE_TASK_TYPE_CRAWL_PLATFORM);
        if(machineWorkTypeList == null || machineWorkTypeList.isEmpty()){
            return;
        }
        for (MachineWorkType machineWorkType : machineWorkTypeList) {
            machineWorkType.setIsBrowser(platform.getIsBrowser());
            machineWorkTypeService.updateMachineWorkTypeIsBrowser(machineWorkType);
            resetMachineCache(machineWorkType.getMachineId());
        }

    }


    /**
     *  用来处理 每种大任务下的小任务
     * @param machineWorkType
     */
    @SneakyThrows
    private void updateMachineWorkTypeTaskList(MachineWorkType machineWorkType){
        // 交集 需要检查状态的
        List<String> idListRetain = new ArrayList<>();
        //listCrawlPlatforms中移除idList有的 需要新增的
        List<String> idListAdd = new ArrayList<>();
        //idList 中移除 listCrawlPlatforms 需要删除的
        List<String> idListRemove = new ArrayList<>();

        String taskTypeName = machineWorkType.getTaskTypeName();

        List<String> listCrawlPlatforms ;
        if(StringUtils.isEmpty(taskTypeName)){
            listCrawlPlatforms = new ArrayList<>();
        }else {
            listCrawlPlatforms = Arrays.asList(taskTypeName.split(","));
        }
        List<MachineWorkTypeTask> machineWorkTypeTaskByWorkTypeId = machineWorkTypeTaskService.getMachineWorkTypeTaskByWorkTypeId(machineWorkType.getId());
        Map<String,MachineWorkTypeTask> businessSystemTaskBySystemIdMap = new HashMap();
        if (machineWorkTypeTaskByWorkTypeId == null || machineWorkTypeTaskByWorkTypeId.isEmpty()) {
            idListAdd.addAll(listCrawlPlatforms);
        }else {
            List<String> idList = new ArrayList<>();
            for (MachineWorkTypeTask machineWorkTypeTask : machineWorkTypeTaskByWorkTypeId) {
                int taskId = machineWorkTypeTask.getTaskId();
                if (!idList.contains(taskId)) {
                    idList.add(String.valueOf(taskId));
                }
                businessSystemTaskBySystemIdMap.put(String.valueOf(taskId), machineWorkTypeTask);
            }
            idListRetain.addAll(idList);
            idListAdd.addAll(listCrawlPlatforms);
            idListRemove.addAll(idList);

            idListRetain.retainAll(listCrawlPlatforms);
            idListRemove.removeAll(listCrawlPlatforms);
            idListAdd.removeAll(idList);
        }

        for (String s : idListAdd) {
            MachineWorkTypeTask machineWorkTypeTask = new MachineWorkTypeTask();
            machineWorkTypeTask.setTaskId(Integer.parseInt(s));
            Task taskById = taskService.findTaskById(Integer.parseInt(s));
            machineWorkTypeTask.setTaskName(taskById.getTaskName());
            machineWorkTypeTask.setWorkTypeId(machineWorkType.getId());
            machineWorkTypeTask.setMachineId(machineWorkType.getMachineId());
            machineWorkTypeTask.setStatus(Constant.STATUS_VALID);
            machineWorkTypeTask.setCreatedTime(DateUtils.getAfterDays(0));

            machineWorkTypeTaskService.insertMachineWorkTypeTask(machineWorkTypeTask);
        }

        for (String s : idListRemove) {
            machineWorkTypeTaskService.updateMachineWorkTypeTaskStatusByWorkTypeIdAndTaskId(machineWorkType.getId(), Integer.parseInt(s), Constant.STATUS_INVALID, Constant.STATUS_IS_UPDATE);
        }

        for (String s : idListRetain) {
            MachineWorkTypeTask machineWorkTypeTask = businessSystemTaskBySystemIdMap.get(s);
            if (machineWorkTypeTask.getStatus() != Constant.STATUS_VALID) {
                machineWorkTypeTask.setStatus(Constant.STATUS_VALID);
                machineWorkTypeTaskService.updateMachineWorkTypeTaskStatusByWorkTypeIdAndTaskId(machineWorkTypeTask.getWorkTypeId(), machineWorkTypeTask.getTaskId(), Constant.STATUS_VALID, machineWorkTypeTask.getIsUpdate());
            }
        }
    }



    @SneakyThrows
    private boolean updateTaskTypeNameByTaskStatus(Task taskByUniqueId, MachineWorkType machineWorkType){
        // 交集 需要检查状态的
        List<String> idListRetain = new ArrayList<>();
        //listCrawlPlatforms中移除idList有的 需要新增的
        List<String> idListAdd = new ArrayList<>();
        //idList 中移除 listCrawlPlatforms 需要删除的
        List<String> idListRemove = new ArrayList<>();

        boolean isContains = false;
        boolean isEqualAccount = (machineWorkType.getPlatformType() == Constant.LARGE_TASK_TYPE_ACCOUNT_PLATFORM && taskByUniqueId.getLargeTaskType() == Constant.LARGE_TASK_TYPE_ACCOUNT_PLATFORM) ? true: false;
        boolean isEqualCrawl = (machineWorkType.getPlatformType() == Constant.LARGE_TASK_TYPE_CRAWL_PLATFORM &&
                taskByUniqueId.getLargeTaskType() == Constant.LARGE_TASK_TYPE_CRAWL_PLATFORM &&
                String.valueOf(machineWorkType.getPlatformId()).equals(taskByUniqueId.getExecutePlatforms())) ? true: false;
        boolean isEqual = (isEqualAccount || isEqualCrawl)? true: false;

        List<MachineWorkTypeTask> machineWorkTypeTasks = machineWorkTypeTaskService.getMachineWorkTypeTaskByMachineIdAndWorkTypeId(machineWorkType.getMachineId(), machineWorkType.getId());
        Map<String,MachineWorkTypeTask> machineWorkTypeTaskMap = new HashMap<>();
        for (MachineWorkTypeTask workTypeTask : machineWorkTypeTasks) {
            if(taskByUniqueId.getId() == workTypeTask.getTaskId()){
                isContains = true;
                machineWorkTypeTaskMap.put(String.valueOf(taskByUniqueId.getId()),workTypeTask);
            }
        }
        if(isContains){
            //需要移除
            if(taskByUniqueId.getStatus() == Constant.STATUS_INVALID || !isEqual){
                idListRemove.add(String.valueOf(taskByUniqueId.getId()));
            }else if(taskByUniqueId.getStatus() == Constant.STATUS_VALID && isEqual){
                idListRetain.add(String.valueOf(taskByUniqueId.getId()));
            }
        }else{
            //需要新增
            if(taskByUniqueId.getStatus() == Constant.STATUS_VALID && isEqual){
                idListAdd.add(String.valueOf(taskByUniqueId.getId()));
            }
        }

        for (String s : idListAdd) {
            MachineWorkTypeTask machineWorkTypeTask = new MachineWorkTypeTask();
            machineWorkTypeTask.setTaskId(Integer.parseInt(s));
            machineWorkTypeTask.setTaskName(taskByUniqueId.getTaskName());
            machineWorkTypeTask.setWorkTypeId(machineWorkType.getId());
            machineWorkTypeTask.setMachineId(machineWorkType.getMachineId());
            machineWorkTypeTask.setStatus(Constant.STATUS_VALID);
            machineWorkTypeTask.setCreatedTime(DateUtils.getAfterDays(0));
            machineWorkTypeTaskService.insertMachineWorkTypeTask(machineWorkTypeTask);
            return true;
        }

        for (String s : idListRemove) {
            MachineWorkTypeTask machineWorkTypeTask = machineWorkTypeTaskMap.get(s);
            if(machineWorkTypeTask.getStatus() == Constant.STATUS_VALID){
                machineWorkTypeTask.setStatus(Constant.STATUS_INVALID);
                if(machineWorkTypeTask.getIsUpdate() == Constant.STATUS_IS_UPDATE_RESET){
                    machineWorkTypeTaskService.updateMachineWorkTypeTaskStatus(machineWorkTypeTask);
                }

                return true;
            }
        }

        for (String s : idListRetain) {
            MachineWorkTypeTask machineWorkTypeTask = machineWorkTypeTaskMap.get(s);
            if (machineWorkTypeTask.getStatus() != Constant.STATUS_VALID) {
                machineWorkTypeTask.setStatus(Constant.STATUS_VALID);
                if(machineWorkTypeTask.getIsUpdate() == Constant.STATUS_IS_UPDATE_RESET){
                    machineWorkTypeTaskService.updateMachineWorkTypeTaskStatus(machineWorkTypeTask);
                }

                return true;
            }
        }
        return false;
    }


    @Override
    public void updateMachineCacheMapCacheByIp(String ip, Machine machine){
        return;
    }

    @Override
    public Machine getMachineCacheMapCacheByIp(String ip){
        return null;
    }


    @Override
    public synchronized Map<String, Machine> getMachineCacheMapCache(){
        Map<String, Machine> machineCacheMapTmp = new ConcurrentHashMap<>();
        List<Machine> machineList = machineService.getMachineAll();
        for (Machine machine: machineList) {
            if(StringUtils.isEmpty(machine.getIp())){
                continue;
            }
            updateMachineWorkType(machine);
            machineCacheMapTmp.put(machine.getIp(), machine);
        }
        return machineCacheMapTmp;
    }



    private void resetMachineCache(int machineId){
        Machine machineById = machineService.getMachineById(machineId);
        updateMachineCache(machineById);
    }


    /**
     *  用来处理 机器的可爬取平台
     * @param machine
     */
    @SneakyThrows
    private void updateMachineCrawlPlatformList(Machine machine){

        // 交集 需要检查状态的
        List<String> idListRetain = new ArrayList<>();
        //listCrawlPlatforms中移除idList有的 需要新增的
        List<String> idListAdd = new ArrayList<>();
        //idList 中移除 listCrawlPlatforms 需要删除的
        List<String> idListRemove = new ArrayList<>();
        List<String> listCrawlPlatforms = null;
        String crawlPlatform = machine.getCrawlPlatform();
        if(StringUtils.isEmpty(crawlPlatform)){
            listCrawlPlatforms = new ArrayList<>();
        }else {
            listCrawlPlatforms = Arrays.asList(crawlPlatform.split(","));
        }

        Map<String,MachineCrawlPlatform> machineCrawlPlatformMap = new HashMap();
        List<MachineCrawlPlatform> machineCrawlPlatformByMachineId = machineCrawlPlatformService.getMachineCrawlPlatformByMachineId(machine.getId());
        if (machineCrawlPlatformByMachineId == null || machineCrawlPlatformByMachineId.isEmpty()) {
            idListAdd.addAll(listCrawlPlatforms);
        }else {
            List<String> idList = new ArrayList<>();
            for (MachineCrawlPlatform machineCrawlPlatform : machineCrawlPlatformByMachineId) {
                int platformId = machineCrawlPlatform.getPlatformId();
                if (!idList.contains(platformId)) {
                    idList.add(String.valueOf(platformId));
                }
                machineCrawlPlatformMap.put(String.valueOf(platformId), machineCrawlPlatform);
            }
            // 把数据库里面所有存在放入
            idListRetain.addAll(idList);
            idListRemove.addAll(idList);

            // 前端选择后的集合放入
            idListAdd.addAll(listCrawlPlatforms);

            idListRetain.retainAll(listCrawlPlatforms);
            idListRemove.removeAll(listCrawlPlatforms);
            idListAdd.removeAll(idList);
        }

        for (String s : idListAdd) {
            MachineCrawlPlatform machineCrawlPlatform = new MachineCrawlPlatform();
            machineCrawlPlatform.setMachineId(machine.getId());
            machineCrawlPlatform.setPlatformId(Integer.parseInt(s));
            Platform platform = platformService.getPlatformById(Integer.parseInt(s));
            machineCrawlPlatform.setPlatformName(platform.getPlatformName());
            machineCrawlPlatform.setMachineIp(machine.getIp());
            machineCrawlPlatform.setStatus(Constant.STATUS_VALID);
            machineCrawlPlatform.setCreatedTime(DateUtils.getAfterDays(0));
            machineCrawlPlatformService.insertMachineCrawlPlatform(machineCrawlPlatform);
        }

        for (String s : idListRemove) {
            MachineCrawlPlatform machineCrawlPlatform = machineCrawlPlatformMap.get(s);
            machineCrawlPlatform.setStatus(Constant.STATUS_INVALID);
            machineCrawlPlatformService.updateMachineCrawlPlatformStatusById(machineCrawlPlatform);
        }

        for (String s : idListRetain) {
            MachineCrawlPlatform machineCrawlPlatform = machineCrawlPlatformMap.get(s);
            if (machineCrawlPlatform.getStatus() != Constant.STATUS_VALID) {
                machineCrawlPlatform.setStatus(Constant.STATUS_VALID);
                machineCrawlPlatformService.updateMachineCrawlPlatformStatusById(machineCrawlPlatform);
            }
        }
    }


    /**
     *  通过机器的大任务类型的状态改变影响可爬取平台
     * @param machineWorkType
     */
    @SneakyThrows
    private void updateMachineCrawlPlatformListByMachineWorkType(MachineWorkType machineWorkType){

        // 交集 需要检查状态的
        List<String> idListRetain = new ArrayList<>();
        //listCrawlPlatforms中移除idList有的 需要新增的
        List<String> idListAdd = new ArrayList<>();
        //idList 中移除 listCrawlPlatforms 需要删除的
        List<String> idListRemove = new ArrayList<>();

        int machineId = machineWorkType.getMachineId();
        List<MachineCrawlPlatform> machineCrawlPlatformByMachineId = machineCrawlPlatformService.getMachineCrawlPlatformByMachineId(machineId);
        Map<String,MachineCrawlPlatform> machineCrawlPlatformMap = new HashMap();
        if (machineCrawlPlatformByMachineId == null || machineCrawlPlatformByMachineId.isEmpty()) {
            if (machineWorkType.getStatus() == Constant.STATUS_VALID){
                idListAdd.add(String.valueOf(machineWorkType.getPlatformId()));
            }
        }else {
            boolean isContain = false;
            for (MachineCrawlPlatform machineCrawlPlatform : machineCrawlPlatformByMachineId) {
                if(machineCrawlPlatform.getPlatformId() == machineWorkType.getPlatformId()){
                    isContain = true;
                    machineCrawlPlatformMap.put(String.valueOf(machineCrawlPlatform.getPlatformId()), machineCrawlPlatform);
                    break;
                }
            }
            if (isContain && machineWorkType.getStatus() == Constant.STATUS_VALID){
                idListRetain.add(String.valueOf(machineWorkType.getPlatformId()));
            } else if (isContain && machineWorkType.getStatus() == Constant.STATUS_INVALID){
                idListRemove.add(String.valueOf(machineWorkType.getPlatformId()));
            }else if (!isContain && machineWorkType.getStatus() == Constant.STATUS_INVALID){
//                idListRemove.add(String.valueOf(machineWorkType.getPlatformId()));
            }else if (!isContain && machineWorkType.getStatus() == Constant.STATUS_VALID){
                idListAdd.add(String.valueOf(machineWorkType.getPlatformId()));
            }
        }


        for (String s : idListAdd) {
            MachineCrawlPlatform machineCrawlPlatform = new MachineCrawlPlatform();
            machineCrawlPlatform.setMachineId(machineWorkType.getMachineId());
            machineCrawlPlatform.setMachineIp(machineWorkType.getMachineIp());
            machineCrawlPlatform.setPlatformId(Integer.parseInt(s));
            Platform platform = platformService.getPlatformById(Integer.parseInt(s));
            machineCrawlPlatform.setPlatformName(platform.getPlatformName());

            machineCrawlPlatform.setStatus(Constant.STATUS_VALID);
            machineCrawlPlatform.setCreatedTime(DateUtils.getAfterDays(0));
            machineCrawlPlatformService.insertMachineCrawlPlatform(machineCrawlPlatform);
        }

        for (String s : idListRemove) {
            MachineCrawlPlatform machineCrawlPlatform = machineCrawlPlatformMap.get(s);
            if (machineCrawlPlatform.getStatus() != Constant.STATUS_INVALID) {
                machineCrawlPlatform.setStatus(Constant.STATUS_INVALID);
                machineCrawlPlatformService.updateMachineCrawlPlatformStatusById(machineCrawlPlatform);
            }
        }

        for (String s : idListRetain) {
            MachineCrawlPlatform machineCrawlPlatform = machineCrawlPlatformMap.get(s);
            if (machineCrawlPlatform.getStatus() != Constant.STATUS_VALID) {
                machineCrawlPlatform.setStatus(Constant.STATUS_VALID);
                machineCrawlPlatformService.updateMachineCrawlPlatformStatusById(machineCrawlPlatform);
            }
        }
    }


    /**
     *  用来处理 机器的可爬取平台变化引起的大机器类型的变化(仅限爬虫平台)
     * @param machine
     */
    @SneakyThrows
    private void updateMachineWorkTypeList(Machine machine){

        // 交集 需要检查状态的
        List<String> idListRetain = new ArrayList<>();
        //listCrawlPlatforms中移除idList有的 需要新增的
        List<String> idListAdd = new ArrayList<>();
        //idList 中移除 listCrawlPlatforms 需要删除的
        List<String> idListRemove = new ArrayList<>();

        String crawlPlatform = machine.getCrawlPlatform();
        List<String> listCrawlPlatforms = null;
        if(StringUtils.isEmpty(crawlPlatform)){
            listCrawlPlatforms = new ArrayList<>();
        }else {
            listCrawlPlatforms = Arrays.asList(crawlPlatform.split(","));
        }
        Map<String,MachineWorkType> machineWorkTypeMap = new HashMap();
        List<MachineWorkType> machineCrawlPlatformByMachineId = machineWorkTypeService.getMachineWorkTypeByMachineId(machine.getId());
        if (machineCrawlPlatformByMachineId == null || machineCrawlPlatformByMachineId.isEmpty()) {
            idListAdd.addAll(listCrawlPlatforms);
        }else {
            List<String> idList = new ArrayList<>();
            for (MachineWorkType machineCrawlPlatform : machineCrawlPlatformByMachineId) {
                if (machineCrawlPlatform.getPlatformType() == Constant.LARGE_TASK_TYPE_CRAWL_PLATFORM) {
                    int platformId = machineCrawlPlatform.getPlatformId();
                    if (!idList.contains(platformId)) {
                        idList.add(String.valueOf(platformId));
                    }
                    machineWorkTypeMap.put(String.valueOf(platformId), machineCrawlPlatform);
                }
            }
            // 把数据库里面所有存在放入
            idListRetain.addAll(idList);
            idListRemove.addAll(idList);

            // 前端选择后的集合放入
            idListAdd.addAll(listCrawlPlatforms);

            idListRetain.retainAll(listCrawlPlatforms);
            idListRemove.removeAll(listCrawlPlatforms);
            idListAdd.removeAll(idList);
        }

        for (String s : idListAdd) {
            MachineWorkType machineWorkType = new MachineWorkType();
            machineWorkType.setMachineId(machine.getId());
            machineWorkType.setMachineIp(machine.getIp());
            machineWorkType.setPlatformId(Integer.parseInt(s));
            Platform platform = platformService.getPlatformById(Integer.parseInt(s));
            machineWorkType.setPlatform(platform.getPlatformName());
            machineWorkType.setStatus(Constant.STATUS_VALID);
            machineWorkType.setCreatedTime(DateUtils.getAfterDays(0));
            machineWorkType.setPlatformType(Constant.LARGE_TASK_TYPE_CRAWL_PLATFORM);
            machineWorkType.setIsBrowser(platform.getIsBrowser());
            machineWorkTypeService.insert(machineWorkType);

            List<Task> taskList = initTaskCache.getWorkTypesMapCache(platform.getPlatformName());
            for (Task task : taskList) {
                MachineWorkTypeTask machineWorkTypeTask = new MachineWorkTypeTask();
                machineWorkTypeTask.setWorkTypeId(machineWorkType.getId());
                machineWorkTypeTask.setMachineId(machine.getId());
                machineWorkTypeTask.setTaskId(task.getId());
                machineWorkTypeTask.setTaskName(task.getTaskName());
                machineWorkTypeTask.setCreatedTime(DateUtils.getAfterDays(0));
                machineWorkTypeTask.setStatus(Constant.STATUS_VALID);
                machineWorkTypeTaskService.insertMachineWorkTypeTask(machineWorkTypeTask);
            }
        }

        for (String s : idListRemove) {
            MachineWorkType machineWorkType = machineWorkTypeMap.get(s);
            if (machineWorkType.getStatus() != Constant.STATUS_INVALID) {
                machineWorkType.setStatus(Constant.STATUS_INVALID);
                machineWorkTypeService.updatePlatformTypeStatusById(machineWorkType);
            }
        }

        for (String s : idListRetain) {
            MachineWorkType machineWorkType = machineWorkTypeMap.get(s);
            if (machineWorkType.getStatus() != Constant.STATUS_VALID) {
                machineWorkType.setStatus(Constant.STATUS_VALID);
                machineWorkTypeService.updatePlatformTypeStatusById(machineWorkType);
            }
        }
    }

}
