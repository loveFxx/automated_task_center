package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.domain.ProxyRequestLogs;
import com.sailvan.dispatchcenter.db.dao.automated.ProxyRequestLogsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
public class ProxyRequestLogsService {

    @Autowired
    ProxyRequestLogsDao proxyRequestLogsDao;

    public int insertRequestLogs(ProxyRequestLogs proxyRequestLogs){
        return proxyRequestLogsDao.insertRequestLogs(proxyRequestLogs);
    }

    public List<Map> sumGroupByProxyId(String minTime, String maxTime, String workType){
        return proxyRequestLogsDao.sumGroupByProxyId(minTime, maxTime,workType);
    }

    public int deleteByTime(String createdTime){
        return proxyRequestLogsDao.deleteByTime(createdTime);
    }

    public List<Map> sumGroupByProxyIdByWorkType(String minTime, String maxTime,String workType){
        return proxyRequestLogsDao.sumGroupByProxyIdByWorkType(minTime, maxTime,workType);
    }

    public List<Map> listOverBannedRate(String minTime,String maxTime,String workType,float bannedRate){
        return proxyRequestLogsDao.listOverBannedRate(minTime, maxTime, workType, bannedRate);
    }

    public List<Map> sumGroupByTimeBlock(String minTime, String maxTime, String workType, int proxyId){
        return proxyRequestLogsDao.sumGroupByTimeBlock(minTime, maxTime, workType, proxyId);
    }

    public Map<String,Integer> sumRangeTime(String minTime, String maxTime, String workType){
        return proxyRequestLogsDao.sumRangeTime(minTime, maxTime, workType);
    }

    public List<ProxyRequestLogs> listRangeTime(String minTime, String maxTime, String workType){
        return proxyRequestLogsDao.listRangeTime(minTime, maxTime, workType);
    }
}

