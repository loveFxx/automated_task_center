package com.sailvan.dispatchcenter.es.domain;

import java.util.List;
import java.util.Map;

public class Table {

    private String name;
    private String index;
    Map<String, String> fields;
    List<Object> list;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public List<Object> getList() {
        return list;
    }

    public void setList(List<Object> list) {
        this.list = list;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
