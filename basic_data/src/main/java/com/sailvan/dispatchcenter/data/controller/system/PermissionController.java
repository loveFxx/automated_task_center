package com.sailvan.dispatchcenter.data.controller.system;


import com.sailvan.dispatchcenter.common.domain.system.Permission;
import com.sailvan.dispatchcenter.common.domain.system.UserDomain;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.db.service.system.PermissionService;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限管理 PermissionController
 * @date 2021-04
 * @author menghui
 */
@Controller
@RequestMapping("permission")
public class PermissionController {

    private static Logger logger = LoggerFactory.getLogger(PermissionController.class);

    @Autowired
    private PermissionService permissionService;



    /**
     * 获取权限菜单列表
     */
    @PostMapping("permissionList")
    @ResponseBody
    public PageDataResult permissionList(@RequestParam("pageNum") Integer pageNum,
                                         @RequestParam("pageSize") Integer pageSize){
        if (logger.isTraceEnabled()) {
            logger.trace("获取权限菜单列表");
        }
        PageDataResult pdr = new PageDataResult();
        try {
            if(null == pageNum) {
                pageNum = 1;
            }
            if(null == pageSize) {
                pageSize = 10;
            }
            // 获取服务类目列表
            pdr = permissionService.getPermissionList(pageNum ,pageSize);
            if (logger.isTraceEnabled()) {
                logger.trace("权限菜单列表查询=pdr:" + pdr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("权限菜单列表查询异常！", e);
        }
        return pdr;
    }


    /**
     * 功能描述: 获取根权限菜单列表
     * @return
     */
    @GetMapping("parentPermissionList")
    @ResponseBody
    public List<Permission> parentPermissionList(){
        if (logger.isTraceEnabled()) {
            logger.trace("获取根权限菜单列表");
        }
        return permissionService.parentPermissionList();
    }




    /**
     * 功能描述:设置权限[新增或更新]
     * @return:
     */
    @PostMapping("setPermission")
    @ResponseBody
    public Map<String,Object> setPermission(Permission permission) {
        if (logger.isTraceEnabled()) {
            logger.trace("设置权限[新增或更新]！permission:" + permission);
        }
        Map<String,Object> data = new HashMap(16);
        if(permission.getId() == null){
            //新增权限
            data = permissionService.addPermission(permission);
        }else{
            //修改权限
            data = permissionService.updatePermission(permission);
        }
        return data;
    }

    /**
     *  删除权限菜单
     * @return:
     */
    @PostMapping("del")
    @ResponseBody
    public Map<String, Object> del(@RequestParam("id") int id) {
        if (logger.isTraceEnabled()) {
            logger.trace("删除权限菜单！id:" + id);
        }
        Map<String, Object> data = new HashMap<>(16);
        //删除服务类目类型
        data = permissionService.del(id);
        return data;
    }



    /**
     * 功能描述: 获取登陆用户的权限
     * @return:
     */
    @GetMapping("getUserPerms")
    @ResponseBody
    public Map<String, Object> getUserPerms(){
        if (logger.isTraceEnabled()) {
            logger.trace("获取登陆用户的权限");
        }
        Map<String, Object> data = new HashMap<>(16);
        UserDomain user = (UserDomain) SecurityUtils.getSubject().getPrincipal();
        data = permissionService.getUserPerms(user);
        return data;
    }

}
