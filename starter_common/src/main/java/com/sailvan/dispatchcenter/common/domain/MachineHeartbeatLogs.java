package com.sailvan.dispatchcenter.common.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;


/**
 * @author mh
 * @date 21-07
 *  客户端机器心跳日志
 */
@Data
public class MachineHeartbeatLogs implements Serializable {

    private static final long serialVersionUID = -1169571L;

    private int id;
    private int machineId;
    private int netWork;
    private String cpu;
    private String memory;
    private String diskSpace;

    private String workType;

    private String types;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private String heartbeat;

    private String date;

    private String createdTime;

    private String clientVersion;

    private String clientFileVersion;
    private String tracertIps;

}
