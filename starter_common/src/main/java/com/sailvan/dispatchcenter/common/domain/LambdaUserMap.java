package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

@Data
public class LambdaUserMap {

    private Integer id;
    private Integer taskId;
    private Integer lambdaAccountId;
    private String accountName;
    private Integer regionId;
    private Integer functionId;
    private String accessKey;
    private String accessSecret;
    private String region;
    private String functionName;
    private String createdAt;
    private String updatedAt;


}
