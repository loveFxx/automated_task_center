package com.sailvan.dispatchcenter.common.cache.impl;

import com.alibaba.fastjson.JSONArray;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.StoreAccount;
import com.sailvan.dispatchcenter.common.domain.StoreAccountSites;
import com.sailvan.dispatchcenter.common.pipe.StoreAccountService;
import com.sailvan.dispatchcenter.common.pipe.StoreAccountSitesService;
import com.sailvan.dispatchcenter.common.cache.InitAccountCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 初始化账号绑定的代理IP
 *
 * @author menghui
 * @date 2021-06
 */
//@Component
public class InitBaseAccountCache implements InitAccountCache {

    private static Logger logger = LoggerFactory.getLogger(InitBaseAccountCache.class);

    @Autowired
    ApplicationContext context;

    @Autowired
    private StoreAccountSitesService storeAccountSitesService;

    @Autowired
    StoreAccountService storeAccountService;

    @PostConstruct
    @Override
    public void init() {
    }

    @Override
    public void updateAccountCache() {
        return;
    }


    @Override
    public JSONArray getAccountMap() {
        return null;
    }

    @Override
    public Set<String> getAccountSiteCache() {
        return null;
    }

    @Override
    public void updateAccountSiteStatusCache(String accountSite, StoreAccountSites storeAccountSites) {
        return;
    }

    @Override
    public void updateStoreAccountCache(StoreAccount storeAccount) {
        return;
    }


    @Override
    public void updateAccountSiteStatusCacheByAccountSite(String account, String site, Integer statusMachine) {
        if (StringUtils.isEmpty(account) || StringUtils.isEmpty(site)) {
            return;
        }
        String key = account + "_" + site;
        StoreAccountSites accountSiteStatusCache = getAccountSiteStatusCache(key);
        if (accountSiteStatusCache != null) {
            accountSiteStatusCache.setStatusMachine(statusMachine);
            storeAccountSitesService.updateMachineStatus(accountSiteStatusCache);
            updateAccountSiteStatusCache(key, accountSiteStatusCache);
        } else {
            StoreAccountSites storeAccountSites = storeAccountService.getStoreAccountSitesByAccount(account, site);
            if (storeAccountSites != null) {
                storeAccountSites.setStatusMachine(statusMachine);
                storeAccountSitesService.updateMachineStatus(storeAccountSites);
                updateAccountSiteStatusCache(key, storeAccountSites);
            }
        }
    }


    @Override
    public StoreAccountSites getAccountSiteStatusCache(String accountSite) {
        return null;
    }

    @Override
    public StoreAccount getAccountContinentsStatusCache(String account, String continents) {
        return null;
    }

    @Override
    public void updateAccountContinentsStatusCache(String account, String continents, int haveMachine) {
        return;
    }


    /**
     * 返回是否无效状态  true无效
     *
     * @param account
     * @param site
     * @return
     */
    @Override
    public boolean isInvalidByAccountSite(String taskName, String account, String site, Map<Integer, String> codeMean) {

        if (StringUtils.isEmpty(account) || StringUtils.isEmpty(site)) {
            codeMean.put(ResponseCode.ERROR_CODE, "账号或站点为空");
            return true;
        }
        if (taskNameSpecialPrefix(taskName, Constant.TASK_NAME_SPECIAL_PREFIX)) {
            //特殊 任务无论mini是否存在都可以添加
            return false;
        }
        String key = account + "_" + site;
        Set<String> accountSiteCache = getAccountSiteCache();
        // 判断 当前的账号站点是否 存在
        if (accountSiteCache != null && !accountSiteCache.isEmpty() && !accountSiteCache.contains(key)) {
            logger.info("isInvalidByAccountSite key:{}  code:{}", key, "mini账号站点不存在");
            codeMean.put(ResponseCode.ERROR_CODE, "mini账号站点不存在");
            return true;
        }
        if (StringUtils.isEmpty(taskName)) {
            //如果任务名是空,则不过滤店铺状态
            return false;
        }

        StoreAccount accountContinentsStatusCache = getAccountContinentsStatusCache(account, Constant.SITE_CONTINENT_MAP.get(site));
        if (accountContinentsStatusCache != null) {
            int haveMachine = accountContinentsStatusCache.getHaveMachine();
            if (haveMachine != 1) {
                logger.info("isInvalidByAccountSite key:{}  code:{}", key, ResponseCode.RESPONSE_CODE_MAP.get(ResponseCode.NO_MACHINE_ERROR));
                codeMean.put(ResponseCode.NO_MACHINE_ERROR, ResponseCode.RESPONSE_CODE_MAP.get(ResponseCode.NO_MACHINE_ERROR) + "值是:" + haveMachine);
                return true;
            }
        }
        StoreAccountSites storeAccountSites = getAccountSiteStatusCache(key);
        if (storeAccountSites != null) {
            int statusMachine = storeAccountSites.getStatusMachine();
            //首先 判断机器验证状态
//            if (isInValidAccountErrorCode(statusMachine)) {
//                codeMean.put(statusMachine, ResponseCode.RESPONSE_CODE_MAP.get(statusMachine));
//            }
            //然后 判断人工验证状态(token系统)
//            int statusPerson = storeAccountSites.getStatusPerson();
//            if(statusPerson == Constant.ACCOUNT_STATUS_CLOSE_SHOP_CANNOT_LOGIN || statusPerson == Constant.ACCOUNT_STATUS_INVALID_SHOP){
//                codeMean.put(statusPerson, ResponseCode.ACCOUNT_CODE_MAP.get(statusPerson));
//            }

            if (storeAccountSites.getStatus() == Constant.STATUS_VALID) {
                return false;
            } else if (storeAccountSites.getStatus() == Constant.STATUS_INVALID) {
                if(statusMachine == -2){
                    codeMean.put(ResponseCode.ERROR_CODE,"店铺状态无效,机器状态是初始化值-2,人工状态是:"+ ResponseCode.ACCOUNT_CODE_MAP.get(storeAccountSites.getStatusPerson()));
                }else {
                    codeMean.put(ResponseCode.ERROR_CODE, "店铺状态无效,机器状态是:"+statusMachine+",人工状态是:"+ ResponseCode.ACCOUNT_CODE_MAP.get(storeAccountSites.getStatusPerson()));
                }
                logger.info("isInvalidByAccountSite key:{}  code:{}", key, ResponseCode.RESPONSE_CODE_MAP.get(statusMachine));
                return true;
            }

        }
        return false;
    }

    /**
     * 是否包含特殊前缀
     *
     * @param taskName
     * @param special
     * @return
     */
    @Override
    public boolean taskNameSpecialPrefix(String taskName, String[] special) {
        if (StringUtils.isEmpty(taskName)) {
            return false;
        }
        for (String taskNameSpecialPrefix : special) {
            if (taskName.contains(taskNameSpecialPrefix)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 判断是否是无效的账号错误码
     *
     * @param code
     * @return
     */
    @Override
    public boolean isInValidAccountErrorCode(int code) {
        boolean flag = false;
        switch (code) {
            case ResponseCode.ACCOUNT_LOCK:
            case ResponseCode.PASSWORD_ERROR:
            case ResponseCode.NEED_TO_RESET_PASSWORD:
            case ResponseCode.NO_OPEN_VERIFY:
            case ResponseCode.NEED_TO_UPLOAD_QCCODE:
            case ResponseCode.QC_CODE_ERROR:
            case ResponseCode.NO_CREDIT_CARD:
                flag = true;
                break;
            default:
                flag = false;
        }
        return flag;
    }
}
