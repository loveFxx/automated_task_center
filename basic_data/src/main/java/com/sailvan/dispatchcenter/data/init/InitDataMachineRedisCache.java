package com.sailvan.dispatchcenter.data.init;

import com.sailvan.dispatchcenter.common.domain.Machine;
import com.sailvan.dispatchcenter.common.pipe.MachineService;
import com.sailvan.dispatchcenter.data.plugs.InitCacheMarkerConfiguration;
import com.sailvan.dispatchcenter.redis.init.InitMachineRedisCache;
import org.apache.lucene.util.RamUsageEstimator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.List;

@Primary
@ConditionalOnBean(name = "initCacheMarker")
public class InitDataMachineRedisCache extends InitMachineRedisCache {

    @Override
    public void init() {
        List<Machine> machineList = machineService.getMachineAll();
        System.out.println("machineList:"+machineList.size());
        for (Machine machine : machineList) {
            if(StringUtils.isEmpty(machine.getIp())){
                continue;
            }
            updateMachineCache(machine);
        }
        long l = RamUsageEstimator.sizeOf(machineCacheMap.toString().getBytes());
        System.out.println("machineList init ...  size:"+(l/(1024))+"KB "+(l/(1024*1024))+"MB");
    }
}
