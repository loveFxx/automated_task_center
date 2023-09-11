package com.sailvan.dispatchcenter.common.pipe;


import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

import java.util.List;
import java.util.Map;


/**
 * @author mh
 * @date 2021-04
 */
public interface LambdaUserService {


    public List<LambdaUser> getLambdaUserAll();

    public List<LambdaUser> getLambdaUserByLambdaUser(LambdaUser lambdaUser);

    public LambdaUser getLambdaUserById(Integer id);

    public int updateLambdaUser(LambdaUser lambdaUser);
    public int insertLambdaUser(LambdaUser lambdaUser);

    public PageDataResult getLambdaUserList(LambdaUser machine, Integer pageNum, Integer pageSize);

    LambdaUser getLambdaUserByAccountName(String accountName);

    List<AwsUserRegionFunction> getUserRegionFunctions();

}
