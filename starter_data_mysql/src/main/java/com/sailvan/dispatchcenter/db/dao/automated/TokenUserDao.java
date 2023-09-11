package com.sailvan.dispatchcenter.db.dao.automated;


import com.sailvan.dispatchcenter.common.domain.TokenUser;
import com.sailvan.dispatchcenter.common.domain.UserSearch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * UserDao
 * @date 2021-04
 * @author menghui
 */
@Mapper
public interface TokenUserDao {

    List<TokenUser> getUserList(UserSearch userSearch);

    TokenUser checkTokenUser(TokenUser user);

    int updateUserStatus(@Param("id") Integer id, @Param("status") Integer status);

    int updateByPrimaryKey(TokenUser user);

    int insert(TokenUser user);

    TokenUser findByUserName(@Param("userName") String userName);

    TokenUser selectByPrimaryKey(int id);

    int updatePwd(@Param("userName") String userName, @Param("password") String password);

}