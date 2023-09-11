package com.sailvan.dispatchcenter.common.cache.impl;

import com.sailvan.dispatchcenter.common.domain.Version;
import com.sailvan.dispatchcenter.common.cache.InitValidVersionCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * 初始化有效的最新的客户端版本
 *
 * @author menghui
 * @date 2021-07
 */

public class InitBaseValidVersionCache implements InitValidVersionCache {

    @Autowired
    ApplicationContext context;

    @PostConstruct
    @Override
    public void init() {
        updateValidVersionCache();
    }

    @Override
    public void updateValidVersionCache(){
        return;
    }

    @Override
    public List<Version> getVersionParentCache(){
        return null;
    }

    @Override
    public List<Version> getVersionChildCache(){
        return null;
    }

    @Override
    public List<Version> getVersionAllCache() {
        return null;
    }

    @Override
    public void putVersionAll(Map allFile, Version version) {

    }
}
