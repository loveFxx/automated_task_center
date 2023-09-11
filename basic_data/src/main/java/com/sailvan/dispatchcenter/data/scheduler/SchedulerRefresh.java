package com.sailvan.dispatchcenter.data.scheduler;

import com.sailvan.dispatchcenter.common.cache.InitMachineCache;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.Machine;
import com.sailvan.dispatchcenter.db.service.MachineService;
import com.sailvan.dispatchcenter.db.service.StoreAccountService;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

import static com.sailvan.dispatchcenter.common.constant.Constant.HEAT_BEAT_INTERVAL;

/**
 * 定时刷新机器及账号信息
 * @author mh
 * @date 2021
 */
@Component
public class SchedulerRefresh {

    private static Logger logger = LoggerFactory.getLogger(SchedulerRefresh.class);

    @Autowired
    StoreAccountService storeAccountService;

    @Autowired
    InitMachineCache initMachineCache;

    @Autowired
    MachineService machineService;

    @Scheduled(cron = "0 0 4 * * ?")
    public void refreshMiNiMachine(){
        logger.info("SchedulerRefresh refreshMiNiMachine start");
        storeAccountService.refreshMiNiMachine();
        logger.info("SchedulerRefresh refreshMiNiMachine over");
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void refreshMiNi(){
        logger.info("SchedulerRefresh refreshMiNi start");
        storeAccountService.refreshMiNiAccountProxyIpAccountSite();
        logger.info("SchedulerRefresh refreshMiNi over");
    }


    /**
     *  5分钟检查一次心跳
     */
    @SneakyThrows
    @Scheduled(cron = "0 */10 * * * ?")
    public void refreshInvalidMachineByHeartBeat(){
        logger.info("SchedulerRefresh refreshInvalidMachineByHeartBeat ...");
        Map<String, Machine> machineCacheMapCache = initMachineCache.getMachineCacheMapCache();
        if(machineCacheMapCache == null){
            return;
        }
        for (Map.Entry<String, Machine> stringMachineEntry : machineCacheMapCache.entrySet()) {
            boolean isInvalid = false;
            Machine machine = stringMachineEntry.getValue();
            String lastHeartbeat = machine.getLastHeartbeat();
            if(StringUtils.isEmpty(lastHeartbeat)){
                isInvalid = true;
            }else {
                String currentTime = DateUtils.getAfterDays(0);
                long interval = DateUtils.calTime(currentTime, lastHeartbeat);
                if ( interval > 6*HEAT_BEAT_INTERVAL ) {
                    isInvalid = true;
                }
            }

            if(isInvalid && machine.getStatus() == Constant.STATUS_VALID && machine.getMachineType() != Constant.STATUS_INVALID){
                int update = machineService.updateMachineStatus(machine.getId(), Constant.STATUS_INVALID);
                if(update > 0){
                    logger.error("refreshInvalidMachineByHeartBeat machineIP:{} is invalid",machine.getIp());
                    initMachineCache.updateMachineCacheMapCacheByIp(machine.getIp(), machine);
                }
            }
        }
    }

}
