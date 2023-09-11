package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: automated_task_center
 * @description:
 * @author: Wu Xingjian
 * @create: 2021-12-15 17:10
 **/
@Data
public class MachineTypeCountDTO implements Serializable {

    private String machineType;

    /**
     * 总数
     */
    private int totalCount;

    /**
     * 具有有效心跳数
     */
    private int machineWithLivingHeartbeatCount;

    /**
     * 开启机器数
     */
    private int machineStatusOnCount;


}
