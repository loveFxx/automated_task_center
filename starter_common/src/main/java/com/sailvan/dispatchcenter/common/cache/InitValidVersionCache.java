package com.sailvan.dispatchcenter.common.cache;

import com.sailvan.dispatchcenter.common.domain.Version;

import java.util.List;
import java.util.Map;

/**
 * 初始化有效的最新的客户端版本
 *
 * @author menghui
 * @date 2021-07
 */
public interface InitValidVersionCache {

    public void init() ;

    public void updateValidVersionCache();

    public List<Version> getVersionParentCache();
    public List<Version> getVersionChildCache();

    List<Version> getVersionAllCache();

    void putVersionAll(Map allFile,Version version);
}
