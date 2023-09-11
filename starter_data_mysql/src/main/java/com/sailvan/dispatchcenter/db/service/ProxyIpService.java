package com.sailvan.dispatchcenter.db.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.db.dao.automated.*;
import com.sailvan.dispatchcenter.common.cache.InitAccountCache;
import com.sailvan.dispatchcenter.common.cache.InitPlatformCache;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.CommonUtils;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @program: automated_task_center
 * @description:
 * @author: Wu Xingjian
 * @create: 2021-05-10 15:45
 **/
@Service
public class ProxyIpService implements com.sailvan.dispatchcenter.common.pipe.ProxyIpService {
    private static final Logger logger = LoggerFactory.getLogger(ProxyIpService.class);

    @Autowired
    private ProxyIpDao proxyIpDao;

//    @Autowired
//    ProxyIpShopDao proxyIpShopDao;

    @Autowired
    StoreAccountDao storeAccountDao;

    @Autowired
    AccountProxyDao accountProxyDao;


    @Autowired
    ProxyIpPlatformDao proxyIpPlatformDao;

    @Autowired
    InitAccountCache initAccountCache;

    @Autowired
    InitPlatformCache initPlatformCache;

    @Override
    public void refreshCrawlPlatform(){
        List<ProxyIp> proxyIps = proxyIpDao.getProxyIpAll();
        for (ProxyIp proxyIp : proxyIps) {
            if (proxyIp.getCrawlPlatform().equals("7")){
                continue;
            }
            List<AccountProxy> accountProxies = accountProxyDao.getAccountProxyByProxyIpId(proxyIp.getId());
            List<String> list = new ArrayList<>(Arrays.asList(Constant.PLATFORMS));
            List<Integer> listId = new ArrayList<>();
            for (AccountProxy accountProxy : accountProxies) {
                // 会把之前绑定的无效店铺代理IP设置 平台
//                if (accountProxy.getStatus() != Constant.STATUS_VALID) {
//                    continue;
//                }
                list.remove("Amazon");
                list.remove("AmazonVC");
            }
            for (String s : list) {
                Platform platform = initPlatformCache.getPlatformCacheByName(s);
                if(!listId.contains(platform.getId())){
                    listId.add(platform.getId());
                }
            }
            String result = Joiner.on(",").join(listId);
            proxyIpDao.updateCrawlPlatform(proxyIp.getId(), result);
            logger.info("update success id:{}, crawlPlatform:{}", proxyIp.getId(), result);
        }
    }

    @Override
    public synchronized void refreshProxyIPPlatform() {
        logger.info("refreshProxyIPPlatform");
        List<ProxyIp> proxyIps = proxyIpDao.getProxyIpAll();
        for (ProxyIp proxyIp : proxyIps) {
            boolean isFlag = false;
            Set<String> strings = new HashSet<>();
            List<AccountProxy> accountProxyByProxyIpId = accountProxyDao.getAccountProxyByProxyIpId(proxyIp.getId());
            if(accountProxyByProxyIpId !=null && !accountProxyByProxyIpId.isEmpty()){
                isFlag = true;
                for (AccountProxy accountProxy : accountProxyByProxyIpId) {
                    if(accountProxy.getStatus() == Constant.STATUS_VALID){
                        strings.add(accountProxy.getPlatform());
                    }
                }
            }
            String crawlPlatform = proxyIp.getCrawlPlatform();
            if(!StringUtils.isEmpty(crawlPlatform) && crawlPlatform.equals("7")){
               continue;
            }
            for (String platform : Constant.PLATFORMS) {
                ProxyIpPlatform proxyIpPlatform = new ProxyIpPlatform();
                proxyIpPlatform.setProxyIpId(proxyIp.getId());
                proxyIpPlatform.setPlatform(platform);
                proxyIpPlatform.setStatus(Constant.STATUS_INVALID);
                List<ProxyIpPlatform> proxyIpPlatformByPlatform = proxyIpPlatformDao.getProxyIpPlatformByProxyIpIdPlatform(proxyIp.getId(), platform);
                if(proxyIpPlatformByPlatform ==null || proxyIpPlatformByPlatform.isEmpty()){
                    proxyIpPlatform.setStatus(Constant.STATUS_VALID);
                    if(!strings.contains(platform)){
                        if (!platform.equals("AmazonDaemon")) {
                            proxyIpPlatformDao.insertProxyIpPlatform(proxyIpPlatform);
                        }
                        logger.info("insert account success ProxyIP {},platform {} ",proxyIp.getIp(), platform);
                    }
                }else if((isFlag && strings.contains(platform)) || platform.equals("AmazonDaemon")){
                    proxyIpPlatformDao.deleteProxyIpPlatform(proxyIpPlatform);
                    logger.info("contains refreshProxyIPPlatform delete:{}", JSONObject.toJSONString(proxyIpPlatform));
                }else if(proxyIpPlatformByPlatform.size() > 1){
                    for (int i = 1; i < proxyIpPlatformByPlatform.size() ; i++) {
                        proxyIpPlatformDao.deleteProxyIpPlatform(proxyIpPlatformByPlatform.get(i));
                        logger.info("refreshProxyIPPlatform delete:{}", JSONObject.toJSONString(proxyIpPlatformByPlatform.get(i)));
                    }
                }


            }
        }
    }



