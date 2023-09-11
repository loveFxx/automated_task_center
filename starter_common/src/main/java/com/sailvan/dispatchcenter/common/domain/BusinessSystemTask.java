package com.sailvan.dispatchcenter.common.domain;


import lombok.Data;

import java.io.Serializable;

/**
 * @author mh
 * @date 21-11
 *  业务系统执行的任务
 */
@Data
public class BusinessSystemTask implements Serializable {


    private int id;

    private int systemId;

    private String systemName;

    private int taskId;

    private String taskName;

    private String taskCallbackAddress;
    private String apiVersion;

    private int status = 0;
    private int networkType;

    private String createTime;

    private String updateTime;

    private String deleteTime;


}
