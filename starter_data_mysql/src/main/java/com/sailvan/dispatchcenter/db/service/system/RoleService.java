package com.sailvan.dispatchcenter.db.service.system;

import com.sailvan.dispatchcenter.common.domain.system.Permission;
import com.sailvan.dispatchcenter.common.domain.system.RoleDomain;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Title: AdminRoleServiceImpl
 * @Description:
 * @date 2021-04
 * @author menghui
 */
@Service
public class RoleService implements com.sailvan.dispatchcenter.common.pipe.system.RoleService {

    private static Logger logger = LoggerFactory.getLogger(RoleService.class);

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private PermissionDao permissionDao;

    @Override
    public PageDataResult getRoleList(Integer pageNum, Integer pageSize) {
        PageDataResult pageDataResult = new PageDataResult();
        List<RoleDomain> roles = roleDao.getRoleList();

        List<RoleDomain> roleList = new ArrayList <>();
        for(RoleDomain r:roles){
            RoleDomain roleDTO =  new RoleDomain();

            String permissions = r.getPermissions();
            BeanUtils.copyProperties(r,roleDTO);
            roleDTO.setPermissionIds(permissions);

            if(!StringUtils.isEmpty(permissions)){
                String[] ids = permissions.split(",");
                List<String> p = new ArrayList <>();
                for(String id: ids){
                    if(id.contains("/")){
                        id = id.split("/")[0];
                    }
                    Permission permission = this.permissionDao.selectByPrimaryKey(Integer.parseInt(id));
                    String name = permission.getName();
                    p.add(name);
                }
                roleDTO.setPermissions(p.toString());
            }
            roleList.add(roleDTO);
        }

        PageHelper.startPage(pageNum, pageSize);

        if(roleList.size() != 0){
            PageInfo<RoleDomain> pageInfo = new PageInfo<>(roleList);
            pageDataResult.setList(roleList);
            pageDataResult.setTotals((int) pageInfo.getTotal());
            pageDataResult.setPageNum(pageNum);
        }
        return pageDataResult;
    }

    @Override
    public Map<String,Object> addRole(RoleDomain role) {
        Map<String,Object> data = new HashMap(16);
        try {
            role.setCreateTime(DateUtils.getCurrentDate());
            role.setUpdateTime(DateUtils.getCurrentDate());
            role.setRoleStatus(1);
            int result = roleDao.insert(role);
            if(result == 0){
                data.put("code",0);
                data.put("msg","新增角色失败");
                logger.error("新增角色失败");
                return data;
            }
            data.put("code",1);
            data.put("msg","新增角色成功");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("添加角色并授权！异常！", e);
        }
        return data;

    }

    @Override
    public RoleDomain findRoleById(Integer id) {
        return roleDao.selectByPrimaryKey(id);
    }

    @Override
    public Map<String,Object> updateRole(RoleDomain role) {
        Map<String,Object> data = new HashMap(16);
        try{
            role.setUpdateTime(DateUtils.getCurrentDate());
            int result = roleDao.updateRole(role);
            if(result == 0){
                data.put("code",0);
                data.put("msg","更新失败！");
                logger.error("角色[更新]，结果=更新失败！");
                return data;
            }
            data.put("code",1);
            data.put("msg","更新成功！");
            logger.info("角色[更新]，结果=更新成功！");
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("角色[更新]异常！", e);
            return data;
        }
        return data;
    }

    @Override
    public Map<String, Object> delRole(Integer id,Integer status) {
        Map<String, Object> data = new HashMap<>(16);
        try {
            int result = roleDao.updateRoleStatus(id,status);
            if(result == 0){
                data.put("code",0);
                data.put("msg","删除角色失败");
            }
            data.put("code",1);
            data.put("msg","删除角色成功");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("删除角色异常！", e);
        }
        return data;
    }

    @Override
    public Map <String, Object> recoverRole(Integer id, Integer status) {
        Map<String, Object> data = new HashMap<>(16);
        try {
            int result = roleDao.updateRoleStatus(id,status);
            if(result == 0){
                data.put("code",0);
                data.put("msg","恢复角色失败");
            }
            data.put("code",1);
            data.put("msg","恢复角色成功");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("恢复角色异常！", e);
        }
        return data;
    }

    @Override
    public List<RoleDomain> getRoles() {
        return roleDao.getRoleList();
    }
}
