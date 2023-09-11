package com.sailvan.dispatchcenter.common.constant;

/**
 *  请求数量
 *  @author mh
 *  @date 2021-12
 */
public class RequestCountCode {

    public final String[] stages = new String[]{REQUEST_NUM,REQUEST_TIMEOUT, REQUEST_EXCEPTION, REQUEST_LIMIT,REQUEST_SUCCESS};
    /**
     *  请求总数
     */
    public final static String REQUEST_NUM = "REQUEST_NUM";

    /**
     *  请求总数
     */
    public final static String REQUEST_SUCCESS = "REQUEST_SUCCESS";

    /**
     *  请求超时异常
     */
    public final static String REQUEST_TIMEOUT = "REQUEST_TIMEOUT";

    /**
     *  请求异常
     */
    public final static String REQUEST_EXCEPTION = "REQUEST_EXCEPTION";

    /**
     *  限流异常
     */
    public final static String REQUEST_LIMIT = "REQUEST_LIMIT";


}
