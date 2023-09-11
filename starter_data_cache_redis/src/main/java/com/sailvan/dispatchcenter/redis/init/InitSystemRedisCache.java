package com.sailvan.dispatchcenter.redis.init;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.BusinessSystem;
import com.sailvan.dispatchcenter.common.pipe.BusinessSystemService;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.common.cache.impl.InitBaseSystemCache;
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
public class InitSystemRedisCache extends InitBaseSystemCache {


    @Autowired
    public BusinessSystemService businessSystemService;

    @Autowired
    ApplicationContext context;

    @Autowired
    public RedisUtils redisUtils;

    public static String SYSTEM_NAME_MAP_PREFIX = "systemName:systemNameMap:";

    @PostConstruct
    @Override
    public void init() {

    }

    @Override
    public void updateSystemNameCache(){
        init();
    }

    @Override
    public JSONArray getSystemNameMapCache(){
        Object o = redisUtils.get(SYSTEM_NAME_MAP_PREFIX);
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        return JSONArray.parseArray((String.valueOf(o)));
    }
}
