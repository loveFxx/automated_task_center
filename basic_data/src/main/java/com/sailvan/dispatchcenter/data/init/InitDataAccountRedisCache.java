package com.sailvan.dispatchcenter.data.init;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.StoreAccount;
import com.sailvan.dispatchcenter.common.domain.StoreAccountSites;
import com.sailvan.dispatchcenter.data.plugs.InitCacheMarkerConfiguration;
import com.sailvan.dispatchcenter.redis.init.InitAccountRedisCache;
import org.apache.lucene.util.RamUsageEstimator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;


@Primary
@ConditionalOnBean(InitCacheMarkerConfiguration.InitCacheMarker.class)
public class InitDataAccountRedisCache extends InitAccountRedisCache {

    @Override
    public void init() {
        JSONArray accountMap = new JSONArray();
        Set<String> accountSiteCache = new HashSet<>();
        List<StoreAccount> storeAccounts = storeAccountService.getStoreAccountAll();
        System.out.println("storeAccounts:"+storeAccounts.size());
        Map<String, StoreAccountSites> accountSiteStatusCacheTmp = new HashMap<>();
        for (StoreAccount storeAccount : storeAccounts) {
            String account = storeAccount.getAccount();
            String continents = storeAccount.getContinents();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name",account);
            jsonObject.put("value",account);
            if (!StringUtils.isEmpty(account) && !accountMap.contains(jsonObject)) {
                accountMap.add(jsonObject);
            }
            if( !StringUtils.isEmpty(account) && !StringUtils.isEmpty(continents)){
                redisUtils.put(ACCOUNT_CONTINENTS_STATUS_CACHE_PREFIX+account+"_"+continents,JSONObject.toJSONString(storeAccount) , Constant.EFFECTIVE);
            }

            StoreAccountSites storeAccountSites = new StoreAccountSites();
            storeAccountSites.setAccountId(storeAccount.getId());
            List<StoreAccountSites> storeAccountSitesByAccountId = storeAccountSitesService.getStoreAccountSitesByAccountId(storeAccountSites);
            if(storeAccountSitesByAccountId !=null && !storeAccountSitesByAccountId.isEmpty()){
                for (StoreAccountSites accountSites : storeAccountSitesByAccountId) {
                    accountSiteCache.add(storeAccount.getAccount()+"_"+accountSites.getSite());
                    if (!accountSiteStatusCacheTmp.containsKey(storeAccount.getAccount()+"_"+accountSites.getSite())) {
                        redisUtils.put(ACCOUNT_SITE_STATUS_CACHE_PREFIX+storeAccount.getAccount()+"_"+accountSites.getSite(),JSONObject.toJSONString(accountSites) , Constant.EFFECTIVE);
                    }
                }
            }

        }
        redisUtils.put(ACCOUNT_MAP_PREFIX,accountMap.toJSONString() ,Constant.EFFECTIVE);
        redisUtils.put(ACCOUNT_SITE_CACHE_PREFIX,String.join(",", accountSiteCache) ,Constant.EFFECTIVE);
        long l = RamUsageEstimator.sizeOf(accountMap.toJSONString().getBytes());
        System.out.println("account init size:"+(l/(1024))+"KB "+(l/(1024*1024))+"MB");
    }
}
