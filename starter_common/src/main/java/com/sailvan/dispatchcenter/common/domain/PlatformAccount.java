package com.sailvan.dispatchcenter.common.domain;


import lombok.Data;

import java.io.Serializable;

/**
 * @author
 * @date 21-05
 *  mini platform_account对应实体类
 */
@Data
public class PlatformAccount implements Serializable {

    private int id;
    private String platform;
    private String ipMac;
    private String shopName;
    private int shopId;
    private String platformAccount;
    private String platformPsWord;
    private String managerAccount;
    private int isProxy;
    private String proxyHost;
    private String proxyPort;
    private int browserType;

    private String site;
    private String createdAt;
    private String updatedAt;
}
