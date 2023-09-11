package com.sailvan.dispatchcenter.common.cache;

import com.alibaba.fastjson.JSONArray;

/**
 * 初始化lambda模块的区域数据
 *
 * @author yyj
 * @date 2022-02
 */
public interface InitLambdaCache {

    public void init() ;

    JSONArray getRegionIdAndNameCache();

    JSONArray getFunctionNameCache();

    JSONArray getLambdaUserName();

    JSONArray getRegionFunctionName();

}
