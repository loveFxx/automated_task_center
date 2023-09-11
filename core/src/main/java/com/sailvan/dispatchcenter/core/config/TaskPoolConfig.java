package com.sailvan.dispatchcenter.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "taskpool")
public class TaskPoolConfig {

    //任务池上限数量
    private int maxNum;

    //任务最大入池次数
    private int maxInPoolTimes;

    private int batchLimitNum;

    public int getMaxNum() {
        return maxNum;
    }

    public void setMaxNum(int maxNum) {
        this.maxNum = maxNum;
    }

    public int getMaxInPoolTimes() {
        return maxInPoolTimes;
    }

    public void setMaxInPoolTimes(int maxInPoolTimes) {
        this.maxInPoolTimes = maxInPoolTimes;
    }

    public int getBatchLimitNum() {
        return batchLimitNum;
    }

    public void setBatchLimitNum(int batchLimitNum) {
        this.batchLimitNum = batchLimitNum;
    }
}
