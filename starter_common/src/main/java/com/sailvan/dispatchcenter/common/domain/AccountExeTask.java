package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

/**
 * 机器执行任务
 * @author yyj
 * @date 21-12
 */
@Data
public class AccountExeTask {

    private int id;
    private String account;
    private String continent;
    private String period;
    private String taskType;
    private int taskInPool = 0;
    private int taskGet = 0;
    private int taskSuccess = 0;
    private int taskFail = 0;
    private String createdTime;

}
