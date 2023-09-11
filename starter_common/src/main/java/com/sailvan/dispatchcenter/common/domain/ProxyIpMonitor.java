package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

import java.util.List;

@Data
public class ProxyIpMonitor {

    private int proxyIpTotal = 0;

    private int ipInvalidNum=0;

    private int amazonProxyIpTotal=0;

    private int amazonIpInvalidNum=0;

    private List<PlatformProxyIpStatus> platformProxyIpStatusList;

    private int amazonDaemonTotal = 0;

    private int amazonDaemonIpInvalidNum = 0;

}
