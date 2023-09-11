package com.sailvan.dispatchcenter.common.domain;


import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author mh
 * @date 21-04
 *  客户端机器信息
 */
@Data
public class Machine implements Serializable {

    private int id;
    private String ids;
    private String ip;
    private String mac;


    private List<MachineShop> machineShopList;

    private List<MachineTaskType> machineTaskTypeList;
    private List<MachineWorkType> machineWorkTypeList;
    private String machineTaskTypeLists;

    private String account;

    private JSONArray accountSelect;

    /**
     * 用来初始化可爬取搜索框(已经去除存在的账号平台)
     */
    private JSONArray crawlPlatformSelect;

//    private String continents;
//    private String platform;
    private String username;
    private String password;

    /**
     * 机器类型 0账号机 1内网VPS 2外网VPS 3重庆VPS 4重庆账号机
     */
    private int machineType = -1;
    private int netWork ;

    private String dialUsername;
    private String dialPassword;

    /**
     *  机器状态 0:禁用、无效  1:正常、有效
     *
     */
    private int status = -1;
    private int machineStatus = -1;

    /**
     * 即将到期 默认-1
     */
    private int expiring = -1;
    private String cpu;
    private String memory;
    private String diskSpace;

    private String dueTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private String startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private String endTime;

    private String serviceProvider;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private String lastHeartbeat;

    private String taskType;

    private JSONArray taskTypeSelect;

    private String lastExecuteTaskType;

    private String lastExecuteTask;
    private String lastExecuteWorkType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private String lastExecuteTime;
    private String token;

    /**
     * 操作系统
     */
    private String operatingSystem;


    /**
     * 可爬取平台
     */
    private String crawlPlatform;

    private String crawlPlatformName;

    private String updateTime;
    private String createdTime;

    private String machineLocalTime;

    private String userNames;
    private String userNum;
    private String clientVersion;
    private String clientFileVersion;
    private int updateMachineStatus = 0;

    /**
     * 最大IO数
     */
    private int maxIO;
}
