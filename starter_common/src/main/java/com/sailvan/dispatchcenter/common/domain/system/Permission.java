package com.sailvan.dispatchcenter.common.domain.system;


import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Title: Permission
 * @Description:
 * @author mh
 * @date 2021
 */
@Data
public class Permission implements Serializable {

    private static final long serialVersionUID = -3L;

    private Integer id;

    private String name;
    private String value;

    private Integer pid;

    private String pname;

    private String descpt;

    private String url;

    private String createTime;

    private String updateTime;

    private Integer delFlag;

    List<String> operating = new ArrayList<>();
    List<Permission> children;
    List<Permission> childrens;
}
