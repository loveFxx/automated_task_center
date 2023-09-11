package com.sailvan.dispatchcenter.common.domain;


import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author mh
 * @date 21-12
 *  请求数
 */
@Data
public class RequestCount implements Serializable {

    private int id;
    private String period;
    private String systemName;
    private String requestMethod;
    private String requestNum;

    private String requestSuccess;
    private String requestTimeout;
    private String requestException;
    private String requestLimit;
    private String updateTime;
}
