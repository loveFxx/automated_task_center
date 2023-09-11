package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.LambdaUserMap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LambdaUserMapDao {


    void insertLambdaUserMap(LambdaUserMap lambdaUserMap);

    List<LambdaUserMap> getAllLambdaUserMap();

    void updateLambdaUserMap(LambdaUserMap userMap);

    LambdaUserMap getMapByLambdaUserMap(LambdaUserMap lambdaUserMap);

    List<LambdaUserMap> getMapListByAccountName(String accountName);

    LambdaUserMap getMapById(@Param("mapId") Integer mapId);
}
