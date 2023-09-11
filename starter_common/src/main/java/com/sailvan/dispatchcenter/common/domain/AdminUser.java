package com.sailvan.dispatchcenter.common.domain;

import lombok.Data;


/**
 * AdminUser
 * @author mh
 * @date 2021
 */
@Data
public class AdminUser {

    private Integer id;

    private String sysUserName;

    private String sysUserPwd;

    private Integer roleId;

    private String roleName;

    private String userPhone;


    private String regTime;


    private Integer userStatus;

}
