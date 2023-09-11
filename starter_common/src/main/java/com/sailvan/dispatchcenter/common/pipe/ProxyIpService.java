package com.sailvan.dispatchcenter.common.pipe;

import com.alibaba.fastjson.JSONArray;
import com.sailvan.dispatchcenter.common.domain.ProxyIp;
import com.sailvan.dispatchcenter.common.domain.ProxyIpMonitor;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

import java.util.List;


/**
 * @program: automated_task_center
 * @description:
 * @author: Wu Xingjian
 * @create: 2021-05-10 15:45
 **/
public interface ProxyIpService {


    public void refreshCrawlPlatform();

    public void refreshProxyIPPlatform();
    public void refreshProxyIPLargeTaskType();

    public List<ProxyIp> getProxyIpAll() ;

    public PageDataResult getProxyIpList(ProxyIp proxyIp, Integer pageNum, Integer pageSize);



    public int update(ProxyIp proxyIp) ;

    public int updateProxyIpPort(Integer id, Integer port) ;

    public int updateCrawlPlatform(Integer id, String crawlPlatform) ;

    public int updateLargeTaskType(Integer id, Integer largeTaskType);

    public int insert(ProxyIp proxyIp) ;

    public List<ProxyIp> select(ProxyIp proxyIp);

    public ProxyIp getProxyIpByIp(String proxyIp);


    public int delete(Integer id);

    public Object getProxyIp(String account, String continents, String platform);

    PageDataResult getPlatformProxyIpStatus();

    ProxyIpMonitor getProxyIpNum();

    void setProxyIpValidStatus(int validStatus, int id);

    ProxyIp getProxyIpByUniqueKey(String ip, int port);

    public ProxyIp findProxyIpById(int id);

    int updateProxyStatus(int validStatus,int id);

    List<ProxyIp> getProxyByPlatform(String platform);

    int batchSetRate(String limitConfig, int unitTime, int maxBannedRate, int delayTime, Object[] ids);

    ProxyIp getFirstProxyByPlatform(String platform);

    int updateValidateTimes(int validateTimes, int isDeleted, int id);

    List<ProxyIp> listProxyByLargeTaskType(int largeTaskType);

    List<ProxyIp> listProxyByExpireTime(String expireTime);
}

