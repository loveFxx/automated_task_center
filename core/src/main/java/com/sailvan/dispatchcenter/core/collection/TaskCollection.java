package com.sailvan.dispatchcenter.core.collection;

import com.sailvan.dispatchcenter.common.domain.TaskBufferMeta;

import java.util.concurrent.PriorityBlockingQueue;

public class TaskCollection extends TaskCollectionFactory{

    int minCapacity = 1000;

    private PriorityBlockingQueue<TaskBufferMeta> taskBufferMetaPriorityBlockingQueue;

    public void setTaskBufferMetaPriorityBlockingQueue(PriorityBlockingQueue<TaskBufferMeta> taskBufferMetaPriorityBlockingQueue) {
        this.taskBufferMetaPriorityBlockingQueue = taskBufferMetaPriorityBlockingQueue;
    }

    @Override
    void pushTask(String data) {

    }

    //构建缓冲队列数据源
    public TaskBufferMeta buildTaskBufferMeta(int priority, String taskBufferId){
        TaskBufferMeta taskBufferMeta = new TaskBufferMeta();
        taskBufferMeta.setPriority(priority);
        taskBufferMeta.setTaskBufferId(taskBufferId);
        return taskBufferMeta;
    }

    public void clearQueue(){
        taskBufferMetaPriorityBlockingQueue.clear();
    }

    public boolean put(TaskBufferMeta taskBufferMeta){
        return taskBufferMetaPriorityBlockingQueue.offer(taskBufferMeta);
    }

    /**
     * 检验是否在重启初始化状态，不是调用此方法
     * @param priority
     * @param taskBufferId
     * @return
     */
    public boolean offerPriorityBlockingQueue(int priority, String taskBufferId){
        TaskBufferMeta taskBufferMeta = buildTaskBufferMeta(priority, taskBufferId);
        return put(taskBufferMeta);
    }

    public boolean offerQueue(TaskBufferMeta taskBufferMeta){
        return put(taskBufferMeta);
    }

    //从缓冲队列获取数据
    public String getTaskBufferId(){
        TaskBufferMeta taskBufferMeta = taskBufferMetaPriorityBlockingQueue.poll();
        if (taskBufferMeta != null){
            return taskBufferMeta.getTaskBufferId();
        }
        return "";
    }

    public TaskBufferMeta getTaskBuffer(){
        return taskBufferMetaPriorityBlockingQueue.poll();
    }

    public int getPriority(){
        TaskBufferMeta taskBufferMeta = taskBufferMetaPriorityBlockingQueue.peek();
        if (taskBufferMeta != null){
            return taskBufferMeta.getPriority();
        }
        return 0;
    }

    public boolean removeQueue(int priority, String taskBufferId){
        TaskBufferMeta taskBufferMeta = buildTaskBufferMeta(priority, taskBufferId);
        return taskBufferMetaPriorityBlockingQueue.remove(taskBufferMeta);
    }
}
