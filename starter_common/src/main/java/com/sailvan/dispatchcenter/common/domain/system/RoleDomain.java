package com.sailvan.dispatchcenter.common.domain.system;

import lombok.Data;

import java.io.Serializable;


/**
 * @Title: AdminRole
 * @Description:
 * @author mh
 * @date 2021
 */
@Data
public class RoleDomain implements Serializable {

    private static final long serialVersionUID = -2L;

    private Integer id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色描述
     */
    private String roleDesc;

    private String permissionIds;
    /**
     * 权限
     */
    private String permissions;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 1：有效
     0：无效
     */
    private Integer roleStatus;
}
