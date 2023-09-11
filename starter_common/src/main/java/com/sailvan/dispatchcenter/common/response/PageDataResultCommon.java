package com.sailvan.dispatchcenter.common.response;

import java.util.List;

/**
 * @Title: 接口使用
 * @Description: 封装DTO分页数据（记录数和所有记录）
 * @date 2021-04
 * @author menghui
 */
public class PageDataResultCommon{

    private Integer code = 1;

    private String msg = "success";

    private Integer totals;

    private List<?> lists;


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }


    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getTotals() {
        return totals;
    }

    public void setTotals(Integer totals) {
        this.totals = totals;
    }

    public List<?> getLists() {
        return lists;
    }

    public void setLists(List<?> lists) {
        this.lists = lists;
    }
}
