package com.sailvan.dispatchcenter.common.pipe;


import com.sailvan.dispatchcenter.common.domain.ProxyIpShop;

import java.util.List;

/**
 * @author mh
 * @date 2021-06
 */
public interface ProxyIpShopService {



    public List<ProxyIpShop> getProxyIpShopByProxyIpId(String proxyIpShop);

    public List<ProxyIpShop> getProxyIpShopByProxyIpShop(ProxyIpShop proxyIpShop) ;

    public int update(ProxyIpShop proxyIpShop);

    public int insert(ProxyIpShop proxyIpShop);


}
