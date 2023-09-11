package com.sailvan.dispatchcenter.core.scheduler;

import com.sailvan.dispatchcenter.common.constant.CacheKey;
import com.sailvan.dispatchcenter.common.domain.TaskResult;
import com.sailvan.dispatchcenter.common.pipe.TaskResultService;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.core.async.AsyncPushTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Set;

@Component
public class DelayedSendResult {

    private static Logger logger = LoggerFactory.getLogger(DelayedSendResult.class);

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    TaskResultService taskResultService;

    @Autowired
    AsyncPushTask asyncPushTask;

    @Scheduled(cron = "*/10 * * * * ?")
    public void delay() throws Exception {
        long currentTimeMillis = System.currentTimeMillis();

        check(currentTimeMillis, CacheKey.RESULT_1M_DELAY,60);
        check(currentTimeMillis,CacheKey.RESULT_10M_DELAY,10*60-60);
        check(currentTimeMillis,CacheKey.RESULT_40M_DELAY,40*60-10*60);
        check(currentTimeMillis,CacheKey.RESULT_2H_DELAY,2*60*60-10*60);
        check(currentTimeMillis,CacheKey.RESULT_22H_DELAY,22*60*60-2*60*60);
    }

    private void check(long currentTimeMillis, String cacheKey,int seconds) throws Exception {
        Set range = redisUtils.rangeWithScores(cacheKey, 0, 1000);
        if (range != null && !range.isEmpty()) {
            Iterator obj = range.iterator();
            while (obj.hasNext()) {
                DefaultTypedTuple next = (DefaultTypedTuple) obj.next();
                Double score = next.getScore();
                Object value = next.getValue();
                if (score != null) {
                    if (currentTimeMillis - score > seconds * 1000) {
                        redisUtils.remove(cacheKey,value);
                        String[] split = String.valueOf(value).split("_");
                        TaskResult taskResult = taskResultService.findById(Integer.parseInt(split[0]));
                        if (taskResult != null){
                            asyncPushTask.sendSystem(taskResult, split[1],cacheKey);
                        }
                    }
                }
            }
        }
    }
}
