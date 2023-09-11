package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ClientRequestLog implements Serializable {

    /**
     * 请求地址
     */
    private String url;

    /**
     * url归类
     */
    private String urlCategory;

    /**
     * http code
     */
    private int httpCode;


    /**
     * 执行状态--布尔值
     */
    private boolean code;

    /**
     * 枚举执行状态--指代具体
     */
    private int status;

    /**
     * 代理Ip
     */
    private String proxyIp;

    /**
     * 对代理IP裁剪处理后
     */
    private String shortProxyIp;

    /**
     * 出口外网IP
     */
    private String outIpRecord;

    /**
     * 拨号出口外网IP
     */
    private String outIp;

    /**
     * 机器Ip
     */
    private String machineIp;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 任务执行大类型
     */
    private String workType;

    /**
     * 任务执行小类型
     */
    private String jobType;

    /**
     * 请求开始时间戳
     */
    private long requestTime;

    /**
     * 是否封禁
     */
    private boolean isBlocked;

    /**
     * 被封权重
     */
    private int weight;

    /**
     * 创建时间
     */
    private String createdTime;

    /**
     * 任务库ID
     */
    private String taskSourceId;
}
