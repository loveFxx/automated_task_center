package com.sailvan.dispatchcenter.common.domain;


import lombok.Data;

import java.io.Serializable;

/**
 * @author mh
 * @date 21-04
 *  客户端机器信息
 */
@Data
public class MachineShop implements Serializable {

    private int id;
    private int machineId;
    private String platform;
    private String account;
    private String continents;
}
