package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

@Data
public class AwsRegion {

    private int id;
    private String region ;
    private String regionCn;
    private String createdAt;
    private String updatedAt;

}
