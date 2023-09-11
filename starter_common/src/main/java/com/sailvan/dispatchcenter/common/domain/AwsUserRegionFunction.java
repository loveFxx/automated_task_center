package com.sailvan.dispatchcenter.common.domain;



import lombok.Data;

import java.util.List;

@Data
public class AwsUserRegionFunction {

    private String name;
    private String value;
    List<AwsUserRegionFunction> children;

}
