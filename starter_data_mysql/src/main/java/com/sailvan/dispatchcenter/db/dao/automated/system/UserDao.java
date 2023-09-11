package com.sailvan.dispatchcenter.db.dao.automated.system;


import com.sailvan.dispatchcenter.common.domain.UserSearch;
import com.sailvan.dispatchcenter.common.domain.system.UserDomain;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * UserDao
 * @date 2021-04
 * @author menghui
 */
@Mapper
public interface UserDao {

    /**
     * getUserList
     * @param userSearch
     * @return
     */
    List<UserDomain> getUserList(UserSearch userSearch);

    /**
     * 根据id和名字查询
     * @param sysUserName
     * @param id
     * @return
     */
    UserDomain getUserByUserName(@Param("sysUserName") String sysUserName, @Param("id") Integer id);

    /**
     *  更新用户状态
     * @param id
     * @param status
     * @return
     */
    int updateUserStatus(@Param("id") Integer id, @Param("status") Integer status);

    /**
     *  更新用户
     * @param user
     * @return
     */
    int updateUser(UserDomain user);

    /**
     *  插入
     * @param user
     * @return
     */
    int insert(UserDomain user);

    /**
     *  查询用户名
     * @param userName
     * @return
     */
    UserDomain findByUserName(@Param("userName") String userName);

    /**
     *  根据id查询
     * @param id
     * @return
     */
    UserDomain selectByPrimaryKey(int id);

    /**
     *  更新密码
     * @param userName
     * @param password
     * @return
     */
    int updatePwd(@Param("userName") String userName, @Param("password") String password);

}