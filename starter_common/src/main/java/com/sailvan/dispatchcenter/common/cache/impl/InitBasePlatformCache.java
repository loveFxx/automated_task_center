package com.sailvan.dispatchcenter.common.cache.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.domain.Platform;
import com.sailvan.dispatchcenter.common.cache.InitPlatformCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 初始化账号绑定的代理IP
 *
 * @author menghui
 * @date 2021-06
 */
public class InitBasePlatformCache implements InitPlatformCache {



    @Autowired
    ApplicationContext context;


    @Override
    public void init() {
    }

    @Override
    public Map<String, Platform> getPlatformNameCache(){
        return null;
    }


    @Override
    public void updateCache(){}


    @Override
    public JSONArray getCrawlPlatformSelectCache(){
        return null;
    }

    @Override
    public Platform getPlatformCache(Integer id){
       return null;
    }

    @Override
    public Platform getPlatformCacheByName(String name){
        return null;
    }

    @Override
    public boolean getCrawlPlatformByRemoveAccountPlatform(JSONArray crawlPlatformSelect, int index, String platformAccount){
        String id = String.valueOf(((JSONObject) crawlPlatformSelect.get(index)).get("value"));
        Platform platform = getPlatformCache(Integer.parseInt(id));
        if (platformAccount.equals(platform.getPlatformName())) {
            crawlPlatformSelect.remove(index);
            return true;
        }
        return false;
    }


    /**
     *  中文是为了前端显示
     * @param crawlPlatform
     * @param language
     * @return
     */
    @Override
    public List<String> getCrawlPlatformNameByPlatformId(String crawlPlatform, String language){
        if(StringUtils.isEmpty(crawlPlatform)){
            return null;
        }
        List<String> platformNameListZh = new ArrayList<>();
        List<String> platformNameListEn = new ArrayList<>();
        String comma = ",";
        if (crawlPlatform.contains(comma)) {
            String[] split = crawlPlatform.split(comma);
            for (String s : split) {
                putPlatformNameList(s, platformNameListZh, platformNameListEn);
            }
        } else {
            putPlatformNameList(crawlPlatform, platformNameListZh, platformNameListEn);
        }
        if(!StringUtils.isEmpty(language) && language.equals("zh")){
            return platformNameListZh;
        }
        return platformNameListEn;
    }

    private void putPlatformNameList(String id, List<String> platformNameListZh, List<String> platformNameListEn){
        String platformName = getPlatformCache(Integer.parseInt(id)).getPlatformNameZh();
        if(!platformNameListZh.contains(platformName)){
            platformNameListZh.add(platformName);
        }
        String platformNameEn = getPlatformCache(Integer.parseInt(id)).getPlatformName();
        if(!platformNameListEn.contains(platformNameEn)){
            platformNameListEn.add(platformNameEn);
        }
    }


}
