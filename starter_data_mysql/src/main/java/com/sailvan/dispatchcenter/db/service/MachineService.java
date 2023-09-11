package com.sailvan.dispatchcenter.db.service;

import com.alibaba.fastjson.JSONArray;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.db.dao.automated.*;
import com.sailvan.dispatchcenter.common.cache.InitAccountCache;
import com.sailvan.dispatchcenter.common.cache.InitPlatformCache;
import com.sailvan.dispatchcenter.common.cache.InitTaskCache;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.CommonUtils;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * @author mh
 * @date 2021-04
 */
@Service
public class MachineService implements com.sailvan.dispatchcenter.common.pipe.MachineService {

    private static Logger logger = LoggerFactory.getLogger(MachineService.class);

    @Autowired
    private MachineDao machineDao;


//    @Autowired
//    MachineTaskTypeDao machineTaskTypeDao;

    @Autowired
    MachineWorkTypeDao machineWorkTypeDao;

    @Autowired
    MachineWorkTypeTaskDao machineWorkTypeTaskDao;

    @Autowired
    MachineCrawlPlatformDao machineCrawlPlatformDao;

    @Autowired
    StoreAccountDao storeAccountDao;

    @Autowired
    TaskDao taskDao;

    @Autowired
    InitTaskCache initTaskCache;

    @Autowired
    InitAccountCache initAccountCache;

    @Autowired
    InitPlatformCache initPlatformCache;

    @Override
    public List<Machine> getMachineAll() {
        List<Machine> list = machineDao.getMachineAll();
        return list;
    }


    @Override
    public void refreshMachineWorkTypeTask() throws Exception {
        logger.info("refreshMachineWorkTypeTask...");
        List<Task> taskList = taskDao.listTask();
        List<MachineWorkType> machineWorkTypeByPlatformType = machineWorkTypeDao.getMachineWorkTypeByPlatformType(Constant.LARGE_TASK_TYPE_ACCOUNT_PLATFORM);
        for (Task task : taskList) {
            if (task.getLargeTaskType() != Constant.LARGE_TASK_TYPE_ACCOUNT_PLATFORM) {
                continue;
            }
            for (MachineWorkType machineWorkType : machineWorkTypeByPlatformType) {
                if (StringUtils.isEmpty(machineWorkType.getMachineIp())) {
                    continue;
                }
                List<MachineWorkTypeTask> list = machineWorkTypeTaskDao.getMachineWorkTypeTaskByMachineIdAndWorkTypeIdAndTaskId(machineWorkType.getMachineId(), machineWorkType.getId(), task.getId());
                if (list == null || list.isEmpty()) {
                    MachineWorkTypeTask machineWorkTypeTask = new MachineWorkTypeTask();
                    machineWorkTypeTask.setMachineId(machineWorkType.getMachineId());
                    machineWorkTypeTask.setWorkTypeId(machineWorkType.getId());
                    machineWorkTypeTask.setTaskId(task.getId());
                    machineWorkTypeTask.setTaskName(task.getTaskName());
                    machineWorkTypeTask.setStatus(Constant.STATUS_VALID);
                    machineWorkTypeTask.setCreatedTime(DateUtils.getAfterDays(0));
                    machineWorkTypeTaskDao.insertMachineWorkTypeTask(machineWorkTypeTask);
                    logger.error("INSERT...{}",machineWorkTypeTask.toString());
                }else {
                    for (MachineWorkTypeTask machineWorkTypeTask : list) {
                        if (machineWorkTypeTask.getStatus() == Constant.STATUS_INVALID) {
                            machineWorkTypeTask.setStatus(Constant.STATUS_VALID);
                            machineWorkTypeTask.setIsUpdate(Constant.STATUS_IS_UPDATE_RESET);
                            machineWorkTypeTaskDao.updateMachineWorkTypeTask(machineWorkTypeTask);
                            logger.error("UPDATE...{}",machineWorkTypeTask.toString());
                        }
                    }
                }

            }

        }
    }

