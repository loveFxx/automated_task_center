package com.sailvan.dispatchcenter.common.domain;


import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.io.Serializable;

/**
 * @author mh
 * @date 21-11
 *  机器执行大类型
 *  机器根据大类型(1、账号大洲 2、平台)有哪些可执行任务
 */
@Data
public class MachineWorkType implements Serializable {

    private int id;
    private int machineId;
    private int isBrowser;
    private String machineIp;

    private int platformId;
    private String platform;


    private String platformName;
    private String account;
    private String continents;

    /**
     *  1可爬取平台 2账号平台
     *  #Constant.LARGE_TASK_TYPE_CRAWL_PLATFORM
     *
     */
    private int platformType = -1;
    private String taskTypeName;

    /**
     *  用来存储 大任务类型下的小类型名字
     */
    private String workTypeTaskName;


    private JSONArray taskTypeNameArray;
    private String[] taskTypeNameStringArray;

    /**
     * 0无效 1有效
     */
    private int status = 0;
    private int isUpdate = 0;

    private String createdTime;
    private String updatedTime;
    private String deletedTime;

    /**
     * 后台登录方式： 1.账号机  2.超级浏览器
     */
    private int loginPlatform;
    /**
     * 机器表中的状态 此字段用于与status字段一起判断机器状态
     */
    private int machineStatus = 0;


}
