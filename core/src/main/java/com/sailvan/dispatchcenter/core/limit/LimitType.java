package com.sailvan.dispatchcenter.core.limit;

/**
 * 限流
 * @author mh
 * @date 2021
 */
public enum LimitType {
    /**
     * 自定义key
     */
    CUSTOMER,
    /**
     * 根据请求者IP
     */
    IP;
}