    @Override
    public Machine getMachineById(Integer id) {
        return machineDao.getMachineById(id);
    }

    @Override
    public PageDataResult getMachineList(Machine machine, Integer pageNum, Integer pageSize) throws Exception {

        String empty = "-1";
        if (!empty.equals(String.valueOf(machine.getExpiring()))) {
            int expiring = machine.getExpiring();
            machine.setStartTime(DateUtils.getAfterDays(expiring, DateUtils.DATE_FORMAT_DAY));
            machine.setEndTime(DateUtils.getAfterDays(expiring + 1, DateUtils.DATE_FORMAT_DAY));
        }
        String ids = getRetainId(machine);
        machine.setIds(ids);

        PageHelper.startPage(pageNum, pageSize);

        List<Machine> machineList = machineDao.getMachineByMachine(machine);
        PageInfo<Machine> pageInfoOld = new PageInfo<>(machineList);

        List<Machine> machineLists = new ArrayList<>();
        int insert = 0;
        for (Machine machineInfo : machineList) {
            // 设置机器执行的任务类型
            List<MachineWorkType> machineTaskTypeByMachineId = machineWorkTypeDao.getMachineWorkTypeByMachineId(machineInfo.getId());
            List<MachineWorkType> machineTaskTypeList = new ArrayList<>();
            // 除去店铺的平台,选择可爬取
            JSONArray crawlPlatformSelect = new JSONArray();
            crawlPlatformSelect.addAll(initPlatformCache.getCrawlPlatformSelectCache());

            for (MachineWorkType machineTaskType : machineTaskTypeByMachineId) {
                //MachineWorkType查到loginPlatform
                StoreAccount storeAccount = new StoreAccount();
                storeAccount.setAccount("('" + machineTaskType.getAccount() + "')");
                storeAccount.setContinents(machineTaskType.getContinents());
                storeAccount.setStatus(-1);


                List<StoreAccount> storeAccountByStoreAccount = storeAccountDao.getStoreAccountByStoreAccount(storeAccount);
                if (storeAccountByStoreAccount != null && storeAccountByStoreAccount.size() != 0) {
                    int loginPlatform = storeAccountByStoreAccount.get(0).getLoginPlatform();
                    machineTaskType.setLoginPlatform(loginPlatform);
                }

                machineTaskTypeList.add(machineTaskType);

                if (machineInfo.getMachineType() == Constant.MACHINE_TYPE_INTRANET_VPS) {
                    // 如果是内网VPS 不做限制
                    continue;
                }
                if (machineTaskType.getPlatformType() == Constant.LARGE_TASK_TYPE_CRAWL_PLATFORM) {
                    // 去掉爬虫平台
                    continue;
                }
                if (!StringUtils.isEmpty(machineTaskType.getPlatform())) {
                    for (int i = 0; i < crawlPlatformSelect.size(); i++) {
                        initPlatformCache.getCrawlPlatformByRemoveAccountPlatform(crawlPlatformSelect, i, machineTaskType.getPlatform());
                    }
                }
            }

            machineInfo.setMachineWorkTypeList(machineTaskTypeList);
            machineInfo.setCrawlPlatformSelect(crawlPlatformSelect);

            List<String> listCrawls = new ArrayList();
            List<MachineCrawlPlatform> machineCrawlPlatformByMachineIdStatus = machineCrawlPlatformDao.getMachineCrawlPlatformByMachineIdStatus(machineInfo.getId(), Constant.STATUS_VALID);
            for (MachineCrawlPlatform crawlPlatformByMachineIdStatus : machineCrawlPlatformByMachineIdStatus) {
                if (!listCrawls.contains(String.valueOf(crawlPlatformByMachineIdStatus.getPlatformId()))) {
                    listCrawls.add(String.valueOf(crawlPlatformByMachineIdStatus.getPlatformId()));
                }
            }
            String joinCrawls = String.join(",", listCrawls);
            machineInfo.setCrawlPlatform(joinCrawls);
            List<String> crawlPlatformName = initPlatformCache.getCrawlPlatformNameByPlatformId(joinCrawls, "zh");
            if (crawlPlatformName != null) {
                machineInfo.setCrawlPlatformName(String.join(",", crawlPlatformName));
            }
            if (insert == 0) {
                // 5、为第一个机器对象 设置搜索框的值
                machineInfo.setAccountSelect(initAccountCache.getAccountMap());
                machineInfo.setTaskTypeSelect(initTaskCache.getTaskIdMapCache());
            }
            machineLists.add(machineInfo);
            insert++;
        }

        PageDataResult pageDataResult = new PageDataResult();
        if (machineList.size() != 0) {
            PageInfo<Machine> pageInfoNew = new PageInfo<>();
            // 处理 只能查询pageSize的bug
            BeanUtils.copyProperties(pageInfoOld, pageInfoNew);
            pageDataResult.setList(machineLists);
            pageDataResult.setTotals((int) pageInfoNew.getTotal());
            pageDataResult.setPageNum(pageNum);
        }
        return pageDataResult;
    }


