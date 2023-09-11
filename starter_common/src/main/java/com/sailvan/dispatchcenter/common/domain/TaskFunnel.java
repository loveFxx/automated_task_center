package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

import java.math.BigInteger;

@Data
public class TaskFunnel {

    private int id;

    private int taskId;

    private String taskName;

    private int isMain;

    private int taskNum;

    private BigInteger nextFireTime;
}