    @Override
    public synchronized void refreshProxyIPLargeTaskType() {
        logger.info("refreshProxyIPLargeTaskType");
        List<ProxyIp> proxyIps = proxyIpDao.getProxyIpAll();
        for (ProxyIp proxyIp : proxyIps) {
            List<AccountProxy> accountProxyByProxyIpId = accountProxyDao.getAccountProxyByProxyIpId(proxyIp.getId());
            if(accountProxyByProxyIpId !=null && !accountProxyByProxyIpId.isEmpty()){
                proxyIpDao.updateLargeTaskType(proxyIp.getId(),Constant.LARGE_TASK_TYPE_ACCOUNT_PLATFORM);
            }
        }
    }



    @Override
    public List<ProxyIp> getProxyIpAll() {
        List<ProxyIp> list = proxyIpDao.getProxyIpAll();
        return list;
    }

    @Override
    public PageDataResult getProxyIpList(ProxyIp proxyIp, Integer pageNum, Integer pageSize) {
        if ("1".equals(proxyIp.getPlatformShop())){
            proxyIp.setPlatformShop("Amazon");
        }else {
            proxyIp.setPlatformShop("");
        }

        String ids = getRetainId(proxyIp);
        proxyIp.setIds(ids);

        PageHelper.startPage(pageNum, pageSize);
        List<ProxyIp> proxyIpList = proxyIpDao.getProxyIpByProxyIp(proxyIp);
        PageInfo<ProxyIp> pageInfoOld = new PageInfo<>(proxyIpList);

        List<ProxyIp> proxyIps = new ArrayList<>();
        int insert = 0;
        for (ProxyIp ip : proxyIpList) {

            //1、 设置代理IP店铺信息
//            List<ProxyIpShop> proxyIpShopByProxyIpId = proxyIpShopDao.getProxyIpShopByProxyIpId(String.valueOf(ip.getId()));
            List<AccountProxy> accountProxies = accountProxyDao.getAccountProxyByProxyIpId(ip.getId());
            List<AccountProxy> nodes = accountProxies.stream().filter(p->p.getStatus()==1).collect(Collectors.toList());
            ip.setAccountProxies(nodes);
            String crawlPlatformId = ip.getCrawlPlatform();
            List<String> crawlPlatformName = initPlatformCache.getCrawlPlatformNameByPlatformId(crawlPlatformId, "zh");
            if(crawlPlatformName !=null){
                ip.setCrawlPlatformName(String.join( ",",crawlPlatformName));
            }

            JSONArray crawlPlatformSelect = new JSONArray();
            JSONArray crawlPlatformSelectMap = initPlatformCache.getCrawlPlatformSelectCache();
            crawlPlatformSelect.addAll(crawlPlatformSelectMap);
            for (AccountProxy accountProxy : accountProxies) {
                if (!StringUtils.isEmpty(accountProxy.getPlatform())) {
                    for (int i = 0; i < crawlPlatformSelect.size(); i++) {
                        initPlatformCache.getCrawlPlatformByRemoveAccountPlatform(crawlPlatformSelect, i, accountProxy.getPlatform());
                    }
                }
            }
            ip.setCrawlPlatformSelect(crawlPlatformSelect);


            //2、 设置代理IP对应的平台及其状态
            List<ProxyIpPlatform> proxyIpPlatformByIpId = proxyIpPlatformDao.getProxyIpPlatformByIpId(ip.getId());
            ip.setProxyIpPlatforms(proxyIpPlatformByIpId);

            proxyIps.add(ip);
            if (insert == 0) {
                // 3、为搜索框设置select值
                ip.setAccountSelect(initAccountCache.getAccountMap());
            }
            insert ++;

        }

        PageDataResult pageDataResult = new PageDataResult();
        if (proxyIpList.size() != 0) {
            PageInfo<ProxyIp> pageInfo = new PageInfo<>();
            BeanUtils.copyProperties(pageInfoOld, pageInfo);

            pageDataResult.setList(proxyIps);
            pageDataResult.setTotals((int) pageInfo.getTotal());
            pageDataResult.setPageNum(pageNum);
        }

        return pageDataResult;
    }


