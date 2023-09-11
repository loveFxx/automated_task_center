package com.sailvan.dispatchcenter.common.pipe;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.domain.PlatformAccount;
import com.sailvan.dispatchcenter.common.domain.StoreAccount;
import com.sailvan.dispatchcenter.common.domain.StoreAccountSites;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

import java.util.List;
import java.util.Map;


/**
 * @author mh
 * @date 2021-04
 */
public interface StoreAccountService {


    public List<StoreAccount> getStoreAccountAll() ;

    public JSONArray getValidAccountContinents();
    public JSONArray getValidAccountSites();

    public List<PlatformAccount> refreshMiNi() ;

    public PageDataResult getStoreAccountList(StoreAccount storeAccount, Integer pageNum, Integer pageSize) ;


    public int update(StoreAccount storeAccount);
    public int updateHaveMachine(StoreAccount storeAccount);
    public int refreshAccount(StoreAccount storeAccount);

    public int updateProxyIpById(StoreAccount storeAccount);

    public int checkIsHaveMachineByAccountContinents(String account, String continent);

    public int insert(StoreAccount storeAccount);


    public List<StoreAccount> getStoreAccountByIp(String ip);

    public List<StoreAccount> getStoreAccountByAccountPlatformContinents(StoreAccount storeAccount);
    public List<StoreAccount> getStoreAccountByParams(StoreAccount storeAccount);

    public List<StoreAccount> getStoreAccountByUsername(StoreAccount storeAccount);
    public List<StoreAccount> getStoreAccountPlatform(StoreAccount storeAccount);

    public StoreAccount getStoreAccountByAccount(String account, String site) ;

    public StoreAccountSites getStoreAccountSitesByAccount(String account, String site) ;
    public StoreAccount getStoreAccountById(int id) ;

    public int insertBatch(List<StoreAccount> storeAccountList);

    public int delete(Integer id);

    public void refreshMiNiMachine();



    public void refreshMiNiAccountProxyIpAccountSite();


    /**
     *  这里是根据token获取的数据，设置代理IP(agent_ip)、线上店铺名字(online_name)、店铺状态(account_status)到StoreAccount
     * @param tokenInfo
     * @param storeAccount
     * @return
     */
    public JSONObject getTokenData(String tokenInfo, StoreAccount storeAccount);


    /**
     *  这里主要是刷新 用户名、密码、代理IP、店铺状态
     * @param storeAccount
     * @param platformAccount
     * @return
     */
    public StoreAccount refreshAccount(StoreAccount storeAccount, PlatformAccount platformAccount);


    /**
     *  刷新站点
     * @param
     * @param storeAccount
     */
    public void refreshAccountSites(StoreAccount storeAccount, JSONObject data);


    /**
     *  需要根据 更新店铺代理IP前后的变化 判断所影响涉及的代理IP及其相关表(代理IP表、代理IP店铺表、代理IP平台禁用表)
     *  需要重置账号表的代理IP和端口的缓存
     * @param sourceStoreAccount
     * @param descStoreAccount
     */
    public void updateStoreAndProxyIp(StoreAccount sourceStoreAccount, StoreAccount descStoreAccount);

    /**
     * 根据店铺拥有站点的站点状态查询
     * @param pageNum
     * @param pageSize
     * @param accountSiteStatus
     * @return
     */
    public PageDataResult getStoreAccountBySiteStatus(int pageNum,int pageSize,StoreAccount storeAccount,Integer accountSiteStatus);


    StoreAccount getStoreAccountByAccountContinents(String account, String continents);
}
