package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.EveryDayMaxSingleId;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
public interface EveryDayMaxSingleIdDao {


    void recordTodayMaxSingleId(EveryDayMaxSingleId everyDayMaxSingleId);

    int getMaxNumByDate(String yesterdayDate);
}
