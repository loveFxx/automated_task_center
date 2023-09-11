package com.sailvan.dispatchcenter.data.async;

import com.sailvan.dispatchcenter.common.cache.InitMachineCache;
import com.sailvan.dispatchcenter.common.cache.InitTaskCache;
import com.sailvan.dispatchcenter.common.domain.Task;
import com.sailvan.dispatchcenter.common.pipe.ProxyIpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 *  异步推送任务到业务端
 * @author mh
 * @date
 */
@Component
public class AsyncUpdateCache {

    private static Logger logger = LoggerFactory.getLogger(AsyncUpdateCache.class);

    @Autowired
    InitTaskCache initTaskCache;

    @Autowired
    InitMachineCache initMachineCache;

    @Autowired
    ProxyIpService proxyIpService;

    /**
     * 根据beanName指定特定线程池 异步推送
     */
    @Async("async_bean_executor_task")
    public void updateTaskCache(){
        try {
            initTaskCache.updateTaskCache();
            logger.info("updateTaskCache--success");
        } catch (Exception e) {
            logger.error("updateTaskCache--更新失败");
        }
    }

    @Async("async_bean_executor_task")
    public void updateMachineCacheMap(Task task){
        try {
            initMachineCache.updateMachineCacheMap(task);
            logger.info("updateMachineCacheMap--success");
        } catch (Exception e) {
            logger.error("updateMachineCacheMap--更新失败");
        }
    }

    @Async("async_bean_executor_task")
    public void updateProxyIpMapByPlatformUpdate(){
        try {
            proxyIpService.refreshCrawlPlatform();
            proxyIpService.refreshProxyIPPlatform();
            logger.info("refreshCrawlPlatform refreshProxyIPPlatform--success");
        } catch (Exception e) {
            logger.error("refreshCrawlPlatform refreshProxyIPPlatform--更新失败");
        }
    }

}

