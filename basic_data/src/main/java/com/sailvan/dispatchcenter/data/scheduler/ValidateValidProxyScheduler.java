package com.sailvan.dispatchcenter.data.scheduler;

import com.sailvan.dispatchcenter.common.cache.ProxyIPPool;
import com.sailvan.dispatchcenter.common.domain.ProxyIp;
import com.sailvan.dispatchcenter.common.pipe.ProxyIpService;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时刷新机器及账号信息
 * @author mh
 * @date 2021
 */
@Component
public class ValidateValidProxyScheduler {

    @Autowired
    ProxyIPPool proxyIPPool;

    @Autowired
    ProxyIpService proxyIpService;

    /**
     * 验证代理IP是否过期，过期的移除代理IP池，并软删除
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void validate(){
        List<ProxyIp> proxyIps = proxyIpService.listProxyByExpireTime(DateUtils.getCurrentDate());
        for (ProxyIp proxyIp : proxyIps) {
            proxyIPPool.removeProxy(proxyIp.getId());
            proxyIpService.updateValidateTimes(0,1,proxyIp.getId());
        }
    }

}
