package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.domain.Platform;
import com.sailvan.dispatchcenter.common.domain.ProxyIpPlatform;
import com.sailvan.dispatchcenter.db.dao.automated.ProxyIpPlatformDao;
import com.sailvan.dispatchcenter.common.cache.InitPlatformCache;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: mh
 * @create: 2021-06
 **/
@Service
public class ProxyIpPlatformService implements com.sailvan.dispatchcenter.common.pipe.ProxyIpPlatformService {
    private static final Logger logger = LoggerFactory.getLogger(ProxyIpPlatformService.class);


    @Autowired
    ProxyIpPlatformDao proxyIpPlatformDao;

    @Autowired
    InitPlatformCache initPlatformCache;


    @Override
    public List<ProxyIpPlatform> getProxyIpPlatformByIpId(int proxyIpId) {
        List<ProxyIpPlatform> list = proxyIpPlatformDao.getProxyIpPlatformByIpId(proxyIpId);
        return list;
    }

    @Override
    public List<ProxyIpPlatform> getProxyIpPlatformByPlatform(ProxyIpPlatform proxyIpPlatform) {
        List<ProxyIpPlatform> list = proxyIpPlatformDao.getProxyIpPlatformByPlatform(proxyIpPlatform);
        return list;
    }

    @Override
    public PageDataResult getProxyIpPlatformList(ProxyIpPlatform proxyIpPlatform, Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);
        List<ProxyIpPlatform> proxyIpList = proxyIpPlatformDao.getProxyIpPlatformByIpId(proxyIpPlatform.getProxyIpId());
        for (ProxyIpPlatform ipPlatform : proxyIpList) {
            Platform platform = initPlatformCache.getPlatformCacheByName(ipPlatform.getPlatform());
            ipPlatform.setPlatformName(platform.getPlatformNameZh());
        }
        PageDataResult pageDataResult = new PageDataResult();
        if(proxyIpList.size() != 0){
            PageInfo<ProxyIpPlatform> pageInfo = new PageInfo<>(proxyIpList);
            pageDataResult.setList(proxyIpList);
            pageDataResult.setTotals((int) pageInfo.getTotal());
        }
        return pageDataResult;
    }

    @Override
    public int update(ProxyIpPlatform proxyIpPlatform){
        return proxyIpPlatformDao.updateProxyIpPlatform(proxyIpPlatform);
    }

    @Override
    public int updateProxyIpPlatformLastUsedTimestamp(ProxyIpPlatform proxyIpPlatform){
        return proxyIpPlatformDao.updateProxyIpPlatformLastUsedTimestamp(proxyIpPlatform);
    }

    @Override
    public int insert(ProxyIpPlatform proxyIpPlatform){
        return proxyIpPlatformDao.insertProxyIpPlatform(proxyIpPlatform);
    }

    @Override
    public List<String> listPlatformByProxyId(int proxyId){
        return proxyIpPlatformDao.listPlatformByProxyId(proxyId);
    }

    public int deleteByProxyId(int proxyId){
        return proxyIpPlatformDao.deleteByProxyId(proxyId);
    }

    public ProxyIpPlatform getByProxyIdAndPlatform(int proxyId, String platform){
        return proxyIpPlatformDao.getByProxyIdAndPlatform(proxyId, platform);
    }

    public int updateStatusById(int id, int status){
        return proxyIpPlatformDao.updateStatusById(id,status);
    }

    @Override
    public int countByPlatformAndStatus(String platform, int status){
        return proxyIpPlatformDao.countByPlatformAndStatus(platform,status);
    }

    @Override
    public int countByPlatformAndOpenTimestamp(String platform, long openTimestamp){
        return proxyIpPlatformDao.countByPlatformAndOpenTimestamp(platform, openTimestamp);
    }

    @Override
    public int deleteByProxyIdAndPlatform(int proxyId,String platform){
        return proxyIpPlatformDao.deleteByProxyIdAndPlatform(proxyId,platform);
    }
}

