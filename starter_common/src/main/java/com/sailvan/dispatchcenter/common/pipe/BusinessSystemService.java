package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.BusinessSystem;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

import java.util.List;


/**
 * @author mh
 * @date 2021-06
 */
public interface BusinessSystemService {


    public abstract List<BusinessSystem> getBusinessSystemAll();

    public PageDataResult getBusinessSystemList(BusinessSystem businessSystem, Integer pageNum, Integer pageSize) ;


    public int update(BusinessSystem businessSystem);
    public int updateBusinessSystemInvokeTimesMonthUsed(BusinessSystem businessSystem);

    public int updateLastLogin(BusinessSystem businessSystem);

    public BusinessSystem checkBusiness(String systemName);

    BusinessSystem checkBusinessBySecret(BusinessSystem businessSystem);

    public int insert(BusinessSystem businessSystem);


    public int delete(Integer id);

    public List<BusinessSystem> getMachinePlatform(BusinessSystem businessSystem) ;


    /**
     * 创建Token，这里要根据当前时间获取密钥，并且生成Token,更新用户的最后登入时间
     * @param businessSystem
     * @return Transactional
     */
    public String createBusinessToken(BusinessSystem businessSystem) ;

    public BusinessSystem findBySystemName(String systemName);

    public BusinessSystem findSystemById(int id);
}
