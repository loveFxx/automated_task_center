package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.BusinessSystem;
import com.sailvan.dispatchcenter.common.cache.InitTaskCache;
import com.sailvan.dispatchcenter.common.domain.BusinessSystemTask;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.WebTokenUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sailvan.dispatchcenter.db.dao.automated.BusinessSystemDao;
import com.sailvan.dispatchcenter.db.dao.automated.BusinessSystemTaskDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


/**
 * @author mh
 * @date 2021-06
 */
@Service
public class BusinessSystemService implements com.sailvan.dispatchcenter.common.pipe.BusinessSystemService {

    private static Logger logger = LoggerFactory.getLogger(BusinessSystemService.class);

    @Autowired
    private BusinessSystemDao businessSystemDao;

    @Autowired
    InitTaskCache initTaskCache;

    @Autowired
    BusinessSystemTaskDao businessSystemTaskDao;

    @Override
    public List<BusinessSystem> getBusinessSystemAll() {
        List<BusinessSystem> list = businessSystemDao.getBusinessSystemAll();
        return list;
    }

    @Override
    public PageDataResult getBusinessSystemList(BusinessSystem businessSystem, Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);

        List<BusinessSystem> businessSystemList = businessSystemDao.getBusinessSystemByBusinessSystem(businessSystem);
        int insert =0;
        for (BusinessSystem businessSystemInfo : businessSystemList) {

            if(insert==0){
                // 为第一个机器对象 设置搜索框的值
                businessSystemInfo.setTaskTypeSelect(initTaskCache.getTaskIdMapCache());
            }
            List<BusinessSystemTask> businessSystemTaskBySystemIdAndStatus = businessSystemTaskDao.getBusinessSystemTaskBySystemIdAndStatus(businessSystemInfo.getId(), Constant.STATUS_VALID);
            List<String> list = new ArrayList<>();
            for (BusinessSystemTask systemTaskBySystemIdAndStatus : businessSystemTaskBySystemIdAndStatus) {
                String s = String.valueOf(systemTaskBySystemIdAndStatus.getTaskId());
                if(!list.contains(s)){
                    list.add(s);
                }
            }
            businessSystemInfo.setTaskTypeName(String.join(",", list));
            insert++;
        }

        PageDataResult pageDataResult = new PageDataResult();
        if(businessSystemList.size() != 0){
            PageInfo<BusinessSystem> pageInfoNew = new PageInfo<>(businessSystemList);
            pageDataResult.setList(businessSystemList);
            pageDataResult.setTotals((int) pageInfoNew.getTotal());
            pageDataResult.setPageNum(pageNum);
        }

        return pageDataResult;
    }


    @Override
    public int update(BusinessSystem businessSystem){
        int result =  businessSystemDao.updateBusinessSystem(businessSystem);
        return result;
    }

    @Override
    public int updateBusinessSystemInvokeTimesMonthUsed(BusinessSystem businessSystem){
        int result =  businessSystemDao.updateBusinessSystemInvokeTimesMonthUsed(businessSystem);
        return result;
    }

    @Override
    public int updateLastLogin(BusinessSystem businessSystem){
        int result =  businessSystemDao.updateLastLogin(businessSystem);
        return result;
    }

    @Override
    public BusinessSystem checkBusiness(String systemName){
        return businessSystemDao.checkBusiness(systemName);
    }

    @Override
    public BusinessSystem checkBusinessBySecret(BusinessSystem businessSystem){
        return businessSystemDao.checkBusinessBySecret(businessSystem);
    }

    @Override
    public int insert(BusinessSystem businessSystem){
        int result = businessSystemDao.insertBusinessSystem(businessSystem);
        return result;
    }


    @Override
    public int delete(Integer id){
        return businessSystemDao.deleteBusinessSystemById(id);
    }

    @Override
    public List<BusinessSystem> getMachinePlatform(BusinessSystem businessSystem) {
        List<BusinessSystem> list = businessSystemDao.getBusinessSystemByBusinessSystem(businessSystem);
        return list;
    }


    /**
     * 创建Token，这里要根据当前时间获取密钥，并且生成Token,更新用户的最后登入时间
     * @param businessSystem
     * @return Transactional
     */
    @Override
    public String createBusinessToken(BusinessSystem businessSystem) {
        Instant now = Instant.now();
        String secretKey = WebTokenUtil.genSecretKey(now);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String token = WebTokenUtil.create(secretKey, String.valueOf(businessSystem.getId()), now, Constant.TOKEN_VALIDITY_TIME);
        businessSystem.setLastLogin(df.format(LocalDateTime.ofInstant(now, ZoneId.of("+08:00"))));
        updateLastLogin(businessSystem);
        return token;
    }

    @Override
    public BusinessSystem findBySystemName(String systemName){
        return businessSystemDao.findBySystemName(systemName);
    }

    @Override
    public BusinessSystem findSystemById(int id){
        return businessSystemDao.findSystemById(id);
    }
}
