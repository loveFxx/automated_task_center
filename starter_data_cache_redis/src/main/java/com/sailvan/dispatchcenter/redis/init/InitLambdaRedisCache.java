package com.sailvan.dispatchcenter.redis.init;

import com.alibaba.fastjson.JSONArray;
import com.sailvan.dispatchcenter.common.cache.impl.InitBaseLambdaCache;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

public class InitLambdaRedisCache extends InitBaseLambdaCache {

    @Autowired
    RedisUtils redisUtils;

    public static String REGION_NAME_MAP_PREFIX = "regionCache:regionIdMap:";
    public static String LAMBDA_FUNCTION_MAP_PREFIX = "lambdaFunctionCache:FunctionNameMap:";
    public static String LAMBDA_USER_NAME_PREFIX = "lambdaUserCache:LambdaUserName:";
    public static String REGION_FUNCTION_NAME_PREFIX = "regionFunctionNameCache:regionFunctionName:";
    //public static String USER_REGION_FUNCTION_PREFIX = "userRegionFunctionNameCache:userRegionFunctionName:";

    @PostConstruct
    @Override
    public void init() {

    }

    @Override
    public JSONArray getRegionIdAndNameCache() {
        Object o = redisUtils.get(REGION_NAME_MAP_PREFIX);
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        return JSONArray.parseArray(String.valueOf(o));
    }

    @Override
    public JSONArray getFunctionNameCache() {
        Object o = redisUtils.get(LAMBDA_FUNCTION_MAP_PREFIX);
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        return JSONArray.parseArray(String.valueOf(o));
    }

    @Override
    public JSONArray getLambdaUserName() {
        Object o = redisUtils.get(LAMBDA_USER_NAME_PREFIX);
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        return JSONArray.parseArray(String.valueOf(o));
    }

    @Override
    public JSONArray getRegionFunctionName() {
        Object o = redisUtils.get(REGION_FUNCTION_NAME_PREFIX);
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        return JSONArray.parseArray(String.valueOf(o));
    }

}
