package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.domain.AwsLambdaFunction;
import com.sailvan.dispatchcenter.common.domain.AwsRegion;
import com.sailvan.dispatchcenter.common.domain.LambdaUser;
import com.sailvan.dispatchcenter.common.domain.LambdaUserMap;
import com.sailvan.dispatchcenter.common.pipe.AwsLambdaFunctionService;
import com.sailvan.dispatchcenter.common.pipe.LambdaUserService;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.db.dao.automated.AwsLambdaFunctionDao;
import com.sailvan.dispatchcenter.db.dao.automated.LambdaUserMapDao;
import com.sailvan.dispatchcenter.db.dao.automated.RegionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LambdaUserMapService implements com.sailvan.dispatchcenter.common.pipe.LambdaUserMapService {

    @Autowired
    RegionService regionService;

    @Autowired
    AwsLambdaFunctionDao awsLambdaFunctionDao;

    @Autowired
    LambdaUserMapDao lambdaUserMapDao;

    @Autowired
    LambdaUserService lambdaUserService;

    @Autowired
    AwsLambdaFunctionService awsFunctionService;

    @Override
    public void insertLambdaUserMap(LambdaUser lambdaUser) {

        LambdaUserMap lambdaUserMap = new LambdaUserMap();
        String regionName = lambdaUser.getRegion();
        AwsRegion awsRegion = regionService.getRegionByRegionName(regionName);
        String lambdaFunction = lambdaUser.getLambdaFunction();
        AwsLambdaFunction function = awsLambdaFunctionDao.getFunctionByFunctionName(lambdaFunction);
        lambdaUserMap.setRegionId(awsRegion.getId());
        lambdaUserMap.setRegion(regionName);
        lambdaUserMap.setFunctionId(function.getId());
        lambdaUserMap.setFunctionName(lambdaFunction);
        lambdaUserMap.setLambdaAccountId(lambdaUser.getId());
        lambdaUserMap.setAccountName(lambdaUser.getAccountName());
        lambdaUserMap.setCreatedAt(DateUtils.getCurrentDate());
        lambdaUserMap.setUpdatedAt(DateUtils.getCurrentDate());
        lambdaUserMapDao.insertLambdaUserMap(lambdaUserMap);
    }


    @Override
    public List<LambdaUserMap> getAllLambdaUserMap() {
        return lambdaUserMapDao.getAllLambdaUserMap();
    }


    @Override
    public boolean updateLambdaUserMap(LambdaUserMap lambdaUserMap) {
        LambdaUserMap userMapByUserMap = lambdaUserMapDao.getMapByLambdaUserMap(lambdaUserMap);
            if (userMapByUserMap == null){
                AwsRegion region = regionService.getRegionByRegionName(lambdaUserMap.getRegion());
                AwsLambdaFunction functionByName = awsFunctionService.getFunctionByFunctionName(lambdaUserMap.getFunctionName());
                LambdaUserMap userMap = new LambdaUserMap();
                userMap.setId(lambdaUserMap.getId());
                userMap.setLambdaAccountId(lambdaUserMap.getLambdaAccountId());
                userMap.setRegion(lambdaUserMap.getRegion());
                userMap.setRegionId(region.getId());
                userMap.setFunctionName(lambdaUserMap.getFunctionName());
                userMap.setFunctionId(functionByName.getId());
                lambdaUserMapDao.updateLambdaUserMap(userMap);
                return true;
        }
        return false;
    }

    @Override
    public LambdaUserMap getMapByLambdaUserMap(LambdaUserMap lambdaUserMap) {

        return lambdaUserMapDao.getMapByLambdaUserMap(lambdaUserMap);
    }

    @Override
    public void addLambdaUserMap(LambdaUserMap lambdaUserMap) {
        lambdaUserMapDao.insertLambdaUserMap(lambdaUserMap);
    }

    @Override
    public List<LambdaUserMap> getMapListByAccountName(String accountName) {
        return lambdaUserMapDao.getMapListByAccountName(accountName);
    }

    @Override
    public LambdaUserMap getMapById(Integer awsLambdaMapId) {

        return lambdaUserMapDao.getMapById(awsLambdaMapId);
    }

}
