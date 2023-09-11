package com.sailvan.dispatchcenter.data.init;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.Platform;
import com.sailvan.dispatchcenter.data.plugs.InitCacheMarkerConfiguration;
import com.sailvan.dispatchcenter.redis.init.InitPlatformRedisCache;
import org.apache.lucene.util.RamUsageEstimator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Primary
@ConditionalOnBean(name = "initCacheMarker")
public class InitDataPlatformRedisCache extends InitPlatformRedisCache {
    @Override
    public void init() {
        List<Platform> platformAll = platformService.getPlatformAll();
        JSONArray crawlPlatformSelect = new JSONArray();
        platformCache = new HashMap<>();
        platformNameCache = new HashMap<>();
        List<String> list = new ArrayList<>();
        for (Platform platform : platformAll) {
            Constant.EXECUTE_PLATFORMS.put(String.valueOf(platform.getId()), platform.getPlatformName());

            redisUtils.put(PLATFORM_PREFIX+platform.getId(), JSONObject.toJSONString(platform),Constant.EFFECTIVE);
            redisUtils.put(PLATFORM_NAME_PREFIX+platform.getPlatformName(),JSONObject.toJSONString(platform),Constant.EFFECTIVE);
            if(!list.contains(platform.getPlatformName())){
                list.add(platform.getPlatformName());
            }
            JSONObject jsonObjectCrawl = new JSONObject();
            jsonObjectCrawl.put("name",platform.getPlatformNameZh());
            jsonObjectCrawl.put("value",platform.getId());
//            if (!crawlPlatformSelect.contains(jsonObjectCrawl)) {
            crawlPlatformSelect.add(jsonObjectCrawl);
//            }
        }
        redisUtils.put(CRAWL_PLATFORM_SELECT_PREFIX,crawlPlatformSelect.toJSONString(),Constant.EFFECTIVE);

        Constant.PLATFORMS = list.toArray(new String[list.size()]);
        long l = RamUsageEstimator.sizeOf(platformAll.toString().getBytes());
        System.out.println("platform init ... size:"+(l/(1024))+"KB "+(l/(1024*1024))+"MB");
    }
}
