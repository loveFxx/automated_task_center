package com.sailvan.dispatchcenter.common.pipe;


import com.sailvan.dispatchcenter.common.domain.AccountProxy;
import com.sailvan.dispatchcenter.common.domain.ProxyIpShop;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author mh
 * @date 2021-06
 */
public interface AccountProxyService {



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
     *  查询
     * @param proxyIpId
     * @param accountId
     * @return
     */
    AccountProxy selectAccountProxy(@Param("proxyIpId") Integer proxyIpId, @Param("accountId") Integer accountId);


}
