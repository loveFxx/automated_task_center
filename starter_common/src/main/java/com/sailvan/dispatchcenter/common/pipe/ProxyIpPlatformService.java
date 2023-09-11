package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.ProxyIpPlatform;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

import java.util.List;

/**
 * @author: mh
 * @create: 2021-06
 **/
public interface ProxyIpPlatformService {


    public List<ProxyIpPlatform> getProxyIpPlatformByIpId(int proxyIpId);

    public List<ProxyIpPlatform> getProxyIpPlatformByPlatform(ProxyIpPlatform proxyIpPlatform) ;

    public PageDataResult getProxyIpPlatformList(ProxyIpPlatform proxyIpPlatform, Integer pageNum, Integer pageSize) ;

    public int update(ProxyIpPlatform proxyIpPlatform);

    public int updateProxyIpPlatformLastUsedTimestamp(ProxyIpPlatform proxyIpPlatform);

    public int insert(ProxyIpPlatform proxyIpPlatform);

    List<String> listPlatformByProxyId(int proxyId);

    int countByPlatformAndStatus(String platform, int status);

    int countByPlatformAndOpenTimestamp(String platform, long openTimestamp);

    int deleteByProxyId(int proxyId);

    int deleteByProxyIdAndPlatform(int proxyId,String platform);
}

