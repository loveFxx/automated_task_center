package com.sailvan.dispatchcenter.common.domain;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;

@Data
public class TaskResult {

    private int id;

    private String taskBufferId;

    private int clientCode;

    private String taskType;

    private String resultType;

    private int needRetry;

    private String errorLevel;

    private String taskSourceId;

    private int uniqueId;

    private int resultHashKey;

    @JsonRawValue
    private String clientResult;

    @JsonRawValue
    private String centerParams;

    @JsonRawValue
    private String returnParams;

    private String refreshTime;

    private String clientMsg;

    private String clientError;

    private int retryTimes;

    private String createdTime;

    private String account;

    private String site;

    private String workType;

    private String ip;

    private int runMode = 0;
}
