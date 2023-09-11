package com.sailvan.dispatchcenter.common.constant;

public class CacheKey {

    public final static String BEGIN_DELAY               = "begin:delay";
    public final static String RESULT_1M_DELAY           = "result:1mDelay";
    public final static String RESULT_10M_DELAY           = "result:10mDelay";
    public final static String RESULT_40M_DELAY           = "result:40mDelay";
    public final static String RESULT_2H_DELAY           = "result:2hDelay";
    public final static String RESULT_22H_DELAY           = "result:22hDelay";

    /**
     *  任务结果自增id
     */
    public final static String TASK_RESULT_ID = "taskResultId";



    /**
     *  任务库周期任务自增id
     */
    public final static String TASK_RESULT_CIRCLE_ID = "taskSourceId_circle";

    /**
     *  任务库单次任务自增id
     */
    public final static String TASK_RESULT_SINGLE_ID = "taskSourceId_single";

    /**
     * 周期任务 数据库标识
     */
    public final static String CIRCLE = "circle";

    /**
     * 单次任务 数据库标识
     */
    public final static String SINGLE = "single";

    /**
     *  单表容量
     */
    public final static int SINGLE_TABLE_CAPACITY = 10000000;

    /**
     * 客户端存储在中心端的浏览器token
     */
    public final static String CSRF_TOKEN = "csrf_token";

    /**
     * 代理池
     */
    public final static String PROXY_POOL = "proxyPool:";
}
