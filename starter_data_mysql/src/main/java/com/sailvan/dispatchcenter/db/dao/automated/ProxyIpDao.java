package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.ProxyIp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @program: automated_task_center
 * @description: 代理IP表
 * @author: Wu Xingjian
 * @create: 2021-05-10 15:15
 **/

@Mapper
public interface ProxyIpDao {

    /**
     * 搜索所有
     * @return
     */
    List<ProxyIp> getProxyIpAll();

    /**
     *  根据指定个别参数搜索
     * @param proxyIp
     * @return
     */
    List<ProxyIp> getProxyIpByProxyIp(ProxyIp proxyIp);

    /**
     *  根据指定个别参数更新
     * @param proxyIp
     * @return
     */
    int updateProxyIp(ProxyIp proxyIp);

    /**
     *  更新端口号
     * @param id
     * @param port
     * @return
     */
    int updateProxyIpPort(@Param("id") Integer id, @Param("port") Integer port);

    /**
     *  更新可爬取平台
     * @param id
     * @param crawlPlatform
     * @return
     */
    int updateCrawlPlatform(@Param("id") Integer id, @Param("crawlPlatform") String crawlPlatform);

    int updateLargeTaskType(@Param("id") Integer id, @Param("largeTaskType") Integer largeTaskType);

    /**
     *  插入
     * @param proxyIp
     * @return
     */
    int insertProxyIp(ProxyIp proxyIp);

    /**
     *  删除
     * @param id
     * @return
     */
    int deleteProxyIpById(Integer id);

    /**
     * 查询
     * @param proxyIp
     * @return
     */
    List<ProxyIp> select(ProxyIp proxyIp);

    /**
     * 根据IP 查询
     * @param ip
     * @return
     */
    ProxyIp getProxyIpByIp(@Param("ip") String ip);

    List<Map> getPlatformProxyIpTotal(@Param("yesterday") String yesterday);

    void setProxyIpValidStatus(int validStatus, int id);

    ProxyIp getProxyIpByUniqueKey(String ip, int port);

    ProxyIp findProxyIpById(int id);

    int updateProxyStatus(int validStatus,int id);

    List<ProxyIp> getProxyByPlatform(String platform);

    int batchSetRate(String limitConfig, int unitTime, int maxBannedRate, int delayTime, Object[] ids);

    ProxyIp getFirstProxyByPlatform(String platform);

    int updateValidateTimes(int validateTimes, int isDeleted, int id);

    List<ProxyIp> listProxyByLargeTaskType(int largeTaskType);

    List<ProxyIp> listProxyByExpireTime(String expireTime);
}

