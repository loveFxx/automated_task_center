package com.sailvan.dispatchcenter.db.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.pipe.LambdaUserMapService;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.db.dao.automated.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * @author mh
 * @date 2021-04
 */
@Service
public class LambdaUserService implements com.sailvan.dispatchcenter.common.pipe.LambdaUserService {

    @Autowired
    private LambdaUserDao lambdaUserDao;

    @Autowired
    LambdaUserService lambdaUserService;

    @Autowired
    private RegionDao regionDao;

    @Autowired
    AwsLambdaFunctionDao awsLambdaFunctionDao;

    @Autowired
    LambdaUserMapService lambdaUserMapService;

    @Autowired
    LambdaUserMapDao lambdaUserMapDao;



    @Override
    public List<LambdaUser> getLambdaUserAll() {
        return lambdaUserDao.getLambdaUserAll();
    }

    @Override
    public LambdaUser getLambdaUserById(Integer id) {
        return lambdaUserDao.getLambdaUserById(id);
    }

    @Override
    public List<LambdaUser> getLambdaUserByLambdaUser(LambdaUser lambdaUser) {
        return lambdaUserDao.getLambdaUserByLambdaUser(lambdaUser);
    }

    @Override
    public int updateLambdaUser(LambdaUser lambdaUser) {
        return lambdaUserDao.updateLambdaUser(lambdaUser);
    }

    @Override
    public int insertLambdaUser(LambdaUser lambdaUser) {
        return lambdaUserDao.insertLambdaUser(lambdaUser);
    }

    @Override
    public PageDataResult getLambdaUserList(LambdaUser lambdaUser, Integer pageNum, Integer pageSize)  {
        PageHelper.startPage(pageNum, pageSize);


        List<LambdaUserMap> lambdaUserMapList = lambdaUserMapService.getAllLambdaUserMap();
        for (LambdaUserMap userMap : lambdaUserMapList){
            LambdaUser lambdaUserById = lambdaUserService.getLambdaUserById(userMap.getLambdaAccountId());
            userMap.setAccessKey(lambdaUserById.getAccessKey());
            userMap.setAccessSecret(lambdaUserById.getAccessSecret());
            userMap.setLambdaAccountId(lambdaUserById.getId());
        }
        PageInfo<LambdaUserMap> pageInfoOld = new PageInfo<>(lambdaUserMapList);

        PageDataResult pageDataResult = new PageDataResult();
        if (lambdaUserMapList.size() != 0) {
            PageInfo<LambdaUser> pageInfoNew = new PageInfo<>();
            // 处理 只能查询pageSize的bug
            BeanUtils.copyProperties(pageInfoOld, pageInfoNew);
            pageDataResult.setList(lambdaUserMapList);
            pageDataResult.setTotals((int) pageInfoNew.getTotal());
            pageDataResult.setPageNum(pageNum);
        }
        return pageDataResult;
    }

    @Override
    public LambdaUser getLambdaUserByAccountName(String accountName) {

        return lambdaUserDao.getLambdaUserByAccountName(accountName);
    }

    @Override
    public List<AwsUserRegionFunction> getUserRegionFunctions() {

        List<AwsUserRegionFunction> userRegionFunctionList = new ArrayList<>();
        List<LambdaUser> lambdaUserAll = lambdaUserDao.getLambdaUserAll();

        for (LambdaUser lambdaUser : lambdaUserAll){
            List<AwsUserRegionFunction> urfSonList = new ArrayList<>();
            AwsUserRegionFunction aurf = new AwsUserRegionFunction();
            List<LambdaUserMap> mapListByAccountName
                    = lambdaUserMapService.getMapListByAccountName(lambdaUser.getAccountName());
            if (!mapListByAccountName.isEmpty()){
                for (LambdaUserMap userMap : mapListByAccountName){
                    String region = userMap.getRegion();
                    String functionName = userMap.getFunctionName();
                    AwsUserRegionFunction aurfSon = new AwsUserRegionFunction();
                    aurfSon.setName(region+"_"+functionName);
                    aurfSon.setValue(region+"_"+functionName);
                    urfSonList.add(aurfSon);
                }
                aurf.setName(lambdaUser.getAccountName());
                aurf.setValue(lambdaUser.getAccountName());
                aurf.setChildren(urfSonList);
                userRegionFunctionList.add(aurf);
            }

        }


        return userRegionFunctionList;
    }


}
