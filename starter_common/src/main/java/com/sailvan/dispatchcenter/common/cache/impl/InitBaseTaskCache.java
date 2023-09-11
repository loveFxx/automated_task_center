package com.sailvan.dispatchcenter.common.cache.impl;

import com.alibaba.fastjson.JSONArray;
import com.sailvan.dispatchcenter.common.domain.Task;
import com.sailvan.dispatchcenter.common.cache.InitTaskCache;

import java.util.List;

/**
 * 初始化账号绑定的代理IP
 *
 * @author menghui
 * @date 2021-06
 */

public class InitBaseTaskCache implements InitTaskCache {

    @Override
    public void init() {
        return;
    }

    @Override
    public void updateTaskCache(){
        return;
    }

    @Override
    public JSONArray getTaskNameMapCache(){
        return null;
    }

    @Override
    public List<Task> getWorkTypesMapCache(String platform){
        return null;
    }

    @Override
    public JSONArray getTaskIdMapCache(){
        return null;
    }

}
