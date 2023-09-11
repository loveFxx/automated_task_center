package com.sailvan.dispatchcenter.common.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * @author mh
 * @date 21-06
 *  代理IP支持平台信息
 */
@Data
public class ProxyIpPlatform implements Serializable {

    private int id;
    private int proxyIpId;
    private String platform;
    private String platformName;

    /**
     *  1有效
     */
    private int status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private String banPeriod;

    private BigInteger lastUsedTimestamp;

    private BigInteger openTimestamp;
}
