package com.sailvan.dispatchcenter.data.init;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.Version;
import com.sailvan.dispatchcenter.common.domain.VersionFile;
import com.sailvan.dispatchcenter.common.pipe.VersionFileService;
import com.sailvan.dispatchcenter.common.pipe.VersionService;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.data.plugs.InitCacheMarkerConfiguration;
import com.sailvan.dispatchcenter.redis.init.InitValidVersionRedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 初始化有效的最新的客户端版本
 *
 * @author menghui
 * @date 2021-07
 */
@Primary
@ConditionalOnBean(name = "initCacheMarker")
public class InitDataValidVersionRedisCache extends InitValidVersionRedisCache {

    @Override
    @PostConstruct
    public void init() {
        updateValidVersionCache();
        System.out.println("ValidVersion init");
    }
}
