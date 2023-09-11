package com.sailvan.dispatchcenter.db.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sailvan.dispatchcenter.common.cache.InitPlatformCache;
import com.sailvan.dispatchcenter.common.cache.InitTaskCache;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.db.dao.automated.MachineCrawlPlatformDao;
import com.sailvan.dispatchcenter.db.dao.automated.MachineDao;
import com.sailvan.dispatchcenter.db.dao.automated.MachineWorkTypeDao;
import com.sailvan.dispatchcenter.db.dao.automated.MachineWorkTypeTaskDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mh
 * @date 2021-06
 */
@Service
public class MachineWorkTypeService implements com.sailvan.dispatchcenter.common.pipe.MachineWorkTypeService {

    private static Logger logger = LoggerFactory.getLogger(MachineWorkTypeService.class);

    @Autowired
    MachineWorkTypeDao machineWorkTypeDao;

    @Autowired
    MachineWorkTypeTaskDao machineWorkTypeTaskDao;

    @Autowired
    MachineCrawlPlatformDao machineCrawlPlatformDao;
    
    @Autowired
    MachineDao machineDao;

    @Autowired
    InitTaskCache initTaskCache;

    @Autowired
    InitPlatformCache initPlatformCache;


    @Override
    public List<MachineWorkType> getMachineWorkTypeByMachineId(int machineId) {
        List<MachineWorkType> list = machineWorkTypeDao.getMachineWorkTypeByMachineId(machineId);
        return list;
    }

    @Override
    public List<MachineWorkType> getMachineWorkTypeByMachineIdStatus(int machineId, int status) {
        List<MachineWorkType> list = machineWorkTypeDao.getMachineWorkTypeByMachineIdStatus(machineId, status);
        return list;
    }


    @Override
    public PageDataResult getMachineWorkTypeList(MachineWorkType machine, Integer pageNum, Integer pageSize) {

//        PageHelper.startPage(pageNum, pageSize);
        List<MachineWorkType> machineList = machineWorkTypeDao.getMachineWorkTypeByMachineId(machine.getMachineId());
        List<MachineWorkType> machineTaskTypeList = new ArrayList<>();
        Map<String,List<Task>> stringListMap = new HashMap<>();
        Map<String,String> platformNameMap = new HashMap<>();

        for (MachineWorkType machineWorkType : machineList) {

            List<MachineWorkTypeTask> machineWorkTypeTaskByWorkTypeIdStatus = machineWorkTypeTaskDao.getMachineWorkTypeTaskByWorkTypeIdStatus(machineWorkType.getId(), Constant.STATUS_VALID);
            List<String> list = new ArrayList();

            for (MachineWorkTypeTask workTypeTaskByWorkTypeIdStatus : machineWorkTypeTaskByWorkTypeIdStatus) {
                if (!list.contains(String.valueOf(workTypeTaskByWorkTypeIdStatus.getTaskId()))) {
                    list.add(String.valueOf(workTypeTaskByWorkTypeIdStatus.getTaskId()));
                }
            }
            machineWorkType.setTaskTypeName(String.join(",", list));
            setTaskType(machineWorkType,stringListMap,platformNameMap);

            machineTaskTypeList.add(machineWorkType);
        }

        PageDataResult pageDataResult = new PageDataResult();
        if(machineTaskTypeList.size() != 0){
            PageInfo<MachineWorkType> pageInfo = new PageInfo<>(machineTaskTypeList);
            pageDataResult.setList(machineTaskTypeList);
            pageDataResult.setTotals((int) pageInfo.getTotal());
        }
        return pageDataResult;
    }



    @Override
    public List<MachineWorkType> getMachineWorkTypeByMachineWorkType(MachineWorkType machinePlatform) {
        List<MachineWorkType> list = machineWorkTypeDao.getMachineWorkTypeByMachineWorkType(machinePlatform);
        return list;
    }


    @Override
    public int update(MachineWorkType machine){
        return machineWorkTypeDao.updateMachineWorkType(machine);
    }

    @Override
    public int updateMachineWorkTypeStatusByIp(String machineIp, int status,int isUpdate){
        return machineWorkTypeDao.updateMachineWorkTypeStatusByIp(machineIp, status, isUpdate);
    }

