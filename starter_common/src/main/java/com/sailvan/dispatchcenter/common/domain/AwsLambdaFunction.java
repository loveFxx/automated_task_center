package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

@Data
public class AwsLambdaFunction {

    private int id;
    private String functionName ;
    private String processNum;
    private String createdAt;
    private String updatedAt;

}
