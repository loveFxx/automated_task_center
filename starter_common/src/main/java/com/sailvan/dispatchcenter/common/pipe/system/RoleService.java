package com.sailvan.dispatchcenter.common.pipe.system;

import com.sailvan.dispatchcenter.common.domain.system.RoleDomain;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

import java.util.List;
import java.util.Map;

/**
 * @Title: AdminRoleServiceImpl
 * @Description:
 * @date 2021-04
 * @author menghui
 */
public interface RoleService {



    public PageDataResult getRoleList(Integer pageNum, Integer pageSize) ;

    public Map<String,Object> addRole(RoleDomain role) ;

    public RoleDomain findRoleById(Integer id) ;

    public Map<String,Object> updateRole(RoleDomain role) ;

    public Map<String, Object> delRole(Integer id, Integer status);

    public Map <String, Object> recoverRole(Integer id, Integer status);

    public List<RoleDomain> getRoles() ;
}