    /**
     * shopFlag和taskTypeFlag 标识的目的为了判断listShopId是否是搜索结果为空,
     * 还是需要查询所有数据
     *
     * @param machine
     * @return
     */
    private String getRetainId(Machine machine) {
        String ids = "";

        List<Integer> listShopId = new ArrayList<>();
        boolean shopFlag = false;
        if (!StringUtils.isEmpty(machine.getAccount())) {
            shopFlag = true;
            String taskTypeName = CommonUtils.searchInValue(machine.getAccount());
            listShopId = machineWorkTypeDao.getMachineTypeByAccount(taskTypeName);

        }

        List<Integer> listTaskTypeId = new ArrayList<>();
        boolean taskTypeFlag = false;
        if (!StringUtils.isEmpty(machine.getTaskType())) {
            taskTypeFlag = true;
            String taskTypeName = CommonUtils.searchInValue(machine.getTaskType());
            listTaskTypeId = machineWorkTypeTaskDao.getMachineTaskTypeByTaskId(taskTypeName);

        }


        List<Integer> listCrawlPlatformId = new ArrayList<>();
        boolean crawlPlatformFlag = false;
        if (!StringUtils.isEmpty(machine.getCrawlPlatform())) {
            crawlPlatformFlag = true;
            String platFormIdList = CommonUtils.searchInValue(machine.getCrawlPlatform());
            listCrawlPlatformId = machineCrawlPlatformDao.getMachineByCrawlPlatform(platFormIdList);

        }

        //用到了的筛选
        List<List<Integer>> usedSearchedList = new ArrayList();

        List<Integer> res = new ArrayList();

        if (shopFlag) {
            usedSearchedList.add(listShopId);
        }
        if (taskTypeFlag) {
            usedSearchedList.add(listTaskTypeId);
        }
        if (crawlPlatformFlag) {
            usedSearchedList.add(listCrawlPlatformId);
        }
        //三个都没搜
        if (!shopFlag && !taskTypeFlag && !crawlPlatformFlag) {
            return "";
        }

        //有任意一个搜索结果为空
        if ((listShopId.isEmpty() && shopFlag) || (listTaskTypeId.isEmpty() && taskTypeFlag) || (listCrawlPlatformId.isEmpty() && crawlPlatformFlag)) {
            return "('-1')";
        } else {
            for (List<Integer> list : usedSearchedList) {
                if (res.isEmpty()) {
                    res = list;
                }
                res.retainAll(list);
            }
        }

        ids = "('" + org.apache.commons.lang.StringUtils.join(res, "','") + "')";
        return ids;
    }


    @Override
    public int update(Machine machine) {
        int result = machineDao.updateMachine(machine);
        return result;
    }

