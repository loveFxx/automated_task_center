package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

@Data
public class TaskSourceList {

    private int id;

    private int uniqueId;

    private int taskId;

    private int type;

    private String systemId;

    private String systemName;

    private int priority;

    private int isEnforced;

    private int isSingle;

    private String workType;

    private String params;

    private String returnParams;

    private String expectedTime;

    private String lastCreateTime;

    private String lastResultTime;

    private String refreshTime;

    private String jobName;

    private String createdAt;

    private String updatedAt;

    private String taskName;//通过taskId从task表获得

    private String taskState;//状态 用于列表展示
}
