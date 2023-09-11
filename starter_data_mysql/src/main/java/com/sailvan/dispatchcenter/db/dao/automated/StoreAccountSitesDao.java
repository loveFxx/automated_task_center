package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.StoreAccountSites;
/*
import org.apache.ibatis.annotations.MapKey;
*/
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author meng
 * @date 21-06
 *
 *  店铺帐号对应站点表
 */
@Mapper
public interface StoreAccountSitesDao {

    /**
     * 搜索所有
     * @return
     */
    List<StoreAccountSites> getStoreAccountSitesAll();


    /**
     *  查询
     * @param storeAccount
     * @return
     */
    List<StoreAccountSites> getStoreAccountSites(StoreAccountSites storeAccount);
    /**
     *  根据指定个别参数搜索
     * @return
     */
    StoreAccountSites getStoreAccountSitesById(String id);
    List<StoreAccountSites> getStoreAccountSitesByAccountId(StoreAccountSites storeAccount);

    List<StoreAccountSites> getStoreAccountSitesByAccountSite(StoreAccountSites storeAccount);
    List<StoreAccountSites> getStoreAccountSitesByAccountContinents(StoreAccountSites storeAccount);

    /**
     *  根据指定个别参数更新
     * @param storeAccount
     * @return
     */
    int updateStoreAccountSites(StoreAccountSites storeAccount);
    int updateStoreAccountSitesByClient(StoreAccountSites storeAccount);

    /**
     *  更新机器验证状态
     * @param storeAccount
     * @return
     */
    int updateMachineStatus(StoreAccountSites storeAccount);


    /**
     *  插入
     * @param storeAccount
     * @return
     */
    int insertStoreAccountSites(StoreAccountSites storeAccount);


    /**
     *  更新禁用状态
     * @param id
     * @return
     */
    int updateStatusInvalid(@Param("id") Integer id);

    /**
     *  删除
     * @param id
     * @return
     */
    int deleteStoreAccountSitesById(Integer id);


    /**
     * 站点 join 店铺后 通过statusPerson和havingMachine分组
     * not1 无可用机器
     * is0 无关联机器
     * @return
     */
    @MapKey("id")
    Map<Integer,Map<String,Integer>> getSiteJoinAccountHavingMachine();

    /**
     * 和getSiteByStatusPersonAndHavingMachine类似
     * @return
     */
    @MapKey("status_machine")
    Map<Integer,Map<String,Object>> getSiteByStatusMachineAndHavingMachine();



}
