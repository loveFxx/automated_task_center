package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

/**
 * 记录每天最大的单次任务id 用于查询时减小范围
 */
@Data
public class EveryDayMaxSingleId {

    private int id;

    private int taskSourceListSingleId;

    private String currentIdDate;
}
