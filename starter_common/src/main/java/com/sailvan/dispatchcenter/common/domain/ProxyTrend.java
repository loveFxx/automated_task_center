package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

@Data
public class ProxyTrend {

    private int id;

    private int proxyId;

    private String proxyIp;

    private String workType;

    private int isUsed;

    private String openTime;

    private String createdTime;
}
