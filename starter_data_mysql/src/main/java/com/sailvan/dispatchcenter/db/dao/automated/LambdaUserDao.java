package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mh
 * @date 22-03
 *
 *  lambda
 */
@Mapper
public interface LambdaUserDao {



    public List<LambdaUser> getLambdaUserAll();

    public List<LambdaUser> getLambdaUserByLambdaUser(LambdaUser lambdaUser);

    public LambdaUser getLambdaUserById(Integer id);

    public int updateLambdaUser(LambdaUser lambdaUser);
    public int insertLambdaUser(LambdaUser lambdaUser);

    public PageDataResult getLambdaUserList(LambdaUser machine, Integer pageNum, Integer pageSize);


    LambdaUser getLambdaUserByAccessKey(String accessKey);

    LambdaUser getLambdaUserByAccountName(String accountName);
}
