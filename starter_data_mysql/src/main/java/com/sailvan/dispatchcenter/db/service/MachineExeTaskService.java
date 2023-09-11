package com.sailvan.dispatchcenter.db.service;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sailvan.dispatchcenter.common.cache.InitAccountCache;
import com.sailvan.dispatchcenter.common.cache.InitPlatformCache;
import com.sailvan.dispatchcenter.common.cache.InitTaskCache;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.CommonUtils;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.db.dao.automated.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * @author mh
 * @date 2021-04
 */
@Service
public class MachineExeTaskService implements com.sailvan.dispatchcenter.common.pipe.MachineExeTaskService {

    private static Logger logger = LoggerFactory.getLogger(MachineExeTaskService.class);

    @Autowired
    private MachineExeTaskDao machineExeTaskDao;


    @Override
    public List<MachineExeTask> getMachineExeTaskByIpTaskNamePeriod(String ip, String taskType, String period) {
        return machineExeTaskDao.getMachineExeTaskByIpTaskNamePeriod(ip, taskType, period);
    }

    @Override
    public int updateMachineExeTask(MachineExeTask machineExeTask) {
        return machineExeTaskDao.updateMachineExeTask(machineExeTask);
    }

    @Override
    public int insertMachineExeTask(MachineExeTask machineExeTask) {
        return machineExeTaskDao.insertMachineExeTask(machineExeTask);
    }
}
