package com.sailvan.dispatchcenter.common.domain;


import lombok.Data;

import java.io.Serializable;

/**
 * @author mh
 * @date 21-06
 *  代理IP下的店铺
 */
@Data
public class ProxyIpShop implements Serializable {

    private int id;
    private int proxyIpId;
    private int accountId;
    private String platform;
    private String account;
    private String continents;

    private int status=-1;
}
