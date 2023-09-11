package com.sailvan.dispatchcenter.common.cache;

import com.alibaba.fastjson.JSONArray;

/**
 * 任务类型页查表获取系统
 * @author mh
 * @date 2021
 */
public interface InitSystemCache {

    public void init() ;

    public void updateSystemNameCache();

    public JSONArray getSystemNameMapCache();
}
