package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.domain.MachineCrawlPlatform;
import com.sailvan.dispatchcenter.db.dao.automated.MachineCrawlPlatformDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author mh
 * @date 2021-06
 */
@Service
public class MachineCrawlPlatformService implements com.sailvan.dispatchcenter.common.pipe.MachineCrawlPlatformService {

    private static Logger logger = LoggerFactory.getLogger(MachineCrawlPlatformService.class);

    @Autowired
    MachineCrawlPlatformDao machineCrawlPlatformDao;


    @Override
    public List<MachineCrawlPlatform> getMachineCrawlPlatformByMachineIdStatus(int machineId, Integer status) {
        return machineCrawlPlatformDao.getMachineCrawlPlatformByMachineIdStatus(machineId, status);
    }

    @Override
    public List<MachineCrawlPlatform> getMachineCrawlPlatformByMachineId(int machineId) {
        return machineCrawlPlatformDao.getMachineCrawlPlatformByMachineId(machineId);
    }

    @Override
    public MachineCrawlPlatform getMachineCrawlPlatformById(Integer id) {
        return machineCrawlPlatformDao.getMachineCrawlPlatformById(id);
    }

    @Override
    public int updateMachineCrawlPlatformStatus(MachineCrawlPlatform machine) {
        return machineCrawlPlatformDao.updateMachineCrawlPlatformStatus(machine);
    }

    @Override
    public int updateMachineCrawlPlatformStatusById(MachineCrawlPlatform machine) {
        return machineCrawlPlatformDao.updateMachineCrawlPlatformStatusById(machine);
    }

    @Override
    public int insertMachineCrawlPlatform(MachineCrawlPlatform machine) {
        return machineCrawlPlatformDao.insertMachineCrawlPlatform(machine);
    }
}
