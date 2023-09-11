package com.sailvan.dispatchcenter.common.domain;


import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.io.Serializable;

/**
 * @author mh
 * @date 21-11
 *  机器执行大类型下的具体小类型
 */
@Data
public class MachineWorkTypeTask implements Serializable {

    private int id;
    private int machineId;
    private int workTypeId;

    private int taskId;
    private String taskName;

    private int status;

    private int isUpdate = 0;

    private String createdTime;
    private String updateTime;
    private String deletedTime;

}
