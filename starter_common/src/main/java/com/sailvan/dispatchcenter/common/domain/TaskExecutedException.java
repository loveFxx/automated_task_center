package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

@Data
public class TaskExecutedException {

    private int id;

    private String taskType;

    private String error;

    private int num;

    private int hour;

    private String date;

    private String createdAt;
}
