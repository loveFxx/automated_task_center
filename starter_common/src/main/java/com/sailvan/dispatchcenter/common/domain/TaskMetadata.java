package com.sailvan.dispatchcenter.common.domain;

import java.util.LinkedHashMap;

public class TaskMetadata {

    private String work_type; //大类型

    private String type;    //任务类型

    private int priority;   //优先级

    private int is_enforced;  ///是否强制

    private LinkedHashMap client_params;  //客户端传递参数

    private LinkedHashMap  center_params;    //中心端生成参数

    private LinkedHashMap  return_params;    //任务结果需带回来的参数

    private int retry_times;

    private String username;

    private String password;

    public String getWork_type() {
        return work_type;
    }

    public void setWork_type(String work_type) {
        this.work_type = work_type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public LinkedHashMap getClient_params() {
        return client_params;
    }

    public void setClient_params(LinkedHashMap client_params) {
        this.client_params = client_params;
    }

    public LinkedHashMap getCenter_params() {
        return center_params;
    }

    public void setCenter_params(LinkedHashMap center_params) {
        this.center_params = center_params;
    }

    public LinkedHashMap getReturn_params() {
        return return_params;
    }

    public void setReturn_params(LinkedHashMap return_params) {
        this.return_params = return_params;
    }

    public int getRetry_times() {
        return retry_times;
    }

    public void setRetry_times(int retry_times) {
        this.retry_times = retry_times;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getIs_enforced() {
        return is_enforced;
    }

    public void setIs_enforced(int is_enforced) {
        this.is_enforced = is_enforced;
    }
}
