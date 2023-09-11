package com.sailvan.dispatchcenter.common.cache.impl;

import com.alibaba.fastjson.JSONArray;
import com.sailvan.dispatchcenter.common.cache.InitLambdaCache;

public class InitBaseLambdaCache implements InitLambdaCache {
    @Override
    public void init() {

    }

    @Override
    public JSONArray getRegionIdAndNameCache() {
        return null;
    }

    @Override
    public JSONArray getFunctionNameCache() {
        return null;
    }

    @Override
    public JSONArray getLambdaUserName() {
        return null;
    }

    @Override
    public JSONArray getRegionFunctionName() {
        return null;
    }
}
