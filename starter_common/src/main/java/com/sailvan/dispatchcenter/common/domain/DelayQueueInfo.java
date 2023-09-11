package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

@Data
public class DelayQueueInfo {

    private String systemOrTask;
    private int oneMinuteDelay;
    private int tenMinuteDelay;
    private int fortyMinuteDelay;
    private int twoHourDelay;
    private int twentyTwoHourDelay;

}
