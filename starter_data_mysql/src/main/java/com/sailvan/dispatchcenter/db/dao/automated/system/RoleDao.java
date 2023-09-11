package com.sailvan.dispatchcenter.db.dao.automated.system;

import com.sailvan.dispatchcenter.common.domain.system.RoleDomain;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RoleDao
 * @date 2021-04
 * @author menghui
 */
@Mapper
public interface RoleDao {

    /**
     *  获取角色
     * @return
     */
    List<RoleDomain> getRoleList();

    /**
     *  获取
     * @return
     */
    List<RoleDomain> getRoles();

    /**
     *  ID查询
     * @param id
     * @return
     */
    RoleDomain selectByPrimaryKey(int id);

    /**
     *  更新
     * @param role
     * @return
     */
    int updateRole(RoleDomain role);

    /**
     *  插入
     * @param role
     * @return
     */
    int insert(RoleDomain role);

    /**
     *  更新
     * @param id
     * @param roleStatus
     * @return
     */
    int updateRoleStatus(@Param("id") Integer id, @Param("roleStatus") Integer roleStatus);

}