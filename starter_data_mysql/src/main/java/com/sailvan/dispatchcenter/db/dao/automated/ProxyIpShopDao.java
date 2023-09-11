package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.ProxyIpShop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author mh
 * @date 21-06
 *
 *  代理IP对应的店铺
 */
@Mapper
public interface ProxyIpShopDao {

    /**
     *  根据ProxyIP参数搜索
     * @param proxyIpId
     * @return
     */
    List<ProxyIpShop> getProxyIpShopByProxyIpId(@Param("proxyIpId") String proxyIpId);

    /**
     *  查询
     * @param proxyIpShop
     * @return
     */
    List<ProxyIpShop> getProxyIpShopByProxyIpShop(ProxyIpShop proxyIpShop);

    /**
     *  查询
     * @param proxyIpShop
     * @return
     */
    List<ProxyIpShop> getProxyIpShopBySearch(ProxyIpShop proxyIpShop);

    /**
     *  根据指定个别参数更新
     * @param proxyIpShop
     * @return
     */
    int updateProxyIpShop(ProxyIpShop proxyIpShop);

    /**
     *  插入
     * @param proxyIpShop
     * @return
     */
    int insertProxyIpShop(ProxyIpShop proxyIpShop);

    /**
     *  修改状态
     * @param proxyIpId
     * @param accountId
     * @param status
     * @return
     */
    int updateProxyIpShopStatus(@Param("proxyIpId") Integer proxyIpId, @Param("accountId") Integer accountId, @Param("status") Integer status);

    /**
     *  查询
     * @param proxyIpId
     * @param accountId
     * @return
     */
    ProxyIpShop selectProxyIpShop(@Param("proxyIpId") Integer proxyIpId, @Param("accountId") Integer accountId);

}
