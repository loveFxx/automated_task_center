package com.sailvan.dispatchcenter.common.domain;


import lombok.Data;

import java.io.Serializable;

/**
 * @author mh
 * @date 21-06
 *  代理IP下的店铺
 */
@Data
public class AccountProxy implements Serializable {

    private int id;
    private int proxyIpId;
    private String proxyIp;
    private int port;
    private int accountId;
    private String platform;
    private String account;
    private String continents;

    private int status=-1;

    private String createdTime;
    private String updatedTime;
    private String deletedTime;
}
