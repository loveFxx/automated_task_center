package com.sailvan.dispatchcenter.common.constant;

public class Event {

    public final static int CREATE_TASK = 1; //生成任务并入任务池缓冲区

    public final static int IN_POOL = 2;    //出任务池缓冲区并入任务池

    public final static int OUT_POOL = 3;   //出池

    public final static int RETURN_RESULT_SUCCESS = 4;      //返回结果成功

    public final static int RETURN_RESULT_FAILURE = 5;        //返回结果失败

    public final static int TIMEOUT_FAILURE = 7; //任务超时结果强制失败(未执行任务)

    public final static int LOWEST_PRIORITY_OUT_POOL = 8; //池满，低优先级出池

    public final static int MANUAL_CREATE_TASK = 9; //手工生成任务

    public final static int MANUAL_IN_POOL = 10; //手工入池
}
