package com.sailvan.dispatchcenter.common.response;

import java.util.List;

/**
 * @Title: PageDataResult
 * @Description: 封装DTO分页数据（记录数和所有记录）
 * @date 2021-04
 * @author menghui
 */
public class PageDataResult {

    private Integer code=200;

    /**
     * 总记录数量
     */
    private Integer totals;

    private int pageNum = 0;

    private List<?> list;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getTotals() {
        return totals;
    }

    public void setTotals(Integer totals) {
        this.totals = totals;
    }

    public List <?> getList() {
        return list;
    }

    public void setList(List <?> list) {
        this.list = list;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    @Override
    public String toString() {
        return "PageDataResult{" +
                "code=" + code +
                ", totals=" + totals +
                ", list=" + list +
                '}';
    }
}
