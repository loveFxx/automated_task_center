package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.StoreAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wxj
 * @date 21-05-08
 *
 *  店铺帐号表
 */
@Mapper
public interface StoreAccountDao {

    /**
     * 搜索所有
     * @return
     */
    List<StoreAccount> getStoreAccountAll();


    /**
     *  搜索
     * @param storeAccount
     * @return
     */
    List<StoreAccount> getStoreAccount(StoreAccount storeAccount);

    /**
     *  根据IP获取 账号信息
     * @param ip
     * @return
     */
    List<StoreAccount> getStoreAccountByIp(@Param("proxyIp") String ip);

    /**
     *  账号查询
     * @param account
     * @return
     */
    List<StoreAccount> getStoreAccountByAccount(@Param("account") String account);

    /**
     *  查询
     * @param storeAccount
     * @return
     */
    List<StoreAccount> getStoreAccountByAccountPlatformContinents(StoreAccount storeAccount);


    /**
     *  根据用户名查询
     * @param storeAccount
     * @return
     */
    List<StoreAccount> getStoreAccountByUsername(StoreAccount storeAccount);

    /**
     *  查询
     * @param storeAccount
     * @return
     */
    List<StoreAccount> getStoreAccountPlatform(StoreAccount storeAccount);


    /**
     *  根据指定个别参数搜索
     * @param storeAccount
     * @return
     */
    List<StoreAccount> getStoreAccountByStoreAccount(StoreAccount storeAccount);

    /**
     *  根据不同参数查询
     * @param storeAccount
     * @return
     */
    List<StoreAccount> getStoreAccountByParams(StoreAccount storeAccount);

    /**
     *  根据id查询
     * @param id
     * @return
     */
    StoreAccount getStoreAccountById(@Param("id") int id);

    /**
     *  根据指定个别参数更新
     * @param storeAccount
     * @return
     */
    int updateStoreAccount(StoreAccount storeAccount);
    int updateHaveMachine(StoreAccount storeAccount);

    /**
     *  刷新
     * @param storeAccount
     * @return
     */
    int refreshAccount(StoreAccount storeAccount);


    /**
     *  更新店铺对应的代理IP的port
     * @param id
     * @param port
     * @return
     */
    int updateStoreAccountPort(@Param("id") Integer id, @Param("port") Integer port);

    /**
     *  根据id更新
     * @param storeAccount
     * @return
     */
    int updateProxyIpById(StoreAccount storeAccount);

    /**
     *  插入
     * @param storeAccount
     * @return
     */
    int insertStoreAccount(StoreAccount storeAccount);

    /**
     *  批量插入
     * @param storeAccountList
     * @return
     */
    int insertBatch(@Param("storeAccountList") List<StoreAccount> storeAccountList);

    /**
     *  删除
     * @param id
     * @return
     */
    int deleteStoreAccountById(Integer id);

    /**
     * 通过storeAccount的id集合查询storeAccount
     * @param storeAccountIdList
     * @return
     */
    List<StoreAccount> getStoreAccountByStoreAccountIdList(@Param("storeAccountIdList")  String storeAccountIdList);



    /**
     *  通过店铺参数和另外一张店铺站点表的参数搜索
     * @param storeAccount
     * @return
     */
    List<StoreAccount> getStoreAccountByStoreAccountAndSite(@Param("storeAccount") StoreAccount storeAccount,@Param("siteStatus") int siteStatus);




    /**
     * having_machine =0 无账号机关联数
     * @return
     */
    List<StoreAccount> getStoreAccountHavingNoMachine();


    /**
     * having_machine !=1
     * 无可执行账号机数 = 0(无关联机器) + 2(开机不让用) + 3(关机不让用) + 4(关机让用)
     * @return
     */
    List<StoreAccount> getStoreAccountHavingNoAvailableMachine();

    StoreAccount getStoreAccountByAccountContinents(String account, String continents);

    /**
     * 在账号表里获取亚马逊平台代理IP
     * @return
     */
    List<String> getProxyIp();
}
