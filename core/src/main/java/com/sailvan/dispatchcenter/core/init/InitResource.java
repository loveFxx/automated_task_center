package com.sailvan.dispatchcenter.core.init;

import com.sailvan.dispatchcenter.common.config.BusSystemConfig;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.Task;
import com.sailvan.dispatchcenter.common.pipe.AwsTaskMapService;
import com.sailvan.dispatchcenter.common.pipe.TaskService;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.core.pool.TaskPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@Component
public class InitResource implements CommandLineRunner {

    @Resource
    TaskPool taskPool;

    @Resource
    BusSystemConfig busSystemConfig;


    @Autowired
    TaskService taskService;

    @Autowired
    AwsTaskMapService awsTaskMapService;

    @Autowired
    RedisUtils redisUtils;

    @Override
    public void run(String... strings) throws Exception {

        Map<String, String> idcMap = new HashMap<>();
        idcMap.put("appKey", busSystemConfig.getIdcAppkey());
        idcMap.put("appSecret", busSystemConfig.getIdcAppSecret());
        idcMap.put("router", busSystemConfig.getIdcRouter());
        Map<String, String> szMap = new HashMap<>();
        szMap.put("appKey", busSystemConfig.getSzAppkey());
        szMap.put("appSecret", busSystemConfig.getSzAppSecret());
        szMap.put("router", busSystemConfig.getSzRouter());
        Map<String, String> hkMap = new HashMap<>();
        hkMap.put("appKey", busSystemConfig.getHkAppkey());
        hkMap.put("appSecret", busSystemConfig.getHkAppSecret());
        hkMap.put("router", busSystemConfig.getHkRouter());
        Constant.MAP_BUS_SYSTEM.put("idc",idcMap);
        Constant.MAP_BUS_SYSTEM.put("sz",szMap);
        Constant.MAP_BUS_SYSTEM.put("hk",hkMap);

        List<Task> tasks = taskService.listTask();
        int num = 1;
        for (Task task : tasks) {
            Constant.taskTypeInPoolNum.put(task.getTaskName(),new AtomicInteger(0));


            if (task.getLargeTaskType() == Constant.LARGE_TASK_TYPE_CRAWL_PLATFORM){
                num = 5;
            }
            Constant.TASK_POP_NUM_MAP.put(task.getTaskName(),num);
            if (task.getIntervalTimes() == 0){
                Constant.FOREVER_TASKS.add(task.getTaskName());
            }

            if (task.getRunMode() == 1 || task.getRunMode() == 2){
                HashMap relationMap = awsTaskMapService.getRelationMap(task.getId());
                if (relationMap != null && !relationMap.isEmpty()){
                    Constant.TASK_LAMBDA_MAP.put(task.getTaskName(),relationMap);
                }
            }
        }

        //任务池初始化
        taskPool.init();
    }
}
