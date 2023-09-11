package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.domain.ProxyIpShop;
import com.sailvan.dispatchcenter.db.dao.automated.ProxyIpShopDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author mh
 * @date 2021-06
 */
@Service
public class ProxyIpShopService implements com.sailvan.dispatchcenter.common.pipe.ProxyIpShopService {

    private static Logger logger = LoggerFactory.getLogger(ProxyIpShopService.class);

    @Autowired
    private ProxyIpShopDao proxyIpShopDao;


    @Override
    public List<ProxyIpShop> getProxyIpShopByProxyIpId(String proxyIpShop) {
        List<ProxyIpShop> list = proxyIpShopDao.getProxyIpShopByProxyIpId(proxyIpShop);
        return list;
    }

    @Override
    public List<ProxyIpShop> getProxyIpShopByProxyIpShop(ProxyIpShop proxyIpShop) {
        List<ProxyIpShop> list = proxyIpShopDao.getProxyIpShopByProxyIpShop(proxyIpShop);
        return list;
    }

    @Override
    public int update(ProxyIpShop proxyIpShop){
        return proxyIpShopDao.updateProxyIpShop(proxyIpShop);
    }

    @Override
    public int insert(ProxyIpShop proxyIpShop){
        return proxyIpShopDao.insertProxyIpShop(proxyIpShop);
    }


}
