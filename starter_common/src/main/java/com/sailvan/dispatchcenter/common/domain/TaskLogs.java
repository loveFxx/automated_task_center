package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TaskLogs implements Serializable {

    private int id;

    private String taskSourceId;

    private int resultHashKey;

    private int hashKey;

    private String taskName;

    private int retryTimes;

    private int event = -1;

    private String explain = "";

    private String account = "";

    private String continent = "";

    private String platform = "";

    private String remoteIp = "";

    private String proxyIp = "";

    private String clientParams = "";

    private String centerParams = "";

    private String refreshTime;

    private String date;
    private String createdTime;

    private int hasFile = 0;

    private int runMode = -1;   //-1默认初始，0：机器；1：Lambda

    private int clientCode;
}