    @Override
    public int updatePlatformTypeIsUpdateStatusById(Integer id, int status, int isUpdate){
        return machineWorkTypeDao.updatePlatformTypeIsUpdateStatusById(id, status,isUpdate);
    }

    @Override
    public int updateMachineWorkTypeTaskTypeName(Integer id, String taskTypeName){
        return machineWorkTypeDao.updateMachineWorkTypeTaskTypeName(id, taskTypeName);
    }

    @Override
    public MachineWorkType getMachineWorkTypeById(Integer id){
        return machineWorkTypeDao.getMachineWorkTypeById(id);
    }
    @Override
    public List<MachineWorkType> getMachineWorkTypeByIp(String machineIp){
        return machineWorkTypeDao.getMachineWorkTypeByIp(machineIp);
    }

    @Override
    public List<MachineWorkType> getMachineWorkTypeByPlatFormIdAndPlatformType(Integer platformId, Integer platformType) {
        return machineWorkTypeDao.getMachineWorkTypeByPlatFormIdAndPlatformType(platformId, platformType);
    }

    @Override
    public List<MachineWorkType> getMachineWorkTypeByPlatformType(int platformType){
        return machineWorkTypeDao.getMachineWorkTypeByPlatformType(platformType);
    }


    @Override
    public int updateStatus(MachineWorkType machine){
        return machineWorkTypeDao.updateMachineWorkTypeStatus(machine);
    }

    @Override
    public int updateMachineWorkTypeIsBrowser(MachineWorkType machine){
        return machineWorkTypeDao.updateMachineWorkTypeIsBrowser(machine);
    }

    @Override
    public int updatePlatformTypeStatus(MachineWorkType machine){
        return machineWorkTypeDao.updateMachineWorkTypePlatformTypeStatus(machine);
    }

    @Override
    public int updatePlatformTypeStatusById(MachineWorkType machine){
        return machineWorkTypeDao.updatePlatformTypeStatusById(machine);
    }


    @Override
    public int insert(MachineWorkType machine){
        return machineWorkTypeDao.insertMachineWorkType(machine);
    }

    private void setTaskType(MachineWorkType machineWorkType, Map<String,List<Task>> stringListMap, Map<String,String> platformNameMap){
        JSONArray array = new JSONArray();
        List<Task> list = new ArrayList<>();
        List<String> name = new ArrayList<>();
        List<Task> tmp = new ArrayList<>();
        if(platformNameMap.containsKey(String.valueOf(machineWorkType.getPlatformId()))){
            machineWorkType.setPlatformName(platformNameMap.get(String.valueOf(machineWorkType.getPlatformId())));
        }else {
            List<String> zh = initPlatformCache.getCrawlPlatformNameByPlatformId(String.valueOf(machineWorkType.getPlatformId()), "zh");
            if(zh != null && !zh.isEmpty()){
                machineWorkType.setPlatformName(zh.get(0));
                platformNameMap.put(String.valueOf(machineWorkType.getPlatformId()),zh.get(0));
            }
        }

        if(machineWorkType.getPlatformType() == Constant.LARGE_TASK_TYPE_ACCOUNT_PLATFORM){
            if(stringListMap.containsKey("Account")){
                list = stringListMap.get("Account");
            }else {
                list = initTaskCache.getWorkTypesMapCache("Account");
                stringListMap.put("Account",list);
            }

        }else {
            if(!StringUtils.isEmpty(machineWorkType.getPlatform())){
                if(stringListMap.containsKey(machineWorkType.getPlatform())){
                    list = stringListMap.get(machineWorkType.getPlatform());
                }else {
                    list = initTaskCache.getWorkTypesMapCache(machineWorkType.getPlatform());
                    stringListMap.put(machineWorkType.getPlatform(),list);
                }
            }
        }
        if (list != null && list.size()>=0) {
            for (Task s : list) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name",s.getTaskName());
                jsonObject.put("value",s.getId());
                array.add(jsonObject);
                tmp.add(s);
                name.add(s.getTaskName());
            }
        }

        String[] str = name.toArray(new String[name.size()]);
        machineWorkType.setTaskTypeNameStringArray(str);
        machineWorkType.setTaskTypeNameArray(array);
    }

}
