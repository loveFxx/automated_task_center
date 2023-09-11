package com.sailvan.dispatchcenter.common.domain;
import lombok.Data;

import java.io.Serializable;


@Data
public class Column implements Serializable {

    private int id;

    private int taskId;

    private String columnsName;

    private String columnType;

    private String columnExplain;

    /**
     * 是否必传
     */
    private int isRequired;

    /**
     * 是否为中心端的生成字段(周期性不传，单次性必传)
     */
    private int isCombined;

    /**
     * 是否需要加入组装参数（用来判重任务）
     */
    private int isCombinedUnique;

    /**
     * 需要带在结果返回的字段标识
     */
    private int isReturnFlag;

    /**
     * 返回任务库id的标识
     */
    private int isIdFlag;
}
