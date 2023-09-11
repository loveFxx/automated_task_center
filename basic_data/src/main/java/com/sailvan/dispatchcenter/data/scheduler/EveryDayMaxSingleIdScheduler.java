package com.sailvan.dispatchcenter.data.scheduler;


import com.sailvan.dispatchcenter.common.config.CoreServiceAddressConfig;
import com.sailvan.dispatchcenter.common.constant.CacheKey;
import com.sailvan.dispatchcenter.common.domain.EveryDayMaxSingleId;
import com.sailvan.dispatchcenter.common.util.TaskUtil;
import com.sailvan.dispatchcenter.common.pipe.*;
import com.sailvan.dispatchcenter.data.controller.VersionController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class EveryDayMaxSingleIdScheduler {

    private static Logger logger = LoggerFactory.getLogger(EveryDayMaxSingleIdScheduler.class);

    @Autowired
    TaskUtil taskUtil;

    @Autowired
    CoreServiceAddressConfig coreServiceAddressConfig;


    @Autowired
    EveryDayMaxSingleIdService everyDayMaxSingleIdService;

    /**
     * 0点记录昨天的单次任务最大id
     */
    @Scheduled(cron = "30 58 23 * * ?")
    public  void recordEveryDayMaxSingleId(){
        Date d = new Date();
//        Date dateBefore = DateUtils.minusDay(1,d);
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd");
        String yesterday = sdf.format(d);
        int idCache = taskUtil.getIdCache(CacheKey.TASK_RESULT_SINGLE_ID,coreServiceAddressConfig.getPath());
        if(idCache == 0){
            logger.info("recordEveryDayMaxSingleId idCache:{}, time:{}", idCache, yesterday);
            return;
        }
        EveryDayMaxSingleId everyDayMaxSingleId = new EveryDayMaxSingleId();
        everyDayMaxSingleId.setTaskSourceListSingleId(idCache);
        everyDayMaxSingleId.setCurrentIdDate(yesterday);
        everyDayMaxSingleIdService.recordTodayMaxSingleId(everyDayMaxSingleId);
        logger.info("recordEveryDayMaxSingleId idCache:{}, time:{}", idCache, yesterday);

    }

}
