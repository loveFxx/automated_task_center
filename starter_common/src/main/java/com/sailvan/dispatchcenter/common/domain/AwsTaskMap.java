package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

@Data
public class AwsTaskMap {

    private Integer id;
    private Integer awsLambdaMapId;
    private Integer taskId;
    private String taskName;

}
