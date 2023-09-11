package com.sailvan.dispatchcenter.core.scheduler;

import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.constant.Event;
import com.sailvan.dispatchcenter.common.domain.Task;
import com.sailvan.dispatchcenter.common.domain.TaskMetadata;
import com.sailvan.dispatchcenter.common.pipe.TaskService;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.core.collection.LambdaCollection;
import com.sailvan.dispatchcenter.core.config.TaskPoolConfig;
import com.sailvan.dispatchcenter.core.domain.TaskBuffer;
import com.sailvan.dispatchcenter.core.pool.TaskPool;
import com.sailvan.dispatchcenter.core.service.TaskBufferService;
import com.sailvan.dispatchcenter.core.service.TaskLogsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sailvan.dispatchcenter.common.constant.Constant.priorityBlockingQueue;

@Component
public class TransferBufferToPoolScheduler {

    private static Logger logger = LoggerFactory.getLogger(TransferBufferToPoolScheduler.class);
    @Autowired
    TaskPool taskPool;

    @Autowired
    TaskPoolConfig taskPoolConfig;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    TaskBufferService taskBufferService;

    @Autowired
    TaskLogsService taskLogsService;

    @Autowired
    TaskService taskService;

    @Scheduled(cron = "1/10 * * * * ?")
    public void transferBufferToPool() {

        while ((taskPool.getTotalNum().get() + LambdaCollection.getLambdaQueue().size()) < taskPoolConfig.getMaxNum()) {

            Integer priority = priorityBlockingQueue.peek();
            if (priority == null){
                break;
            }

            //获取运行模式包含lambda的任务数据
            boolean flag1 = runLambda(priority);

            boolean flag2 = runMachine(priority);

            if (flag1 && flag2){
                priorityBlockingQueue.remove(priority);
            }
        }
        if ((taskPool.getTotalNum().get() + LambdaCollection.getLambdaQueue().size()) >= taskPoolConfig.getMaxNum()){
            List<TaskBuffer> taskBuffers = taskBufferService.listLowestPriorityPoolTasks(1);
            for (TaskBuffer taskBuffer : taskBuffers) {
                Integer priority = priorityBlockingQueue.peek();
                if (priority != null){
                    TaskBuffer taskBufferInfo = taskBufferService.findPriorityTaskBuffer(priority);
                    if (taskBufferInfo == null){
                        priorityBlockingQueue.remove(priority);
                    }else {
                        if (priority != 0 && priority > taskBuffer.getPriority()){
                            switch (taskBuffer.getPool_type()){
                                case 0:
                                    removeFromPool(taskBuffer);
                                    break;
                                case 1:
                                    removeFromLambda(taskBuffer);
                                    break;
                                default:break;
                            }
                            if (taskBufferInfo.getRun_mode() == Constant.LOCAL_MACHINE || taskBufferInfo.getRun_mode() == Constant.LOCAL_MACHINE_AND_LAMBDA){
                                pushTaskPool(taskBufferInfo);
                            }else {
                                pushLambdaCollection(taskBufferInfo);
                            }
                        }else {
                            break;
                        }
                    }
                }
            }
        }

        List<Task> tasks = taskService.getTasksByRunMode(Constant.LOCAL_MACHINE_AND_LAMBDA);

        for (Task task : tasks){
            if (!Constant.taskTypeInPoolNum.containsKey(task.getTaskName())){
                Constant.taskTypeInPoolNum.put(task.getTaskName(),new AtomicInteger(0));
            }
            if (!Constant.taskTypeInLambdaNum.containsKey(task.getTaskName())){
                Constant.taskTypeInLambdaNum.put(task.getTaskName(),new AtomicInteger(0));
            }

            while (Constant.taskTypeInPoolNum.get(task.getTaskName()).get() > 2000 && Constant.taskTypeInLambdaNum.get(task.getTaskName()).get() == 0) {
                //从mongo取2000的四分之一500 批量更新运行模式,转到lambda
                List<TaskBuffer> taskBuffers = taskBufferService.listBuffersByPoolType(Constant.MACHINE, 500);
                List<String> ids = new ArrayList<>();
                for (TaskBuffer taskBuffer : taskBuffers) {
                    taskPool.deleteData(taskBuffer.getWork_type(), taskBuffer.getType(), taskBuffer.getPriority(), taskBuffer.getId());
                    LambdaCollection.instance().incrementQueue(taskBuffer);
                    ids.add(taskBuffer.getId());
                }
                taskBufferService.updatePoolTypeByIds(Constant.LAMBDA_CONTAINER, ids);
            }
        }
    }

