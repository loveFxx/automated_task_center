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
public class MachineCrawlPlatform implements Serializable {

    private int id;
    private int machineId;
    private String machineIp;

    private int platformId;
    private String platformName;


    /**
     * 0无效 1有效
     */
    private int status = 0;

    private String createdTime;
    private String updatedTime;
    private String deletedTime;

}
