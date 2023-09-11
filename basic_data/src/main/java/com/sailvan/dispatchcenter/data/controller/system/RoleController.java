package com.sailvan.dispatchcenter.data.controller.system;

import com.sailvan.dispatchcenter.common.domain.system.RoleDomain;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.db.service.system.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 角色管理 RoleController
 * @date 2021-04
 * @author menghui
 */
@Controller
@RequestMapping("role")
public class RoleController {

    private static Logger logger = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private RoleService roleService;

    /**
     * 跳转到角色管理
     * @return
     */
    @RequestMapping("/roleManage")
    public String toPage() {
        if (logger.isTraceEnabled()) {
            logger.trace("进入角色管理");
        }
        return "/role/roleManage";
    }

    /**
     *
     * 功能描述: 获取角色列表
     *
     * @param:
     * @return:
     */
    @RequestMapping(value = "/getRoleList", method = RequestMethod.GET)
    @ResponseBody
    public PageDataResult getRoleList(@RequestParam("pageNum") Integer pageNum,
                                      @RequestParam("pageSize") Integer pageSize) {
        if (logger.isTraceEnabled()) {
            logger.trace("获取角色列表");
        }
        PageDataResult pdr = new PageDataResult();
        try {
            if(null == pageNum) {
                pageNum = 1;
            }
            if(null == pageSize) {
                pageSize = 10;
            }
            // 获取角色列表
            pdr = roleService.getRoleList(pageNum ,pageSize);
            if (logger.isTraceEnabled()) {
                logger.trace("角色列表查询=pdr:" + pdr);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("角色列表查询异常！", e);
        }
        return pdr;
    }

    /**
     *
     * 功能描述: 获取角色列表
     *
     * @param:
     * @return:
     */
    @GetMapping("getRoles")
    @ResponseBody
    public List<RoleDomain> getRoles(){
        if (logger.isTraceEnabled()) {
            logger.trace("获取角色列表" );
        }
        return roleService.getRoles();
    }

    /**
     *
     *述: 设置角色[新增或更新]
     *
     * @param:
     * @return:
     */
    @PostMapping("setRole")
    @ResponseBody
    public Map<String,Object> setRole(RoleDomain role) {
        if (logger.isTraceEnabled()) {
            logger.trace("设置角色[新增或更新]！role:" + role );
        }
        Map<String,Object> data = new HashMap(16);
        if(role.getId() == null){
            //新增角色
            data = roleService.addRole(role);
        }else{
            //修改角色
            data = roleService.updateRole(role);
        }
        return data;
    }


    /**
     *
     * 功能描述: 删除/恢复角色
     *
     * @param:
     * @return:
     */
    @PostMapping("updateRoleStatus")
    @ResponseBody
    public Map<String,Object> updateRoleStatus(@RequestParam("id") int id, @RequestParam("status") Integer status) {
        if (logger.isTraceEnabled()) {
            logger.trace("删除/恢复角色！id:" + id+" status:"+status);
        }
        Map<String, Object> data = new HashMap<>(16);
        if(status == 0){
            //删除角色
            data = roleService.delRole(id,status);
        }else{
            //恢复角色
            data = roleService.recoverRole(id,status);
        }
        return data;
    }

}
