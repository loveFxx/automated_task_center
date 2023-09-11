package com.sailvan.dispatchcenter.common.domain;


import lombok.Data;

import java.io.Serializable;

/**
 * @author mh
 * @date 21-06
 *  客户端机器支持平台信息
 */
@Data
public class MachinePlatform implements Serializable {

    private String id;
    private String machineId;
    private String platform;
    private String crawlPlatform;
    private String accountPlatform;
    private String account;
    private String continents;
    private String status;

}
