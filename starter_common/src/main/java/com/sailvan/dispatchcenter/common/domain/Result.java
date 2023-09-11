package com.sailvan.dispatchcenter.common.domain;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;

@Data
public class Result {

    private int clientCode;

    private String taskType;

    private String resultType;

    private int needRetry;

    private String errorLevel;

    private String taskSourceId;

    @JsonRawValue
    private String clientResult;

    @JsonRawValue
    private String centerParams;

    @JsonRawValue
    private String returnParams;

    private String clientMsg;

    private String clientError;

    private int retryTimes;

    private String createdTime;
}
