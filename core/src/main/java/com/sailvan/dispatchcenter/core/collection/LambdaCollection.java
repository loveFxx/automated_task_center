package com.sailvan.dispatchcenter.core.collection;

import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.TaskBufferMeta;
import com.sailvan.dispatchcenter.core.domain.TaskBuffer;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class LambdaCollection extends TaskCollection {

    int minCapacity = 1000;

    private static PriorityBlockingQueue<TaskBufferMeta> lambdaQueue = new PriorityBlockingQueue<>();

    private final static LambdaCollection instance = new LambdaCollection();

    private LambdaCollection() {
        super.setTaskBufferMetaPriorityBlockingQueue(lambdaQueue);
    }

    public static LambdaCollection instance() {
        return instance;
    }

    public static PriorityBlockingQueue<TaskBufferMeta> getLambdaQueue() {
        return lambdaQueue;
    }

    @Override
    public void pushTask(String data) {

    }

    @Override
    public boolean offerPriorityBlockingQueue(int priority, String taskBufferId){
        TaskBufferMeta taskBufferMeta = buildTaskBufferMeta(priority, taskBufferId);
        return put(taskBufferMeta);
    }

    public void incrementQueue(TaskBuffer taskBuffer){
        offerPriorityBlockingQueue(taskBuffer.getPriority(),taskBuffer.getId());
        if (Constant.taskTypeInLambdaNum.containsKey(taskBuffer.getType())){
            Constant.taskTypeInLambdaNum.get(taskBuffer.getType()).incrementAndGet();
        }else {
            Constant.taskTypeInLambdaNum.put(taskBuffer.getType(),new AtomicInteger(0));
        }
    }


}
