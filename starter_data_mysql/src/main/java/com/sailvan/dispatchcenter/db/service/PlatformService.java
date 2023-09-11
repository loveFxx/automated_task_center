package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.domain.Platform;
import com.sailvan.dispatchcenter.db.dao.automated.PlatformDao;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author mh
 * @date 2021-09
 */
@Service
public class PlatformService implements com.sailvan.dispatchcenter.common.pipe.PlatformService {

    private static Logger logger = LoggerFactory.getLogger(PlatformService.class);

    @Autowired
    private PlatformDao platformDao;


    @Override
    public List<Platform> getPlatformAll() {
        List<Platform> list = platformDao.getPlatformAll();
        return list;
    }

    @Override
    public Platform getPlatformById(Integer id) {
        return platformDao.getPlatformById(id);
    }

    @Override
    public List<Platform> getPlatform(Platform platform) {
        List<Platform> list = platformDao.getPlatformByPlatform(platform);
        return list;
    }


    @Override
    public int update(Platform platform) {
        return platformDao.updatePlatform(platform);
    }

    @Override
    public int delete(Integer id) {
        return platformDao.deletePlatformById(id);
    }

    @Override
    public List<Platform> getPlatformByPlatform(Platform platform) {
        return platformDao.getPlatformByPlatform(platform);
    }

    @Override
    public int insert(Platform platform) {
        int i;
        i = platformDao.insertPlatform(platform);
        return i;
    }

    @Override
     public Platform getPlatformByName(@Param("platformName") String platformName){
        return platformDao.getPlatformByName(platformName);
    }

}
