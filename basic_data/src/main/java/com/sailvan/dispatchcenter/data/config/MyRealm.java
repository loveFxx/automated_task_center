package com.sailvan.dispatchcenter.data.config;

import com.sailvan.dispatchcenter.common.domain.system.Permission;
import com.sailvan.dispatchcenter.common.domain.system.UserDomain;
import com.sailvan.dispatchcenter.db.service.system.PermissionService;
import com.sailvan.dispatchcenter.db.service.system.RoleService;
import com.sailvan.dispatchcenter.db.service.system.UserService;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

/**
 * MyRealm 对用户赋予不同的权限 例如:add、delete、update
 * @date 2021-04
 * @author menghui
 */
public class MyRealm extends AuthorizingRealm {

    private static Logger logger = LoggerFactory.getLogger(MyRealm.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;


    /**
     *
     * 功能描述: 授权
     *
     * @param:
     * @return:
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection arg0) {
        //授权
        logger.info("授予角色和权限");
        // 添加权限 和 角色信息
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        // 获取当前登陆用户
        Subject subject = SecurityUtils.getSubject();
        UserDomain user = (UserDomain) subject.getPrincipal();
        System.out.println(user);
        String admin = "";
        if (admin.equals(user.getSysUserName())) {
            // 超级管理员，添加所有角色、添加所有权限
            authorizationInfo.addRole("*");
            authorizationInfo.addStringPermission("*");
        } else {
            // 普通用户，查询用户的角色，根据角色查询权限
            String permissions = user.getPermissions();
            String[] ids = permissions.split(",");
            for (String id : ids) {
                if(!id.contains("/")){
                    // 角色对应的权限数据
                    Permission perm = permissionService.getById(Integer.parseInt(id));
                    authorizationInfo.addRole(perm.getName());
                    if (null != perm ) {
                        // 授权角色下所有权限
                        authorizationInfo.addStringPermission(perm.getUrl());
                    }
                }else {
                    String[] operating = id.split("/");
                    id = operating[0];
                    // 角色对应的权限数据
                    Permission perm = permissionService.getById(Integer.parseInt(id));
                    authorizationInfo.addRole(perm.getName());
                    Set<String> stringPermissions = authorizationInfo.getStringPermissions();
                    if(stringPermissions == null || stringPermissions.isEmpty()){
                        authorizationInfo.setStringPermissions(getPermissions(perm, operating[1]));
                    }else {
                        stringPermissions.addAll(getPermissions(perm, operating[1]));
                        authorizationInfo.setStringPermissions(stringPermissions);
                    }
                }
            }
        }
        return authorizationInfo;
    }




    public Set<String> getPermissions(Permission perm, String operatings){
        Set<String> set = new HashSet<String>();
        int diff =  Integer.parseInt(operatings) - perm.getId();
        switch (diff){
            case 1:
                set.add(perm.getDescpt()+":add");
                break;
            case 2:
                set.add(perm.getDescpt()+":delete");
                break;
            case 3:
                set.add(perm.getDescpt()+":update");
                break;
            default:
                break;
        }
        return set;
    }


    /**
     * 功能描述: 认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //UsernamePasswordToken用于存放提交的登录信息
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        logger.info("用户登录认证：验证当前Subject时获取到token为: {}" , ReflectionToStringBuilder
                .toString(token, ToStringStyle.MULTI_LINE_STYLE));
        String username = token.getUsername();
        // 调用数据层
        UserDomain sysUser = userService.findByUserName(username);
        if (sysUser == null) {
            // 用户不存在
            logger.error("用户不存在：username {}" , username);
            return null;
        }
        logger.debug("用户登录认证！用户信息user：{}" , sysUser);
        // 返回密码
        return new SimpleAuthenticationInfo(sysUser, sysUser.getSysUserPwd(), ByteSource.Util.bytes(username), getName());

    }

}
