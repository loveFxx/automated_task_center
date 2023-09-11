package com.sailvan.dispatchcenter.common.domain;

public class TaskBufferMeta implements Comparable<TaskBufferMeta>{

    private int priority;

    private String taskBufferId;

    public int getPriority() {
        return priority;
    }

    public String getTaskBufferId() {
        return taskBufferId;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setTaskBufferId(String taskBufferId) {
        this.taskBufferId = taskBufferId;
    }

    @Override
    public int compareTo(TaskBufferMeta e) {
        if (this.priority > e.getPriority()) {
            return -1;
        } else if (this.priority < e.getPriority()) {
            return 1;
        } else {
            return 0;
        }
    }

}
