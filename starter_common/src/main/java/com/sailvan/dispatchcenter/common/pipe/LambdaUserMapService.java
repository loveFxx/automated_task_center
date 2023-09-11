package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.LambdaUser;
import com.sailvan.dispatchcenter.common.domain.LambdaUserMap;

import java.util.List;

public interface LambdaUserMapService {

    void insertLambdaUserMap(LambdaUser lambdaUser);


    List<LambdaUserMap> getAllLambdaUserMap();

    boolean updateLambdaUserMap(LambdaUserMap lambdaUserMap);

    LambdaUserMap getMapByLambdaUserMap(LambdaUserMap lambdaUserMap);

    void addLambdaUserMap(LambdaUserMap lambdaUserMap);

    List<LambdaUserMap> getMapListByAccountName(String userName);

    LambdaUserMap getMapById(Integer awsLambdaMapId);

}
