package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TaskMetric implements Serializable {

    private int id;

    private String taskType;

    private int generatedNum;

    private int unExecutedNum;

    private int totalSucceedNum;

    private int succeedNum;

    private int fileNum;

    private int failedNum;

    private String date;

    private int hour;

    private String createdAt;
}
