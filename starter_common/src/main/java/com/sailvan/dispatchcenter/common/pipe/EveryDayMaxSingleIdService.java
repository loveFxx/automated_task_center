package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.EveryDayMaxSingleId;

public interface EveryDayMaxSingleIdService {


    public void recordTodayMaxSingleId(EveryDayMaxSingleId everyDayMaxSingleId);

    int getMaxNumByDate(String yesterdayDate);
}
