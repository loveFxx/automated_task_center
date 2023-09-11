package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.domain.ProxyIp;
import com.sailvan.dispatchcenter.common.domain.ProxyTrend;
import com.sailvan.dispatchcenter.common.pipe.ProxyIpService;
import com.sailvan.dispatchcenter.db.dao.automated.ProxyTrendDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProxyTrendService implements com.sailvan.dispatchcenter.common.pipe.ProxyTrendService {

    @Autowired
    ProxyTrendDao proxyTrendDao;

    @Autowired
    ProxyIpService proxyIpService;

    @Override
    public int insertProxyTrend(ProxyTrend proxyTrend){
        return proxyTrendDao.insertProxyTrend(proxyTrend);
    }

    @Override
    public void insertProxyTrend(int proxyId, String workType, int isUsed, String openTime){
        ProxyIp proxyIp = proxyIpService.findProxyIpById(proxyId);
        if (proxyIp != null){
            ProxyTrend proxyTrend = new ProxyTrend();
            proxyTrend.setProxyId(proxyId);
            proxyTrend.setProxyIp(proxyIp.getIp() + ":" + proxyIp.getPort());
            proxyTrend.setWorkType(workType);
            proxyTrend.setIsUsed(isUsed);
            proxyTrend.setOpenTime(openTime);
            insertProxyTrend(proxyTrend);
        }
    }

    @Override
    public List<Map<String,String>> listProxySituation(String beginTime, String endTime, String workType){
        return proxyTrendDao.listProxySituation(beginTime, endTime, workType);
    }

    @Override
    public int countInvalidProxy(String beginTime, String endTime, String workType){
        return proxyTrendDao.countInvalidProxy(beginTime, endTime, workType);
    }

    @Override
    public int countValidProxy(String beginTime, String endTime, String workType){
        return proxyTrendDao.countValidProxy(beginTime, endTime, workType);
    }

    @Override
    public int deleteByTime(String createdTime){
        return proxyTrendDao.deleteByTime(createdTime);
    }
}

