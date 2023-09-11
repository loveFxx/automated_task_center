package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.Platform;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author mh
 * @date 21-09
 *
 *  平台表
 */
@Mapper
public interface PlatformDao {

    /**
     * 搜索所有
     * @return
     */
    List<Platform> getPlatformAll();

    /**
     * 根据id获取平台
     * @param id
     * @return
     */
    Platform getPlatformById(@Param("id") Integer id);

    /**
     *  平台名
     * @param platformName
     * @return
     */
    Platform getPlatformByName(@Param("platformName") String platformName);

    /**
     * 修改平台信息
     * @return
     * @param platform
     */
    int updatePlatform(Platform platform);

    /**
     * 删除平台信息
     * @return
     * @param id
     */
    int deletePlatformById(Integer id);

    /**
     * 查询平台
     * @param platform
     * @return
     */
    List<Platform> getPlatformByPlatform(Platform platform);

    /**
     * 添加平台
     * @param platform
     * @return
     */
    int insertPlatform(Platform platform);

}
