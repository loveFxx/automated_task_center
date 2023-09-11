package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.AccountProxy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author mh
 * @date 21-11
 *
 *  店铺下的代理IP
 */
@Mapper
public interface AccountProxyDao {

    /**
     *  根据ProxyIP参数搜索
     * @param proxyIpId
     * @return
     */
    List<AccountProxy> getAccountProxyByProxyIpId(@Param("proxyIpId") int proxyIpId);

    /**
     *  查询
     * @param accountProxy
     * @return
     */
    List<AccountProxy> getAccountProxyByAccountProxy(AccountProxy accountProxy);


    /**
     *  根据指定个别参数更新
     * @param accountProxy
     * @return
     */
    int updateAccountProxy(AccountProxy accountProxy);

    /**
     *  插入
     * @param accountProxy
     * @return
     */
    int insertAccountProxy(AccountProxy accountProxy);

    /**
     *  修改状态
     * @param proxyIpId
     * @param accountId
     * @param status
     * @return
     */
    int updateAccountProxyStatus(@Param("proxyIpId") Integer proxyIpId, @Param("accountId") Integer accountId, @Param("status") Integer status);

    /**
     *  更新状态
     * @param id
     * @param status
     * @return
     */
    int updateAccountProxyStatusById(@Param("id") Integer id, @Param("status") Integer status);

    int updateAccountProxyPortById(@Param("id") Integer id, @Param("port") Integer port);

    /**
     *  查询
     * @param proxyIpId
     * @param accountId
     * @return
     */
    AccountProxy selectAccountProxy(@Param("proxyIpId") Integer proxyIpId, @Param("accountId") Integer accountId);

    List<AccountProxy> getAccountProxyByAccountProxyTwo(AccountProxy accountProxy);
}
