package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: automated_task_center
 * @description:机器分类情况
 * @author: Wu Xingjian
 * @create: 2021-12-15 16:32
 **/
@Data
public class MachineTypeDTO implements Serializable {

    /**
     * 总数
     */
    private int machineTotalCount;

    /**
     * 具有（半小时内）心跳的机器数
     */
    private int machineWithLivingHeartBeatCount;

    /**
     * 开启机器数
     */
    private int machineStatusOnCount;

}
