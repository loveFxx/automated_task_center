package com.sailvan.dispatchcenter.data.init;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.cache.impl.InitBaseSystemCache;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.BusinessSystem;
import com.sailvan.dispatchcenter.common.pipe.BusinessSystemService;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.data.plugs.InitCacheMarkerConfiguration;
import com.sailvan.dispatchcenter.redis.init.InitSystemRedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 任务类型页查表获取系统
 * @author mh
 * @date 2021
 */
@Primary
@ConditionalOnBean(name = "initCacheMarker")
public class InitDataSystemRedisCache extends InitSystemRedisCache {

    @PostConstruct
    @Override
    public void init() {
        JSONArray systemNameMap = new JSONArray();
        List<BusinessSystem> list = businessSystemService.getBusinessSystemAll();
        for (BusinessSystem businessSystem : list) {
            String systemName = businessSystem.getSystemName();
            int systemId = businessSystem.getId();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name",systemName);
            jsonObject.put("value",systemId);
            if (!StringUtils.isEmpty(systemName) && !systemNameMap.contains(jsonObject)) {
                systemNameMap.add(jsonObject);
            }
        }
        redisUtils.put(SYSTEM_NAME_MAP_PREFIX,systemNameMap.toJSONString(), Constant.EFFECTIVE);
    }

}
