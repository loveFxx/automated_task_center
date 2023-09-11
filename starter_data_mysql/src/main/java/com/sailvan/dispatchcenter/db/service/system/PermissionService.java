package com.sailvan.dispatchcenter.db.service.system;

import com.sailvan.dispatchcenter.common.domain.system.Permission;
import com.sailvan.dispatchcenter.common.domain.system.UserDomain;
import com.sailvan.dispatchcenter.db.dao.automated.system.PermissionDao;
import com.sailvan.dispatchcenter.db.dao.automated.system.RoleDao;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @Title: PermissionServiceImpl
 * @Description:
 * @date 2021-04
 * @author menghui
 *
 */
@Service
public class PermissionService implements com.sailvan.dispatchcenter.common.pipe.system.PermissionService {

    private static Logger logger = LoggerFactory.getLogger(PermissionService.class);

    @Autowired
    private PermissionDao permissionDao;

    @Autowired
    private RoleDao roleDao;

    @Override
    public Map<String, Object> addPermission(Permission permission) {
        Map<String,Object> data = new HashMap(16);
        try {
            permission.setCreateTime(DateUtils.getCurrentDate());
            permission.setUpdateTime(DateUtils.getCurrentDate());
            permission.setDelFlag(1);
            int result = permissionDao.insert(permission);
            if(result == 0){
                data.put("code",0);
                data.put("msg","新增失败！");
                logger.error("权限[新增]，结果=新增失败！");
                return data;
            }
            data.put("code",1);
            data.put("msg","新增成功！");
            logger.info("权限[新增]，结果=新增成功！");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("权限[新增]异常！", e);
            return data;
        }
        return data;
    }

    @Override
    public Map <String, Object> updatePermission(Permission permission) {
        Map<String,Object> data = new HashMap(16);
        try{
            permission.setUpdateTime(DateUtils.getCurrentDate());
            int result = permissionDao.updatePermission(permission);
            if(result == 0){
                data.put("code",0);
                data.put("msg","更新失败！");
                logger.error("权限[更新]，结果=更新失败！");
                return data;
            }
            data.put("code",1);
            data.put("msg","更新成功！");
            logger.info("权限[更新]，结果=更新成功！");
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("权限[更新]异常！", e);
            return data;
        }
        return data;
    }


    @Override
    public PageDataResult getPermissionList(Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);
        List<Permission> permissions = permissionDao.getPermissionList();

        PageDataResult pageDataResult = new PageDataResult();
        if(permissions.size() != 0){
            PageInfo<Permission> pageInfo = new PageInfo<>(permissions);
            pageDataResult.setList(permissions);
            pageDataResult.setTotals((int) pageInfo.getTotal());
            pageDataResult.setPageNum(pageNum);
        }
        return pageDataResult;
    }

    @Override
    public List<Permission> parentPermissionList() {
        List<Permission> permissions = permissionDao.parentPermissionList();
        for (Permission permission : permissions) {
            List<Permission> operating = new ArrayList<>();
            operating(operating, permission, 1, "add");
            operating(operating, permission, 2, "delete");
            operating(operating, permission, 3, "update");
            permission.setChildren(operating);
        }
        return permissions;
    }

    private void operating(List<Permission> permissionList,Permission permission,int ope,String operating){
        Permission permission1 = new Permission();
        permission1.setId(permission.getId()+ope);
        permission1.setValue(permission.getId()+ope+"");
        permission1.setName(operating);
        permissionList.add(permission1);
    }

    @Override
    public Map <String, Object> del(int id) {
        Map<String, Object> data = new HashMap<>(16);
        try {
            // 删除权限菜单
            int result = permissionDao.deleteByPrimaryKey(id);
            if(result == 0){
                data.put("code",0);
                data.put("msg","删除失败");
                logger.error("删除失败");
                return data;
            }
            data.put("code",1);
            data.put("msg","删除成功");
            logger.info("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("删除权限菜单异常！", e);
        }
        return data;
    }


    @Override
    public Permission getById(int id) {
        return permissionDao.selectByPrimaryKey(id);
    }

    @Override
    public Map <String, Object> getUserPerms(UserDomain user) {
        Map<String, Object> data = new HashMap<>(16);
        String permissions = user.getPermissions();
        if(!StringUtils.isEmpty(permissions)){
            String[] ids = permissions.split(",");
            List<Permission> permissionList = new ArrayList<>();
            for (String id : ids) {
                // 角色对应的权限数据
                if (id.contains("/")) {
                    id = id.split("/")[0];
                }
                Permission perm = permissionDao.selectByPrimaryKey(Integer.parseInt(id));
                if (null != perm ) {
                    // 授权角色下所有权限
                    Permission permissionDTO = new Permission();
                    BeanUtils.copyProperties(perm,permissionDTO);
                    //获取子权限
                    List<Permission> childrens = permissionDao.getPermissionListByPid(perm.getId());
                    permissionDTO.setChildrens(childrens);
                    if(!permissionList.contains(permissionDTO)){
                        permissionList.add(permissionDTO);
                    }
                }
            }
            Collections.sort(permissionList, (Permission p1, Permission p2) -> p1.getId().compareTo(p2.getId()));
            data.put("perm",permissionList);
        }
        return data;
    }

//    public Map <String, Object> getUserPerms(UserDomain user) {
//        Map<String, Object> data = new HashMap<>();
//        Integer roleId = user.getRoleId();
//
//        RoleDomain role = roleDao.selectByPrimaryKey(roleId);
//        if (null != role ) {
//            String permissions = role.getPermissions();
//
//            String[] ids = permissions.split(",");
//            List<Permission> permissionList = new ArrayList <>();
//            for (String id : ids) {
//                // 角色对应的权限数据
//                Permission perm = permissionDao.selectByPrimaryKey(Integer.parseInt(id));
//                if (null != perm ) {
//                    // 授权角色下所有权限
//                    Permission permissionDTO = new Permission();
//                    BeanUtils.copyProperties(perm,permissionDTO);
//                    //获取子权限
//                    List<Permission> childrens = permissionDao.getPermissionListByPId(perm.getId());
//                    permissionDTO.setChildrens(childrens);
//                    permissionList.add(permissionDTO);
//                }
//            }
//            data.put("perm",permissionList);
//        }
//
//        return data;
//    }
}
