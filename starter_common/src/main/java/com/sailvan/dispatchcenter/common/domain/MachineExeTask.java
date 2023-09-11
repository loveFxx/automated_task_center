package com.sailvan.dispatchcenter.common.domain;


import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.io.Serializable;

/**
 * 机器执行任务
 * @author mh
 * @date 21-12
 */
@Data
public class MachineExeTask implements Serializable {

    private int id;
    private String ip;

    private String taskType;
    private String period;

    private int taskTotal = 0;
    private int taskSuccess = 0;
    private int taskFail = 0;
    private int totalTime = 0;
    private int avgTime = 0;


    private String createdTime;
    private String updatedTime;

}
