package com.sailvan.dispatchcenter.core.scheduler;

import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.core.collection.LambdaCollection;
import com.sailvan.dispatchcenter.core.service.LambdaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class RequestLambda {

    @Autowired
    LambdaService lambdaService;

    @Autowired
    RedisUtils redisUtils;

    //限制一定量的任务请求到Lambda
    @Scheduled(cron = "*/30 * * * * ?")
    public void requestLambda(){

        ExecutorService executeService = Executors.newCachedThreadPool();
        Set<Callable<String>> callables = new HashSet<Callable<String>>();
        int limit = Constant.LAMBDA_REQUEST_LIMIT.get();
        //根据限制数量去推任务

        int page = 0;
        if (LambdaCollection.getLambdaQueue().size() >= limit){
            page = limit;
        }else {
            page = LambdaCollection.getLambdaQueue().size();
        }

        for (int i=0;i<page;i++){
            callables.add(new Callable<String>() {
                public String call() throws Exception {
                    lambdaService.requestLambda();
                    return "";
                }
            });
        }
        try {
            List<Future<String>> resultList = executeService.invokeAll(callables);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
