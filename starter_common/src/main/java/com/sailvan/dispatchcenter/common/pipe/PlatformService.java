package com.sailvan.dispatchcenter.common.pipe;


import com.sailvan.dispatchcenter.common.domain.Platform;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author mh
 * @date 2021-09
 */
public interface PlatformService {



    public List<Platform> getPlatformAll() ;

    public Platform getPlatformById(Integer id);

    public List<Platform> getPlatform(Platform platform) ;

    public int update(Platform platform);

    public int delete(Integer id);

    public List<Platform> getPlatformByPlatform(Platform platform) ;

    public int insert(Platform platform);

    Platform getPlatformByName(@Param("platformName") String platformName);
}
