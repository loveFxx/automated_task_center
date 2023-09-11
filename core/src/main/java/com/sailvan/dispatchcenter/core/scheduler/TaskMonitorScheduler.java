package com.sailvan.dispatchcenter.core.scheduler;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.Task;
import com.sailvan.dispatchcenter.common.pipe.TaskService;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.core.collection.LambdaCollection;
import com.sailvan.dispatchcenter.core.pool.TaskPool;
import org.apache.lucene.util.RamUsageEstimator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TaskMonitorScheduler {

    private static final String POOL_BEFORE_NUM  = "pool_before_num"; //任务池任务数量

    private static final String POOL_BEFORE_SIZE  = "pool_before_size"; //任务池占用内存大小

    private static final String BEFORE_QPS  = "before_qps";

    private static Logger logger = LoggerFactory.getLogger(TaskMonitorScheduler.class);

    @Autowired
    TaskPool taskPool;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    TaskService taskService;

    @Scheduled(cron = "*/5 * * * * ?")
    public void taskMonitor(){

        AtomicInteger poolNum = taskPool.getTotalNum();
        long poolSize = RamUsageEstimator.sizeOf(taskPool.getHashLinkedList().toString().getBytes());
        int qps = getQueryPerSeconds();

        int poolBeforeNum = 0;
        int poolBeforeSize = 0;
        int beforeQps = 0;


        Object poolBeforeNumObject = redisUtils.get(POOL_BEFORE_NUM);
        Object poolBeforeSizeObject = redisUtils.get(POOL_BEFORE_SIZE);
        Object beforeQpsObject = redisUtils.get(BEFORE_QPS);

        if (poolBeforeNumObject != null){
            poolBeforeNum = Integer.parseInt(String.valueOf(poolBeforeNumObject));
        }
        if (poolBeforeSizeObject != null){
            poolBeforeSize = Integer.parseInt(String.valueOf(poolBeforeSizeObject));
        }
        if (beforeQpsObject != null){
            beforeQps = Integer.parseInt(String.valueOf(beforeQpsObject));
        }

        if (poolBeforeNum != poolNum.get()+ LambdaCollection.getLambdaQueue().size() || poolBeforeSize != poolSize || beforeQps != qps){
            redisUtils.put(POOL_BEFORE_NUM,String.valueOf(poolNum.get()+LambdaCollection.getLambdaQueue().size()),1200L);
            redisUtils.put(POOL_BEFORE_SIZE,String.valueOf(poolSize),1200L);
            redisUtils.put(BEFORE_QPS,String.valueOf(qps),1200L);
            logger.info("任务总数据量: {}, 任务池数量:{},Lambda池子数量:{},任务池大小为: {}B, {}KB, {}MB,接口{}--qps:{}",poolNum.get()+ LambdaCollection.getLambdaQueue().size(),poolNum.get(),LambdaCollection.getLambdaQueue().size(),poolSize,(poolSize/(1024)),(poolSize/(1024*1024)),Constant.ADD_TASK_SOURCE,qps);
        }
    }

    private int getQueryPerSeconds(){
        Object after = redisUtils.get(Constant.ADD_TASK_SOURCE + Constant.SUFFIX_AFTER);
        Object before = redisUtils.get(Constant.ADD_TASK_SOURCE + Constant.SUFFIX_BEFORE);
        int qps = 0;
        if (after != null){
            int afterTimes = Integer.parseInt(String.valueOf(after));
            int beforeTimes = 0;
            if (before != null){
                beforeTimes = Integer.parseInt(String.valueOf(before));
                if (beforeTimes > afterTimes){
                    beforeTimes = 0;
                }
            }
            qps = (afterTimes - beforeTimes) / 5;
            redisUtils.put(Constant.ADD_TASK_SOURCE + Constant.SUFFIX_BEFORE,String.valueOf(after),3600L);
        }
        return qps;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void initBloomFilter(){
        Constant.bloomFilter = BloomFilter.create(
                //Funnel接口实现类的实例，它用于将任意类型T的输入数据转化为Java基本类型的数据（byte、int、char等等）。这里是会转化为byte。
                Funnels.stringFunnel(Charset.forName("utf-8")),
                //期望插入元素总个数n
                5000000,
                //误差率p
                0.01);
    }
}
