package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

@Data
public class PlatformProxyIpStatus {

    private String platform;

    private long  availableProxyIpNum;

    private long banProxyIpNum;

}
