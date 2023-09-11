package com.sailvan.dispatchcenter.common.cache;

import com.sailvan.dispatchcenter.common.domain.Machine;

import java.util.Map;

/**
 * 初始化账号机与店铺的关系
 *
 * @author menghui
 * @date 2021-06
 */
public interface InitMachineCache {

    public void init() ;

    public void updateMachineCacheMap(Object object);

    public void updateMachineCacheMapCacheByIp(String ip, Machine machine);

    public Machine getMachineCacheMapCacheByIp(String ip);

    public Map<String, Machine> getMachineCacheMapCache();


}
