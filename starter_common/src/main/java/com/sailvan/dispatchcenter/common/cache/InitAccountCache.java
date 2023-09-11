package com.sailvan.dispatchcenter.common.cache;

import com.alibaba.fastjson.JSONArray;
import com.sailvan.dispatchcenter.common.domain.StoreAccount;
import com.sailvan.dispatchcenter.common.domain.StoreAccountSites;

import java.util.*;

/**
 * 初始化账号绑定的代理IP
 *
 * @author menghui
 * @date 2021-06
 */
public interface InitAccountCache {

    public void init();

    public void updateAccountCache();

    public JSONArray getAccountMap();

    public Set<String> getAccountSiteCache();

//    public Map<String, StoreAccountSites> getAccountSiteStatusCache();

    public StoreAccountSites getAccountSiteStatusCache(String accountSite);

    public StoreAccount getAccountContinentsStatusCache(String account, String continents);

    public void updateAccountContinentsStatusCache(String account, String continents, int haveMachine);

    public void updateAccountSiteStatusCache(String accountSite, StoreAccountSites storeAccountSites);

    public void updateStoreAccountCache( StoreAccount storeAccount);

    public void updateAccountSiteStatusCacheByAccountSite(String account, String site, Integer statusMachine);


    /**
     *  返回是否无效状态  true无效
     * @param account
     * @param site
     * @return
     */
    public boolean isInvalidByAccountSite(String taskName, String account, String site, Map<Integer, String> codeMean);

    /**
     *  是否包含特殊前缀
     * @param taskName
     * @param special
     * @return
     */
    public boolean taskNameSpecialPrefix(String taskName, String[] special);


    /**
     *  判断是否是无效的账号错误码
     * @param code
     * @return
     */
    public boolean isInValidAccountErrorCode(int code);
}
