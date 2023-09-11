package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.BusinessSystem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author mh
 * @date 21-06
 */
@Mapper
public interface BusinessSystemDao {

    /**
     * 搜索所有
     * @return
     */
    List<BusinessSystem> getBusinessSystemAll();

    /**
     *  注册业务系统，用来获取token
     * @param systemName
     * @return
     */
    BusinessSystem checkBusiness(@Param("systemName") String systemName);

    BusinessSystem checkBusinessBySecret(BusinessSystem businessSystem);


    /**
     *  根据Id获取
     * @param id
     * @return
     */
    BusinessSystem getBusinessById(@Param("id") String id);

    /**
     *  根据指定个别参数搜索
     * @param businessSystem
     * @return
     */
    List<BusinessSystem> getBusinessSystemByBusinessSystem(BusinessSystem businessSystem);


    /**
     *  更新
     * @param businessSystem
     * @return
     */
    int updateBusinessSystem(BusinessSystem businessSystem);

    /**
     *  更新已经使用次数
     * @param businessSystem
     * @return
     */
    int updateBusinessSystemInvokeTimesMonthUsed(BusinessSystem businessSystem);

    /**
     *  更新登录时间
     * @param businessSystem
     * @return
     */
    int updateLastLogin(BusinessSystem businessSystem);

    /**
     *  插入
     * @param businessSystem
     * @return
     */
    int insertBusinessSystem(BusinessSystem businessSystem);


    /**
     *  删除
     * @param id
     * @return
     */
    int deleteBusinessSystemById(Integer id);

    /**
     * 根据系统名获取信息
     * @param systemName
     * @return
     */
    BusinessSystem findBySystemName(String systemName);

    BusinessSystem findSystemById(int id);
}
