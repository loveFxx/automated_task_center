package com.sailvan.dispatchcenter.redis.init;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.Version;
import com.sailvan.dispatchcenter.common.domain.VersionFile;
import com.sailvan.dispatchcenter.common.pipe.VersionFileService;
import com.sailvan.dispatchcenter.common.pipe.VersionService;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.common.cache.impl.InitBaseValidVersionCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 初始化有效的最新的客户端版本
 *
 * @author menghui
 * @date 2021-07
 */

public class InitValidVersionRedisCache extends InitBaseValidVersionCache {


    @Autowired
    private VersionService versionService;

    @Autowired
    VersionFileService versionFileService;

    @Autowired
    ApplicationContext context;

    @Autowired
    RedisUtils redisUtils;

    private static String VERSION_PREFIX = "version:parent";
    private static String VERSION_CHILD_PREFIX = "version:child";
    private static String VERSION_ALL_PREFIX = "version:all";

    @Override
    public void updateValidVersionCache(){
        redisUtils.put(VERSION_PREFIX, "", 1L);
        redisUtils.put(VERSION_CHILD_PREFIX, "", 1L);
        List<Version> versionParentList = versionService.getValidChildVersion(0,Constant.STATUS_VALID);
        if(versionParentList == null || versionParentList.isEmpty()){
            return;
        }
        Version versionParent = versionParentList.get(0);
        List<VersionFile> versionFileList = versionFileService.getVersionFileVersion(versionParent.getId());
        versionParent.setVersionFile(versionFileList);
        redisUtils.put(VERSION_PREFIX, JSON.toJSONString(versionParent), Constant.EFFECTIVE);

        List<Version> versionChildList = versionService.getValidChildVersion(versionParent.getId(),Constant.STATUS_VALID);
        if(versionChildList == null || versionChildList.isEmpty()){
            return;
        }
        Version versionChild = versionChildList.get(0);
        List<VersionFile> versionChildFileList = versionFileService.getVersionFileVersion(versionChild.getId());
        versionChild.setVersionFile(versionChildFileList);

        redisUtils.put(VERSION_CHILD_PREFIX, JSON.toJSONString(versionChild), Constant.EFFECTIVE);
    }

    @Override
    public List<Version> getVersionParentCache(){
        Object o = redisUtils.get(VERSION_PREFIX);
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        Version version = JSONObject.parseObject(String.valueOf(o), Version.class);
        List<Version> versionList = new ArrayList<>();
        versionList.add(version);
        return versionList;
    }

    @Override
    public List<Version> getVersionChildCache(){
        Object o = redisUtils.get(VERSION_CHILD_PREFIX);
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        Version version = JSONObject.parseObject(String.valueOf(o), Version.class);
        List<Version> versionList = new ArrayList<>();
        versionList.add(version);
        return versionList;
    }

    /**
     * 全量更新子版本 扫描所有文件名及路径 存入缓存
     * @param allFile
     * @param version
     */
    @Override
    public void putVersionAll(Map allFile,Version version){
        redisUtils.put(VERSION_ALL_PREFIX, "", 1L);
        Version versionAll = new Version();
        Version versionChild = versionService.getChildVersionByFileVersionId(version.getClientFileVersion());
        List<VersionFile> versionFileList = new ArrayList<>();
        for (Object key : allFile.keySet()) {
            String strKey = key.toString();
            VersionFile versionFile = new VersionFile();
            String value = (String) allFile.get(strKey);
            versionFile.setFileName(value);
            String[] split = strKey.split("\\\\");
            String strPath = "";
            for (int i = 0; i <split.length-1 ; i++) {
                strPath = strPath+split[i]+"\\";
            }
            versionFile.setClientFilePath(strPath);
            versionFile.setVersionId(version.getId());
            versionFileList.add(versionFile);
        }
        versionAll.setVersionFile(versionFileList);
        versionAll.setResetAll(version.getResetAll());
        versionAll.setClientVersion(versionChild.getClientVersion());
        versionAll.setClientFileVersion(version.getClientFileVersion());
        redisUtils.put(VERSION_ALL_PREFIX, JSON.toJSONString(versionAll), Constant.EFFECTIVE);
    };

    /**
     * 全量更新子版本 心跳请求 取出缓存返回
     * @return
     */
    @Override
    public List<Version> getVersionAllCache(){
        Object o = redisUtils.get(VERSION_ALL_PREFIX);
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        Version version = JSONObject.parseObject(String.valueOf(o), Version.class);
        List<Version> versionList = new ArrayList<>();
        versionList.add(version);
        return versionList;
    };
}