    /**
     *   proxyIpShopFlag、proxyIpPlatformFlag 标识的目的为了判断是否是搜索结果为空
     *   还是需要查询所有数据
     * @param proxyIp
     * @return
     */
    private String getRetainId(ProxyIp proxyIp){
        String ids = "";

        List<String> listProxyIpShop = new ArrayList<>();
        boolean proxyIpShopFlag = false;
        if (!StringUtils.isEmpty(proxyIp.getAccount()) || !StringUtils.isEmpty(proxyIp.getPlatformShop())) {
            proxyIpShopFlag = true;
            AccountProxy accountProxy = new AccountProxy();
            accountProxy.setAccount(CommonUtils.searchInValue(proxyIp.getAccount()));
            accountProxy.setPlatform(proxyIp.getPlatformShop());

            List<AccountProxy> proxyIpShopByProxyIpShop = accountProxyDao.getAccountProxyByAccountProxyTwo(accountProxy);
            for (AccountProxy ipShop : proxyIpShopByProxyIpShop) {
                String proxyIpId = String.valueOf(ipShop.getProxyIpId());
                if (!listProxyIpShop.contains(proxyIpId)) {
                    listProxyIpShop.add(proxyIpId);
                }
            }
        }

        List<String> listProxyIpPlatform = new ArrayList<>();
        boolean proxyIpPlatformFlag = false;
        if(proxyIp.getStatus()!=Constant.STATUS_INVALID){
            proxyIpPlatformFlag = true;
            ProxyIpPlatform proxyIpPlatform = new ProxyIpPlatform();
            proxyIpPlatform.setStatus(proxyIp.getStatus());
            List<ProxyIpPlatform> proxyIpPlatformByPlatform = proxyIpPlatformDao.getProxyIpPlatformByPlatform(proxyIpPlatform);
            for (ProxyIpPlatform ipPlatform : proxyIpPlatformByPlatform) {
                String proxyIpId =String.valueOf(ipPlatform.getProxyIpId());
                if (!listProxyIpPlatform.contains(proxyIpId)) {
                    listProxyIpPlatform.add(proxyIpId);
                }
            }
        }

        if( listProxyIpShop.isEmpty() && listProxyIpPlatform.isEmpty()){
            if(proxyIpShopFlag || proxyIpPlatformFlag){
                return "('-1')";
            }
            return "";
        }else if(!listProxyIpShop.isEmpty() && listProxyIpPlatform.isEmpty()){
            if(proxyIpPlatformFlag){
                return "('-1')";
            }
            ids = "('"+ org.apache.commons.lang.StringUtils.join(listProxyIpShop,"','")+"')";
            return ids;
        }else if(listProxyIpShop.isEmpty() && !listProxyIpPlatform.isEmpty()){
            if(proxyIpShopFlag){
                return "('-1')";
            }
            ids = "('"+ org.apache.commons.lang.StringUtils.join(listProxyIpPlatform,"','")+"')";
            return ids;
        }

        listProxyIpShop.retainAll(listProxyIpPlatform);
        ids = "('"+ org.apache.commons.lang.StringUtils.join(listProxyIpShop,"','")+"')";
        return ids;
    }

    @Override
    public int update(ProxyIp proxyIp) {
        int result = proxyIpDao.updateProxyIp(proxyIp);
        return result;
    }

    @Override
    public int updateProxyIpPort(Integer id, Integer port) {
        int result = proxyIpDao.updateProxyIpPort( id, port);
        return result;
    }

    @Override
    public int updateCrawlPlatform(Integer id, String crawlPlatform) {
        int result = proxyIpDao.updateCrawlPlatform( id, crawlPlatform);
        return result;
    }

    @Override
    public int updateLargeTaskType(Integer id, Integer largeTaskType) {
        int result = proxyIpDao.updateLargeTaskType( id, largeTaskType);
        return result;
    }

    @Override
    public int insert(ProxyIp proxyIp) {
        int result = proxyIpDao.insertProxyIp(proxyIp);
        return result;
    }

    @Override
    public List<ProxyIp> select(ProxyIp proxyIp) {
        return proxyIpDao.select(proxyIp);
    }

    @Override
    public ProxyIp getProxyIpByIp(String proxyIp) {
        return proxyIpDao.getProxyIpByIp(proxyIp);
    }


    @Override
    public int delete(Integer id) {
        return proxyIpDao.deleteProxyIpById(id);
    }

