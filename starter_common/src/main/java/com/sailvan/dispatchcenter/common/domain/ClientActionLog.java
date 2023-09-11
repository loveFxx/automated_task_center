package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ClientActionLog implements Serializable {

    private long startTime;

    private long endTime;

    private String recordTime;

    private String action;

    private String method;

    private String machineIp;

    private long timeDiff;
}
