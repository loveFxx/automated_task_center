package com.sailvan.dispatchcenter.db.service;


import com.sailvan.dispatchcenter.common.domain.EveryDayMaxSingleId;
import com.sailvan.dispatchcenter.db.dao.automated.EveryDayMaxSingleIdDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EveryDayMaxSingleIdService implements com.sailvan.dispatchcenter.common.pipe.EveryDayMaxSingleIdService {

    @Autowired
    EveryDayMaxSingleIdDao everyDayMaxSingleIdDao;

    @Override
    public void recordTodayMaxSingleId(EveryDayMaxSingleId everyDayMaxSingleId) {
        everyDayMaxSingleIdDao.recordTodayMaxSingleId(everyDayMaxSingleId);
    }

    @Override
    public int getMaxNumByDate(String yesterdayDate) {
        return everyDayMaxSingleIdDao.getMaxNumByDate(yesterdayDate);
    }

}

