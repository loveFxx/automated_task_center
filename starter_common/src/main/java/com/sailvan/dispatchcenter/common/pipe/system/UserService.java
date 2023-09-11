package com.sailvan.dispatchcenter.common.pipe.system;

import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;
import com.sailvan.dispatchcenter.common.domain.UserSearch;
import com.sailvan.dispatchcenter.common.domain.system.UserDomain;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

/**
 * @Title: AdminUserServiceImpl
 * @Description:
 * @date 2021-04
 * @author menghui
 */
public interface UserService {

    public PageDataResult getUserList(UserSearch userSearch, Integer pageNum, Integer pageSize);

    public ApiResponseDomain addUser(UserDomain user);


    public ApiResponseDomain updateUser(UserDomain user) ;

    public UserDomain getUserById(Integer id) ;


    public ApiResponseDomain delUser(Integer id, Integer status) ;

    public ApiResponseDomain recoverUser(Integer id, Integer status) ;

    public UserDomain findByUserName(String userName);


    public int updatePwd(String userName, String password);
}
