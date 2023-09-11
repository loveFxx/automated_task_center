package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.ProxyIpPlatform;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author mh
 * @date 21-06
 *
 *  客户端机器表
 */
@Mapper
public interface ProxyIpPlatformDao {


    /**
     *  根据指定个别参数搜索
     * @param proxyIpId
     * @return
     */
    List<ProxyIpPlatform> getProxyIpPlatformByIpId(@Param("proxyIpId") int proxyIpId);

    /**
     * 通平台查询
     * @param machinePlatform
     * @return
     */
    List<ProxyIpPlatform> getProxyIpPlatformByPlatform(ProxyIpPlatform machinePlatform);


    /**
     *  查询
     * @param proxyIpId
     * @param platform
     * @return
     */
    List<ProxyIpPlatform> getProxyIpPlatformByProxyIpIdPlatform(@Param("proxyIpId") int proxyIpId, @Param("platform") String platform);

    /**
     *  根据指定个别参数更新
     * @param proxyIpPlatform
     * @return
     */
    int updateProxyIpPlatform(ProxyIpPlatform proxyIpPlatform);

    /**
     *  删除
     * @param proxyIpPlatform
     * @return
     */
    int deleteProxyIpPlatform(ProxyIpPlatform proxyIpPlatform);

    /**
     *  更新上次使用时间
     * @param proxyIpPlatform
     * @return
     */
    int updateProxyIpPlatformLastUsedTimestamp(ProxyIpPlatform proxyIpPlatform);

    /**
     *  插入
     * @param proxyIpPlatform
     * @return
     */
    int insertProxyIpPlatform(ProxyIpPlatform proxyIpPlatform);

    List<String> listPlatformByProxyId(int proxyId);

    int countByPlatformAndStatus(String platform, Integer status);

    int deleteByProxyId(int proxyId);

    ProxyIpPlatform getByProxyIdAndPlatform(int proxyId, String platform);

    int updateStatusById(int id, int status);

    List<Map> getPlatformBanProxyIpNum();

    int countByPlatformAndOpenTimestamp(String platform, long openTimestamp);

    int deleteByProxyIdAndPlatform(int proxyId,String platform);

}
