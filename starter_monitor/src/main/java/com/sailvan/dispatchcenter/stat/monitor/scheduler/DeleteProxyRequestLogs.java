package com.sailvan.dispatchcenter.stat.monitor.scheduler;

import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.db.service.ProxyRequestLogsService;
import com.sailvan.dispatchcenter.db.service.ProxyTrendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DeleteProxyRequestLogs {

    @Autowired
    ProxyRequestLogsService proxyRequestLogsService;

    @Autowired
    ProxyTrendService proxyTrendService;

    @Scheduled(cron = "0 0 5 * * ?")
    public void deleteProxyRequestLogs(){
        //保留15天日志记录
        String createdTime = DateUtils.getHourBeforeDate(15*24);
        proxyRequestLogsService.deleteByTime(createdTime);
    }

    @Scheduled(cron = "0 0 5 * * ?")
    public void deleteProxyTrend(){
        //保留7天日志记录
        String createdTime = DateUtils.getHourBeforeDate(7*24);
        proxyTrendService.deleteByTime(createdTime);
    }
}
