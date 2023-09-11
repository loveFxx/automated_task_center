package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

@Data
public class TaskInPoolMetric {

    private int id;

    private String taskType;

    private int bufferNum;

    private int oneHourNum;

    private int oneToThreeNum;

    private int threeToFiveNum;

    private int fiveToTenNum;

    private int tenToOneDayNum;

    private int overOneDayNum;

    private int hour;

    private String date;

    private String createdAt;
}
