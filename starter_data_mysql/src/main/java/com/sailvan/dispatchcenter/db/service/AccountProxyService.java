package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.domain.AccountProxy;
import com.sailvan.dispatchcenter.common.domain.ProxyIpShop;
import com.sailvan.dispatchcenter.db.dao.automated.AccountProxyDao;
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
public class AccountProxyService implements com.sailvan.dispatchcenter.common.pipe.AccountProxyService {

    private static Logger logger = LoggerFactory.getLogger(AccountProxyService.class);

    @Autowired
    private AccountProxyDao accountProxyDao;


    @Override
    public List<AccountProxy> getAccountProxyByProxyIpId(int proxyIpId) {
        return accountProxyDao.getAccountProxyByProxyIpId(proxyIpId);
    }

    @Override
    public List<AccountProxy> getAccountProxyByAccountProxy(AccountProxy accountProxy) {
        return accountProxyDao.getAccountProxyByAccountProxy(accountProxy);
    }

    @Override
    public int updateAccountProxy(AccountProxy accountProxy) {
        return accountProxyDao.updateAccountProxy(accountProxy);
    }

    @Override
    public int insertAccountProxy(AccountProxy accountProxy) {
        return accountProxyDao.insertAccountProxy(accountProxy);
    }

    @Override
    public int updateAccountProxyStatus(Integer proxyIpId, Integer accountId, Integer status) {
        return accountProxyDao.updateAccountProxyStatus(proxyIpId, accountId, status);
    }

    @Override
    public AccountProxy selectAccountProxy(Integer proxyIpId, Integer accountId) {
        return accountProxyDao.selectAccountProxy(proxyIpId, accountId);
    }
}
