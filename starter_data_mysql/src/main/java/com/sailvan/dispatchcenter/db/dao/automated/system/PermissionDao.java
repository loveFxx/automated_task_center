package com.sailvan.dispatchcenter.db.dao.automated.system;


import com.sailvan.dispatchcenter.common.domain.system.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * PermissionDao
 * @date 2021-04
 * @author menghui
 */
@Mapper
public interface PermissionDao {

    /**
     *  获取权限
     * @return
     */
    List<Permission> getPermissionList();

    /**
     *  插入
     * @param permission
     * @return
     */
    int insert(Permission permission);

    /**
     *  删除
     * @param id
     * @return
     */
    int deleteByPrimaryKey(int id);

    /**
     *  父权限
     * @return
     */
    List<Permission> parentPermissionList();

    /**
     *  通过id 查询
     * @param id
     * @return
     */
    Permission selectByPrimaryKey(int id);

    /**
     *  更新
     * @param permission
     * @return
     */
    int updatePermission(Permission permission);

    /**
     *  查询子权限
     * @param pid
     * @return
     */
    List<Permission> getPermissionListByPid(@Param("pid") Integer pid);
}