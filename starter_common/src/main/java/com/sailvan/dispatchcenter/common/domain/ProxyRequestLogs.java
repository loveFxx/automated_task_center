package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

@Data
public class ProxyRequestLogs {

    private int id;

    private int proxyId;

    private String proxyIp;

    private int port;

    private String workType;

    private String machineIp;

    private int usedNum;

    private int succeedNum;

    private int bannedNum;

    private String createdAt;
}