    /**
     * 从池子移除数据
     * @param taskBuffer
     */
    private void removeFromPool(TaskBuffer taskBuffer){
        redisUtils.remove(Constant.TASK_PREFIX + taskBuffer.getId());  //redis移除任务
        taskPool.deleteData(taskBuffer.getWork_type(), taskBuffer.getType(), taskBuffer.getPriority(), taskBuffer.getId()); //任务池移除数据
        taskBufferService.offerQueue(taskBuffer.getPriority());
        taskBufferService.updateIsInPoolAndInBufferTimeById(0, taskBuffer.getId());
        taskLogsService.addTaskLogs(taskBuffer, Event.LOWEST_PRIORITY_OUT_POOL,"",Constant.MACHINE);
        logger.info("池满，低优先级出池入缓冲区，任务缓冲区ID:{}，任务库ID-{}", taskBuffer.getId(), taskBuffer.getTask_source_id());
    }

    private void removeFromLambda(TaskBuffer taskBuffer){
        redisUtils.remove(Constant.TASK_PREFIX + taskBuffer.getId());  //redis移除任务
        LambdaCollection.instance().removeQueue(taskBuffer.getPriority(),taskBuffer.getId());
        if (Constant.taskTypeInLambdaNum.containsKey(taskBuffer.getType())){
            Constant.taskTypeInLambdaNum.get(taskBuffer.getType()).decrementAndGet();
        }
        taskBufferService.offerQueue(taskBuffer.getPriority());
        taskBufferService.updateIsInPoolAndInBufferTimeById(0, taskBuffer.getId());
        taskLogsService.addTaskLogs(taskBuffer, Event.LOWEST_PRIORITY_OUT_POOL,"",Constant.LAMBDA_CONTAINER);
        logger.info("池满，低优先级出Lambda池入缓冲区，任务缓冲区ID:{}，任务库ID-{}", taskBuffer.getId(), taskBuffer.getTask_source_id());
    }


    private boolean runLambda(int priority){
        List<Integer> lambdaRunModeLists = taskBufferService.buildLambdaRunModeLists();
        List<TaskBuffer> lambdaTaskBuffers = taskBufferService.listPriorityTaskBufferByRunMode(priority, lambdaRunModeLists);
//        List<TaskBuffer> lambdaTaskBuffers = taskBufferService.listTaskBufferByRunMode(lambdaRunModeLists);
        if (lambdaTaskBuffers == null || lambdaTaskBuffers.isEmpty()){
            return true;
        }else {
            for (TaskBuffer lambdaTaskBuffer: lambdaTaskBuffers) {
                pushLambdaCollection(lambdaTaskBuffer);
            }
        }
        return false;
    }

    private boolean runMachine(int priority){
        List<Integer> machineRunModeLists = taskBufferService.buildMachineRunModeLists();
        List<TaskBuffer> machineTaskBuffers = taskBufferService.listPriorityTaskBufferByRunMode(priority, machineRunModeLists);
//        List<TaskBuffer> machineTaskBuffers = taskBufferService.listTaskBufferByRunMode(machineRunModeLists);

        if (machineTaskBuffers == null || machineTaskBuffers.isEmpty()){
            logger.info("缓冲区入任务池，获取数量{}",0);
            return true;
        }else {
            logger.info("缓冲区入任务池，获取数量{}",machineTaskBuffers.size());
            for (TaskBuffer machineTaskBuffer: machineTaskBuffers) {
                logger.info("开始入池，bufferId:{}",machineTaskBuffer.getId());
                pushTaskPool(machineTaskBuffer);
            }
        }
        return false;
    }

    /**
     * 入池操作
     * @param taskBuffer
     */
    private void pushTaskPool(TaskBuffer taskBuffer){
        beforeInPool(taskBuffer);
        taskPool.push(taskBuffer.getWork_type(), taskBuffer.getType(), taskBuffer.getPriority(), taskBuffer.getId());
        afterInPool(taskBuffer,0,Constant.MACHINE);
    }

    private void pushLambdaCollection(TaskBuffer taskBuffer){
        beforeInPool(taskBuffer);
        LambdaCollection.instance().incrementQueue(taskBuffer);
        afterInPool(taskBuffer,1,Constant.LAMBDA_CONTAINER);
    }

    private void beforeInPool(TaskBuffer taskBuffer){
        TaskMetadata taskMetadata = taskPool.buildTaskMetadata(taskBuffer);
        String resultJson = JSONObject.toJSONString(taskMetadata);
        redisUtils.put(Constant.TASK_PREFIX + taskBuffer.getId(), resultJson, 3600 * 24 * 2L);
    }


    private void afterInPool(TaskBuffer taskBuffer,int poolType, int runMode){
        logger.info("任务由任务缓冲区进入任务池，任务库ID:{} 任务缓冲区ID:{}，当前任务池数量--{}", taskBuffer.getTask_source_id(), taskBuffer.getId(), taskPool.getTotalNum());
        int inPoolTimes = taskBuffer.getIn_pool_times() + 1;
        taskBufferService.updateIsInPoolAndInPoolTimeById(1, inPoolTimes, taskBuffer.getId(), poolType);
        taskLogsService.addTaskLogs(taskBuffer, Event.IN_POOL, "",runMode);
    }
}
