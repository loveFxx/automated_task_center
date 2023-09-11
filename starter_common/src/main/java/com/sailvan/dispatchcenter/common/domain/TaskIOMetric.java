package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

@Data
public class TaskIOMetric {

    private int id;

    private String taskType;

    private int inPoolNum;

    private int outPoolNum;

    private int hour;

    private String date;

    private String createdAt;
}