    @Override
    public Object getProxyIp(String account, String continents, String platform) {

        //账号和大洲不是空的，需要绑定的代理IP 从账号表获取
        if (!StringUtils.isEmpty(account) && !StringUtils.isEmpty(continents)) {
            StoreAccount storeAccount = new StoreAccount();
            storeAccount.setAccount(account);
            storeAccount.setContinents(continents);
            StoreAccount accountContinentsStatusCache = initAccountCache.getAccountContinentsStatusCache(account, continents);
            if(accountContinentsStatusCache != null){
                logger.info("initAccountCache getProxyIp account:{}, continents:{}, storeAccounts:{} ",account,continents,  accountContinentsStatusCache);
                return accountContinentsStatusCache;
            }
            List<StoreAccount> storeAccounts = storeAccountDao.getStoreAccount(storeAccount);
            if (storeAccounts.size() == 1) {
                logger.info("getProxyIp account:{}, continents:{}, storeAccounts:{} ",account,continents,  storeAccounts.get(0));
                return storeAccounts.get(0);
            } else if (storeAccounts.size() > 1) {
                return new StoreAccount();
            }
            return new StoreAccount();
        } else if (!StringUtils.isEmpty(platform)) {
            List<String> platformsList = Arrays.asList(Constant.PLATFORMS);
            if (!platformsList.contains(platform)) {
                return "platform error: " + platform;
            }
        }
        return "";

    }

    @Override
    public PageDataResult getPlatformProxyIpStatus() {
        PageDataResult pageDataResult = new PageDataResult();

        List<Map> mapBanProxyIpList = proxyIpPlatformDao.getPlatformBanProxyIpNum();

        pageDataResult.setList(mapBanProxyIpList);
        return pageDataResult;
    }

    @Override
    public ProxyIpMonitor getProxyIpNum() {

        ProxyIpMonitor proxyIpMonitor = new ProxyIpMonitor();
        List<ProxyIp> proxyIpAll = proxyIpDao.getProxyIpAll();
        proxyIpMonitor.setProxyIpTotal(proxyIpAll.size());
        int i = 0;
        for (ProxyIp p : proxyIpAll){
            if (p.getValidStatus() == 0){
                i += 1;
            }
        }
        proxyIpMonitor.setIpInvalidNum(i);
        List<String> amazonProxyIps = storeAccountDao.getProxyIp();
        proxyIpMonitor.setAmazonProxyIpTotal(amazonProxyIps.size());
        int amazonipInvalidNum = 0;
        for (String ip : amazonProxyIps){
            ProxyIp proxyIpByIp = proxyIpDao.getProxyIpByIp(ip);
            if (proxyIpByIp!=null && proxyIpByIp.getValidStatus() == 0){
                    amazonipInvalidNum += 1;
            }
        }

        proxyIpMonitor.setAmazonIpInvalidNum(amazonipInvalidNum);

        int totalNum = proxyIpPlatformDao.countByPlatformAndStatus("AmazonDaemon",Constant.STATUS_VALID);
        proxyIpMonitor.setAmazonDaemonTotal(totalNum);
        int invalidNum = proxyIpPlatformDao.countByPlatformAndOpenTimestamp("AmazonDaemon",System.currentTimeMillis());
        proxyIpMonitor.setAmazonDaemonIpInvalidNum(invalidNum);

        return proxyIpMonitor;
    }

    @Override
    public void setProxyIpValidStatus(int validStatus, int id) {
        proxyIpDao.setProxyIpValidStatus(validStatus,id);
    }

    @Override
    public ProxyIp getProxyIpByUniqueKey(String ip, int port){
        return proxyIpDao.getProxyIpByUniqueKey(ip,port);
    }

    @Override
    public ProxyIp findProxyIpById(int id){
        return proxyIpDao.findProxyIpById(id);
    }

    @Override
    public int updateProxyStatus(int validStatus,int id){
        return proxyIpDao.updateProxyStatus(validStatus,id);
    }

    @Override
    public List<ProxyIp> getProxyByPlatform(String platform){
        return proxyIpDao.getProxyByPlatform(platform);
    }

    @Override
    public int batchSetRate(String limitConfig,int unitTime, int maxBannedRate, int delayTime,Object[] ids){
        return proxyIpDao.batchSetRate(limitConfig, unitTime, maxBannedRate, delayTime, ids);
    }

    @Override
    public ProxyIp getFirstProxyByPlatform(String platform){
        return proxyIpDao.getFirstProxyByPlatform(platform);
    }

    @Override
    public int updateValidateTimes(int validateTimes, int isDeleted, int id){
        return proxyIpDao.updateValidateTimes(validateTimes,isDeleted,id);
    }

    @Override
    public List<ProxyIp> listProxyByLargeTaskType(int largeTaskType){
        return proxyIpDao.listProxyByLargeTaskType(largeTaskType);
    }

    @Override
    public List<ProxyIp> listProxyByExpireTime(String expireTime){
        return proxyIpDao.listProxyByExpireTime(expireTime);
    }
}

