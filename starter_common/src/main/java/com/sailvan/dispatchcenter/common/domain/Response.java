package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;

/**
 * 响应
 * @date 2021-04
 * @author
 */
@Data
public class Response {

    private String code;

    private String msg;

    private Object data;

    /**
     * 响应体
     */
    private Object content;
}
