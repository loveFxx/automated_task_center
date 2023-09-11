package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

/**
 *  响应类
 * @author
 */
@Data
public class ApiResponseDomain {

    private int code;

    private String msg;

    /**
     * 响应体
     */
    private Object content;
}
