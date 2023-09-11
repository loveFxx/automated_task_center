package com.sailvan.dispatchcenter.common.domain;
import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Task implements Serializable {

    private int id;

    private String taskName;

    private int type;

    private int priority;

    private String cronExpression;

    private String taskExplain;

    private int status;

    /**
     *  运行模式 默认0、机器 1、lamdba运行 2、机器和lambda
     */
    private int runMode;

    /**
     * 任务生成速率 - 单位时间（秒）
     */
    private int produceInterval;

    /**
     * 任务生成速率 - 并发数
     */
    private int produceConcurrency;

    /**
     * 任务生成速率 - 生成数
     */
    private int produceCapacity;

    private int apiTimeLimit;

    private int apiMaxTimes;

    private int intervalTimes;

    private int intervalType;

    private int largeTaskType;

    private String executePlatforms;

    private String systems;

    private List<Column> columnList;

    private String createdAt;

    private String updatedAt;

    private JSONArray platformSelect;

    private JSONArray crawlPlatformSelect;

    private String updatedUser;

    private int isTimely;

    private String taskAbbreviation;

    private String lambdaUserName;

    private String regionFunctionName;

    private String awsUserRegionFunctions;

    private int isCombo = 0;

    private String comboColumns = "";

    //正常运行次数上限
    private int exMaxTimes;

    //异常运行次数上限
    private int errorMaxTimes;

    //任务重试次数上限
    private int limitRetryTimes;

}
