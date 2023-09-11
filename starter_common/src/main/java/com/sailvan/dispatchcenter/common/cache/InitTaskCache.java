package com.sailvan.dispatchcenter.common.cache;

import com.alibaba.fastjson.JSONArray;
import com.sailvan.dispatchcenter.common.domain.Task;

import java.util.List;

/**
 * 初始化账号绑定的代理IP
 *
 * @author menghui
 * @date 2021-06
 */
public interface InitTaskCache {


    public void init() ;

    public JSONArray getTaskNameMapCache();

    public  List<Task> getWorkTypesMapCache(String platform);

    public JSONArray getTaskIdMapCache();

    public  void updateTaskCache();

}
