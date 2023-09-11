package com.sailvan.dispatchcenter.core.collection;

import com.sailvan.dispatchcenter.common.domain.TaskBufferMeta;

import java.util.concurrent.PriorityBlockingQueue;

public abstract class TaskCollectionFactory {

    int minCapacity = 1000;

    PriorityBlockingQueue<TaskBufferMeta> taskBufferMetaPriorityBlockingQueue = new PriorityBlockingQueue<>();


    abstract void pushTask(String data);
}
