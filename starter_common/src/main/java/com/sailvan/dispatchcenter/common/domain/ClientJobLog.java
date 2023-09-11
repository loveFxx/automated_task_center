package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ClientJobLog implements Serializable {

    private String taskSourceId;

    private boolean isSucceed;

    private String error;

    private String method;

    private String machineIp;

    private String jobType;

    private String createdTime;
}
