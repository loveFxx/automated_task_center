package com.sailvan.dispatchcenter.common.domain;


import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.io.Serializable;

/**
 * @author mh
 * @date 21-04
 *  机器根据大类型(1、账号大洲 2、平台)有哪些可执行任务
 */
@Data
public class MachineTaskType implements Serializable {

    private int id;
    private int machineId;

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


    private JSONArray taskTypeNameArray;
    private String[] taskTypeNameStringArray;

    /**
     * 0无效 1有效
     */
    private int status = 0;
}
