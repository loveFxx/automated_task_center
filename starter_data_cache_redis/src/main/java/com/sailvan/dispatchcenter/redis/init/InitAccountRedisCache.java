package com.sailvan.dispatchcenter.redis.init;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.StoreAccount;
import com.sailvan.dispatchcenter.common.domain.StoreAccountSites;
import com.sailvan.dispatchcenter.common.pipe.StoreAccountService;
import com.sailvan.dispatchcenter.common.pipe.StoreAccountSitesService;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.common.cache.impl.InitBaseAccountCache;
import org.apache.lucene.util.RamUsageEstimator;
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
public class InitAccountRedisCache extends InitBaseAccountCache {


    @Autowired
    public StoreAccountService storeAccountService;

    @Autowired
    ApplicationContext context;

    @Autowired
    public StoreAccountSitesService storeAccountSitesService;

    @Autowired
    public RedisUtils redisUtils;

    public static String ACCOUNT_MAP_PREFIX = "account:accountMap:";
    public static String ACCOUNT_SITE_CACHE_PREFIX = "account:accountSiteCache:";
    public static String ACCOUNT_SITE_STATUS_CACHE_PREFIX = "account:accountSiteStatusCache:";
    public static String ACCOUNT_CONTINENTS_STATUS_CACHE_PREFIX = "account:accountContinentsStatusCache:";

    @Override
    public void init() {
    }

    @Override
    public void updateAccountCache(){
       init();
    }

    @Override
    public JSONArray getAccountMap(){
        Object o = redisUtils.get(ACCOUNT_MAP_PREFIX);
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        return JSONArray.parseArray((String.valueOf(o)));
    }

    @Override
    public Set<String> getAccountSiteCache(){
        Object o = redisUtils.get(ACCOUNT_SITE_CACHE_PREFIX);
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        return new HashSet<String>(Arrays.asList(String.valueOf(o).split(",")));
    }


    @Override
    public StoreAccountSites getAccountSiteStatusCache(String accountSite){
        Object o = redisUtils.get(ACCOUNT_SITE_STATUS_CACHE_PREFIX+accountSite);
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        return JSONObject.parseObject(String.valueOf(o), StoreAccountSites.class);
    }

    @Override
    public StoreAccount getAccountContinentsStatusCache(String account, String continents){
        if(StringUtils.isEmpty(account) || StringUtils.isEmpty(continents)){
            return null;
        }

        Object o = redisUtils.get(ACCOUNT_CONTINENTS_STATUS_CACHE_PREFIX+account+"_"+continents);
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        return JSONObject.parseObject(String.valueOf(o), StoreAccount.class);
    }


    @Override
    public void updateAccountContinentsStatusCache(String account, String continents, int haveMachine){
        if(StringUtils.isEmpty(account) || StringUtils.isEmpty(continents)){
            return;
        }
        StoreAccount storeAccount = getAccountContinentsStatusCache( account,  continents);
        if(storeAccount != null){
            storeAccount.setHaveMachine(haveMachine);
            redisUtils.put(ACCOUNT_CONTINENTS_STATUS_CACHE_PREFIX+account+"_"+continents,JSONObject.toJSONString(storeAccount) , Constant.EFFECTIVE);
        }
    }

    @Override
    public void updateAccountSiteStatusCache(String accountSite, StoreAccountSites storeAccountSites){
        redisUtils.put(ACCOUNT_SITE_STATUS_CACHE_PREFIX+accountSite,JSONObject.toJSONString(storeAccountSites) ,Constant.EFFECTIVE);
    }

    @Override
    public void updateStoreAccountCache(StoreAccount storeAccount){
        if(storeAccount == null){
            return;
        }
        if(StringUtils.isEmpty(storeAccount.getAccount()) || StringUtils.isEmpty(storeAccount.getContinents())){
            return;
        }
        redisUtils.put(ACCOUNT_CONTINENTS_STATUS_CACHE_PREFIX+storeAccount.getAccount()+"_"+storeAccount.getContinents(),JSONObject.toJSONString(storeAccount) ,Constant.EFFECTIVE);
    }

}
