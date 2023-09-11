package com.sailvan.dispatchcenter.redis.init;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.Platform;
import com.sailvan.dispatchcenter.common.pipe.PlatformService;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.common.cache.impl.InitBasePlatformCache;
import org.apache.lucene.util.RamUsageEstimator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 初始化账号绑定的代理IP
 *
 * @author menghui
 * @date 2021-06
 */
public class InitPlatformRedisCache extends InitBasePlatformCache {


    @Autowired
    public PlatformService platformService;

    @Autowired
    ApplicationContext context;

    @Autowired
    public RedisUtils redisUtils;

    public static String PLATFORM_PREFIX = "platform:platformCache:";
    public static String PLATFORM_NAME_PREFIX = "platform:platformNameCache:";
    public static String CRAWL_PLATFORM_SELECT_PREFIX = "platform:crawlPlatformSelect:";

    public static Map<Integer, Platform> platformCache = new HashMap<>();
    public static Map<String, Platform> platformNameCache = new HashMap<>();

    /**
     *   前端显示 可爬取平台的select选择框 值是id
     */
    public static JSONArray crawlPlatformSelect = new JSONArray();

    @PostConstruct
    @Override
    public void init() {
        List<Platform> platformAll = platformService.getPlatformAll();
        platformCache = new HashMap<>();
        platformNameCache = new HashMap<>();
        List<String> list = new ArrayList<>();
        for (Platform platform : platformAll) {
            Constant.EXECUTE_PLATFORMS.put(String.valueOf(platform.getId()), platform.getPlatformName());
            if(!list.contains(platform.getPlatformName())){
                list.add(platform.getPlatformName());
            }
        }
        Constant.PLATFORMS = list.toArray(new String[list.size()]);
        System.out.println("platform init ... size");
    }

    @Override
    public Map<String, Platform> getPlatformNameCache(){
        return platformNameCache;
    }


    @Override
    public void updateCache(){
        init();
    }


    @Override
    public JSONArray getCrawlPlatformSelectCache(){
        Object o = redisUtils.get(CRAWL_PLATFORM_SELECT_PREFIX);
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        return JSONArray.parseArray((String.valueOf(o)));
    }

    @Override
    public Platform getPlatformCache(Integer id){
        if(StringUtils.isEmpty(id)){
            return null;
        }
        Object o = redisUtils.get(PLATFORM_PREFIX+id);
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        return JSONObject.parseObject(String.valueOf(o), Platform.class);
    }

    @Override
    public Platform getPlatformCacheByName(String name) {
        if(StringUtils.isEmpty(name)){
            return null;
        }
        Object o = redisUtils.get(PLATFORM_NAME_PREFIX+name);
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        return JSONObject.parseObject(String.valueOf(o), Platform.class);
    }

}
