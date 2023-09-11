package com.sailvan.dispatchcenter.common.cache.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.domain.BusinessSystem;
import com.sailvan.dispatchcenter.common.pipe.BusinessSystemService;
import com.sailvan.dispatchcenter.common.cache.InitSystemCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 任务类型页查表获取系统
 * @author mh
 * @date 2021
 */
public class InitBaseSystemCache implements InitSystemCache {


    @Autowired
    public BusinessSystemService businessSystemService;

    @Autowired
    ApplicationContext context;

    public static JSONArray systemNameMap = new JSONArray();

    @PostConstruct
    @Override
    public void init() {
    }

    @Override
    public void updateSystemNameCache(){
        systemNameMap.clear();
        getSystemNameMapCache();
    }

    @Override
    public JSONArray getSystemNameMapCache(){
        if(systemNameMap == null || systemNameMap.isEmpty()){
            synchronized (InitBaseSystemCache.class){
                if(systemNameMap == null || systemNameMap.isEmpty()){
                    JSONArray SystemNameMapTmp = new JSONArray();
                    List<BusinessSystem> list = businessSystemService.getBusinessSystemAll();
                    for (BusinessSystem businessSystem : list) {
                        String systemName = businessSystem.getSystemName();
                        int systemId = businessSystem.getId();
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("name",systemName);
                        jsonObject.put("value",systemId);
                        if (!StringUtils.isEmpty(systemName) && !SystemNameMapTmp.contains(jsonObject)) {
                            SystemNameMapTmp.add(jsonObject);
                        }
                    }
                    systemNameMap.addAll(SystemNameMapTmp);
                    return systemNameMap;
                }
            }
        }
        return systemNameMap;
    }
}
