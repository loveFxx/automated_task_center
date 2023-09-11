package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

import java.util.LinkedHashMap;

@Data
public class TaskWaitResult {

    private int taskSourceId;

    private int resultHashKey;

    private LinkedHashMap centerParams;    //中心端生成参数

    private String refreshTime;
}
