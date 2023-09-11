package com.sailvan.dispatchcenter.common.domain;


import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;

/**
 * @author mh
 * @date 21-06
 *  业务系统
 */
@Data
public class BusinessSystem implements Serializable {

    private int id;

    /**
     * 业务系统名称
     */
    private String systemName;
    private String appKey;
    private String appSecret;

    /**
     * 设置当前业务系统每月执行次数
     */
    private int invokeTimesMonth;
    private int invokeTimesMonthUsed = 0;

    /**
     * 执行间隔分钟min
     */
    private int invokeInterval;


    /**
     * 在invokeInterval 分钟内 执行次数
     */
    private int invokeTimes;

    private int is;

    /**
     * 可执行任务类型名
     */
    private String taskTypeName;

    /**
     * 任务结果回调地址
     */
    private String callbackAddress;


    /**
     *  业务系统上次登录时间
     */
    private String lastLogin;

    private String updateUser;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private String updateTime;

    /**
     * 1正常 0禁用
     */
    private int status = -1;

    /**
     * 界面 可选择的任务名
     */
    private JSONArray taskTypeSelect;
    /**
     * 网络连接类型
     */
    private int networkType;

    /**
     * 版本号
     */
    private String systemVersion;


}
