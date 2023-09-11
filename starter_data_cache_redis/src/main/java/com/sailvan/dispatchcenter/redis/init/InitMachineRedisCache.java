package com.sailvan.dispatchcenter.redis.init;

import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.cache.InitPlatformCache;
import com.sailvan.dispatchcenter.common.cache.InitTaskCache;
import com.sailvan.dispatchcenter.common.cache.impl.InitBaseMachineCache;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.Machine;
import com.sailvan.dispatchcenter.common.pipe.MachineService;
import com.sailvan.dispatchcenter.common.pipe.TaskService;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 初始化账号机与店铺的关系
 *
 * @author menghui
 * @date 2021-06
 */
public class InitMachineRedisCache extends InitBaseMachineCache {

    @Autowired
    public MachineService machineService;

    @Autowired
    ApplicationContext context;

    @Autowired
    InitTaskCache initTaskCache;

    @Autowired
    TaskService taskService;

    @Autowired
    InitPlatformCache initPlatformCache;

    @Autowired
    RedisUtils redisUtils;

    public static volatile  Map<String, Machine> machineCacheMap = new ConcurrentHashMap<>();

    private static String PREFIX = "machine:";

    @Override
    public void updateMachineCacheMapCacheByIp(String ip, Machine machine){
        synchronized (ip.intern()){
            redisUtils.put(PREFIX+ip, JSONObject.toJSONString(machine), Constant.EFFECTIVE);
        }
    }

    @Override
    public Machine getMachineCacheMapCacheByIp(String ip){
        synchronized (ip.intern()){
            Object o = redisUtils.get(PREFIX+ip);
            if (StringUtils.isEmpty(o)) {
                return null;
            }
            Machine machine = JSONObject.parseObject(String.valueOf(o), Machine.class);
            return machine;
        }
    }
}
