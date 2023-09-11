package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: automated_task_center
 * @description:机器大版本情况+机器小版本情况
 * @author: Wu Xingjian
 * @create: 2021-12-15 16:37
 **/
@Data
public class MachineVersionStatDTO implements Serializable {

    private String version;

    /**
     * machine_type=0 账号机的数量
     */
    private int accountMachineCount;

    /**
     * machine_type=1 内网VPS的数量
     */
    private int innerVPSCount;

    /**
     * machine_type=2 外网VPS的数量
     */
    private int overseaVPSCount;

    /**
     * machine_type=3 重庆VPS的数量
     */
    private int ChongqingVpsCount;

    /**
     * machine_type=4 重庆账号机的数量
     */
    private int ChongqingAccountMachineCount;

}
