package com.sailvan.dispatchcenter.common.pipe.system;

import com.sailvan.dispatchcenter.common.domain.system.Permission;
import com.sailvan.dispatchcenter.common.domain.system.UserDomain;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

import java.util.List;
import java.util.Map;

/**
 * @Title: PermissionServiceImpl
 * @Description:
 * @date 2021-04
 * @author menghui
 *
 */

public interface PermissionService {


    public Map<String, Object> addPermission(Permission permission);


    public Map <String, Object> updatePermission(Permission permission);


    public PageDataResult getPermissionList(Integer pageNum, Integer pageSize);

    public List<Permission> parentPermissionList() ;

    public Map <String, Object> del(int id);


    public Permission getById(int id);

    public Map <String, Object> getUserPerms(UserDomain user);

}
