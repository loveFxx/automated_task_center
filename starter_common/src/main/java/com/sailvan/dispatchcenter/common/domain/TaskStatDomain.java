package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

/**
 * @program: automated_task_center
 * @description: 为了方便统计任务成功数失败数创建的pojo
 * @author: Wu Xingjian
 * @create: 2021-11-15 16:39
 **/

@Data

public class TaskStatDomain {

    private int totalCount = 0;

    private int successCount = 0;

    private int failedCount = 0;

    private int forceFailedCount = 0;

    private String forceFailedList="";

    private int fileCount = 0;

    private int totalSuccessCount = 0;

    private int unExecutedCount = 0;

    private int inPoolCount = 0;

}
