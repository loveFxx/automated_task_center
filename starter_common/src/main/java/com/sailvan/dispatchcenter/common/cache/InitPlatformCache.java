package com.sailvan.dispatchcenter.common.cache;

import com.alibaba.fastjson.JSONArray;
import com.sailvan.dispatchcenter.common.domain.Platform;

import java.util.List;
import java.util.Map;

/**
 * 初始化账号绑定的代理IP
 *
 * @author menghui
 * @date 2021-06
 */
public interface InitPlatformCache {

    public void init() ;

    public void updateCache();

    public Map<String, Platform> getPlatformNameCache();

//    public JSONArray getCrawlPlatformSelect();

    public JSONArray getCrawlPlatformSelectCache();

    public Platform getPlatformCache(Integer id);

    public boolean getCrawlPlatformByRemoveAccountPlatform(JSONArray crawlPlatformSelect, int index, String platformAccount);


    public Platform getPlatformCacheByName(String name);
    /**
     *  中文是为了前端显示
     * @param crawlPlatform
     * @param language
     * @return
     */
    public List<String> getCrawlPlatformNameByPlatformId(String crawlPlatform, String language);

}