    @Override
    public int updateByHeatBeat(Machine machine) {
        int result = machineDao.updateByHeatBeat(machine);
        return result;
    }

    @Override
    public int updateMachineStatus(int id, int machineStatus) {
        int result = machineDao.updateMachineStatus(id, machineStatus);
        return result;
    }

    @Override
    public int updateStatus(int id, int status) {
        int result = machineDao.updateStatus(id, status);
        return result;
    }

    @Override
    public int updateLastWorkTaskByIp(Machine machine) {
        int result = machineDao.updateLastWorkTaskByIp(machine);
        return result;
    }

    @Override
    public int updateMachineUserPwd(int id, String username, String password) {
        int result = machineDao.updateMachineUserPwd(id, username, password);
        return result;
    }

    @Override
    public int insert(Machine machine) {
        int result = machineDao.insertMachine(machine);
        return result;
    }

    @Override
    public Machine select(Machine machine) {
        return machineDao.select(machine);
    }


    @Override
    public Machine getMachineByIP(String ip) {
        return machineDao.getMachineByIP(ip);
    }

    @Override
    public int delete(Integer id) {
        return machineDao.deleteMachineById(id);
    }

    @Override
    public List<Machine> getMachineByStandard(int interval) {
        List<Machine> machineList = machineDao.getMachineByStandard(interval);
        return machineList;
    }


    /**
     * 心跳与机器时间相差五分钟 timeDiff
     * atc_machine表中last_heartbeat,machine_local_time相差五分钟
     *
     * @param timeDiff 单位分钟
     * @return
     */
    @Override
    public List<Machine> getMachineWithBigTimeDiff(String timeDiff) {
        return machineDao.getMachineWithBigTimeDiff(timeDiff);
    }


    @Override
    public List<Machine> getMachineStatusOn() {
        return machineDao.getMachineStatusOn();
    }

    @Override
    public List<Machine> getMachineLackingMemory(String memory) {
        return machineDao.getMachineLackingMemory(memory);
    }


    @Override
    public List<Machine> countMachineWithoutNetWork() {
        return machineDao.countMachineWithoutNetWork();
    }

    /**
     * 各机器类型的总数
     *
     * @return
     */
    @Override
    public List<MachineTypeCountDTO> getMachineTotalCountGroupByType() {
        return machineDao.getMachineTotalCountGroupByType();
    }


    /**
     * 各机器类型有有效心跳的计数
     *
     * @param interval
     * @return
     */
    @Override
    public List<Map> getMachineWithLivingHeartbeatCountGroupByType(String interval) {
        return machineDao.getMachineWithLivingHeartbeatCountGroupByType(interval);
    }


    /**
     * 各类型的开启的机器的计数
     *
     * @return
     */
    @Override
    public List<MachineTypeCountDTO> getMachineStatusOnCountGroupByType() {
        return machineDao.getMachineStatusOnCountGroupByType();
    }


    /**
     * (机器类型:总数) left join (机器类型:具有心跳机器数) left join (机器类型:开启机器数)
     *
     * @param interval 心跳距离现在多久算作有效心跳
     * @return
     */
    @Override
    public List<MachineTypeCountDTO> getMachineTypeCount(String interval) {
        return machineDao.getMachineTypeCount(interval);
    }


    @Override
    public List<MachineVersionStatDTO> getMachineFatherVersionStat() {
        return machineDao.getMachineFatherVersionStat();
    }


    @Override
    public List<MachineVersionStatDTO> getMachineSonVersionStat() {
        return machineDao.getMachineSonVersionStat();
    }

    @Override
    public List<Machine> getMachineByType(String machineType) {
        return machineDao.getMachineByType(machineType);
    }


    @Override
    public List<Machine> getMachineByFatherVersion(String version) {
        return machineDao.getMachineByFatherVersion(version);
    }


    @Override
    public List<Machine> getMachineBySonVersion(String version) {
        return machineDao.getMachineBySonVersion(version);
    }

}
