package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;


/**
 *  AdminRole
 * @author mh
 * @date 2021
 */
@Data
public class AdminRole {
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
