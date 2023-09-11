package com.sailvan.dispatchcenter.common.domain.system;

import lombok.Data;

import java.io.Serializable;


/**
 * @Title: AdminUser
 * @Description:
 * @author mh
 * @date 2021
 */
@Data
public class UserDomain implements Serializable {

    private static final long serialVersionUID = -1L;

    private Integer id;

    private String sysUserName;

    private String sysUserPwd;

    private String roleName;

    private String userPhone;


    private String regTime;


    private Integer userStatus;

    private String permissions;
    private String permissionIds;

}
