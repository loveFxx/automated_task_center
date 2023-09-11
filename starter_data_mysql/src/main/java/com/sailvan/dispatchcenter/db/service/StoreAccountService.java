package com.sailvan.dispatchcenter.db.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.AccountAssociateMachine;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.util.CommonUtils;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.db.dao.mini.PlatformAccountDao;
import com.sailvan.dispatchcenter.common.cache.InitAccountCache;
import com.sailvan.dispatchcenter.common.cache.InitMachineCache;
import com.sailvan.dispatchcenter.common.cache.InitTaskCache;
import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.AesUtils;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sailvan.dispatchcenter.db.dao.automated.*;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.sailvan.dispatchcenter.common.constant.Constant.PROXY_IP_ACCOUNT_CONTINENTS_PREFIX;

/**
 * @author mh
 * @date 2021-04
 */
@Service
public class StoreAccountService implements com.sailvan.dispatchcenter.common.pipe.StoreAccountService {
    private static final Logger logger = LoggerFactory.getLogger(StoreAccountService.class);

    @Autowired
    private StoreAccountDao storeAccountDao;

    @Autowired
    private PlatformAccountDao platformAccountDao;

    @Autowired
    MachineDao machineDao;


//    @Autowired
//    MachineTaskTypeDao machineTaskTypeDao;

    @Autowired
    MachineWorkTypeDao machineWorkTypeDao;

    @Autowired
    MachineWorkTypeTaskDao machineWorkTypeTaskDao;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ProxyIpDao proxyIpDao;

    @Autowired
    StoreAccountSitesDao storeAccountSitesDao;

    @Autowired
    ProxyIpShopDao proxyIpShopDao;

    @Autowired
    AccountProxyDao accountProxyDao;

    @Autowired
    ProxyIpPlatformDao proxyIpPlatformDao;

    @Autowired
    PlatformDao platformDao;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    InitAccountCache initAccountCache;

    @Autowired
    InitMachineCache initMachineCache;

    @Autowired
    InitTaskCache initTaskCache;


    @Override
    public List<StoreAccount> getStoreAccountAll() {
        List<StoreAccount> list = storeAccountDao.getStoreAccountAll();
        return list;
    }

    @Override
    public JSONArray getValidAccountContinents() {
        JSONArray SystemNameMapTmp = new JSONArray();
        List<StoreAccount> lists = storeAccountDao.getStoreAccountAll();
        for (StoreAccount storeAccount : lists) {
            JSONObject jsonObject = new JSONObject();
            if (StringUtils.isEmpty(storeAccount.getAccount()) || StringUtils.isEmpty(storeAccount.getContinents())) {
                continue;
            }
            StoreAccountSites storeAccountSites = new StoreAccountSites();
            storeAccountSites.setAccountId(storeAccount.getId());
            List<StoreAccountSites> storeAccountSitesByAccountId = storeAccountSitesDao.getStoreAccountSitesByAccountId(storeAccountSites);
            if (storeAccountSitesByAccountId == null || storeAccountSitesByAccountId.isEmpty()) {
                continue;
            }
            boolean insert = false;
            for (StoreAccountSites accountSites : storeAccountSitesByAccountId) {
                if (accountSites.getStatus() == Constant.STATUS_VALID) {
                    insert = true;
                    break;
                }
            }
            String key = storeAccount.getAccount() + "_" + storeAccount.getContinents();
            jsonObject.put("name", key);
            jsonObject.put("value", key);
            if (insert && !SystemNameMapTmp.contains(jsonObject)) {
                SystemNameMapTmp.add(jsonObject);
            }
        }
        return SystemNameMapTmp;
    }


    @Override
    public JSONArray getValidAccountSites() {
        JSONArray SystemNameMapTmp = new JSONArray();
        List<StoreAccount> lists = storeAccountDao.getStoreAccountAll();
        for (StoreAccount storeAccount : lists) {
            if (StringUtils.isEmpty(storeAccount.getAccount()) || StringUtils.isEmpty(storeAccount.getContinents())) {
                continue;
            }
            JSONObject jsonObject = new JSONObject();
            StoreAccountSites storeAccountSites = new StoreAccountSites();
            storeAccountSites.setAccountId(storeAccount.getId());
            List<StoreAccountSites> storeAccountSitesByAccountId = storeAccountSitesDao.getStoreAccountSitesByAccountId(storeAccountSites);
            if (storeAccountSitesByAccountId == null || storeAccountSitesByAccountId.isEmpty()) {
                continue;
            }
            for (StoreAccountSites accountSites : storeAccountSitesByAccountId) {
                if (accountSites.getStatus() == Constant.STATUS_VALID) {
                    String key = storeAccount.getAccount() + "_" + accountSites.getSite();
                    jsonObject.put("name", key);
                    jsonObject.put("value", key);
                    if (!SystemNameMapTmp.contains(jsonObject)) {
                        SystemNameMapTmp.add(jsonObject);
                    }
                }
            }
        }
        return SystemNameMapTmp;
    }

    @Override
    public List<PlatformAccount> refreshMiNi() {
        List<String> platformList = new ArrayList<>();
        platformList.add("Amazon");
        platformList.add("AmazonVC");
        List<PlatformAccount> list = platformAccountDao.getPlatformAccountAll(platformList);
        return list;
    }

    @Override
    public PageDataResult getStoreAccountList(StoreAccount storeAccount, Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);

        //拼接 account in查询
        if (!StringUtils.isEmpty(storeAccount.getAccount())) {
            String account = storeAccount.getAccount();
            account = account.replaceAll(",", "' , '");
            storeAccount.setAccount("( '" + account + "' )");
        }

        List<StoreAccount> storeAccountList = storeAccountDao.getStoreAccountByStoreAccount(storeAccount);
        int insert = 0;
        for (StoreAccount account : storeAccountList) {
            List<String> machineIp = new ArrayList<>();
            List<Integer> machinestatus = new ArrayList<>();
            List<MachineWorkType> machineWorkType = machineWorkTypeDao.getMachineByAccount(account.getAccount(), account.getContinents());

            account.setMachineWorkTypeList(machineWorkType);

        }
        for (StoreAccount account : storeAccountList) {

            if (insert == 0) {
                account.setAccountSelect(initAccountCache.getAccountMap());
                break;
            }
        }

        PageDataResult pageDataResult = new PageDataResult();
        if (storeAccountList.size() != 0) {
            PageInfo<StoreAccount> pageInfo = new PageInfo<>(storeAccountList);
            pageDataResult.setList(storeAccountList);
            pageDataResult.setTotals((int) pageInfo.getTotal());
            pageDataResult.setPageNum(pageNum);
        }


        return pageDataResult;
    }

    @Override
    public int updateHaveMachine(StoreAccount storeAccount) {
        int result = storeAccountDao.updateHaveMachine(storeAccount);
        return result;
    }

    @Override
    public int update(StoreAccount storeAccount) {
        int result = storeAccountDao.updateStoreAccount(storeAccount);
        return result;
    }

    @Override
    public int refreshAccount(StoreAccount storeAccount) {
        return storeAccountDao.refreshAccount(storeAccount);
    }

    @Override
    public int updateProxyIpById(StoreAccount storeAccount) {
        return storeAccountDao.updateProxyIpById(storeAccount);
    }

    @Override
    public int insert(StoreAccount storeAccount) {
        return storeAccountDao.insertStoreAccount(storeAccount);
    }


    @Override
    public List<StoreAccount> getStoreAccountByIp(String ip) {
        return storeAccountDao.getStoreAccountByIp(ip);
    }

    @Override
    public List<StoreAccount> getStoreAccountByAccountPlatformContinents(StoreAccount storeAccount) {
        return storeAccountDao.getStoreAccountByAccountPlatformContinents(storeAccount);
    }

    @Override
    public List<StoreAccount> getStoreAccountByParams(StoreAccount storeAccount) {
        return storeAccountDao.getStoreAccountByParams(storeAccount);
    }

    @Override
    public List<StoreAccount> getStoreAccountByUsername(StoreAccount storeAccount) {
        return storeAccountDao.getStoreAccountByUsername(storeAccount);
    }

    @Override
    public List<StoreAccount> getStoreAccountPlatform(StoreAccount storeAccount) {
        return storeAccountDao.getStoreAccountPlatform(storeAccount);
    }

    @Override
    public StoreAccount getStoreAccountByAccount(String account, String site) {
        List<StoreAccount> StoreAccounts = storeAccountDao.getStoreAccountByAccount(account);
        if (StoreAccounts == null || StoreAccounts.isEmpty()) {
            return null;
        }
        for (StoreAccount storeAccount : StoreAccounts) {
            StoreAccountSites storeAccountSites = new StoreAccountSites();
            storeAccountSites.setAccountId(storeAccount.getId());
            List<StoreAccountSites> sites = storeAccountSitesDao.getStoreAccountSitesByAccountId(storeAccountSites);
            if (sites == null || sites.isEmpty()) {
                continue;
            }
            for (StoreAccountSites accountSites : sites) {
//                if (accountSites.getStatus() != Constant.STATUS_VALID) {
//                    continue;
//                }
                if (accountSites.getSite().equals(site)) {
                    return storeAccount;
                }
            }
        }
        return null;
    }

    @Override
    public StoreAccount getStoreAccountById(int id){
        return storeAccountDao.getStoreAccountById(id);
    }


    @Override
    public StoreAccountSites getStoreAccountSitesByAccount(String account, String site) {
        List<StoreAccount> StoreAccounts = storeAccountDao.getStoreAccountByAccount(account);
        if (StoreAccounts == null || StoreAccounts.isEmpty()) {
            return null;
        }
        for (StoreAccount storeAccount : StoreAccounts) {
            StoreAccountSites storeAccountSites = new StoreAccountSites();
            storeAccountSites.setAccountId(storeAccount.getId());
            List<StoreAccountSites> sites = storeAccountSitesDao.getStoreAccountSitesByAccountId(storeAccountSites);
            if (sites == null || sites.isEmpty()) {
                continue;
            }
            for (StoreAccountSites accountSites : sites) {
                if (accountSites.getSite().equals(site)) {
                    return accountSites;
                }
            }
        }
        return null;
    }

    @Override
    public int insertBatch(List<StoreAccount> storeAccountList) {
        return storeAccountDao.insertBatch(storeAccountList);
    }

    @Override
    public int delete(Integer id) {
        return storeAccountDao.deleteStoreAccountById(id);
    }

    /**
     * 从mini获取并刷新机器(及其相关的机器店铺、机器执行类型)
     */
    @Override
    public void refreshMiNiMachine() {
        List<PlatformAccount> platformAccounts = refreshMiNi();
        Set<Integer> set = new HashSet<>();
        for (PlatformAccount platformAccount : platformAccounts) {
            //192.168.28.29_48:4D:7E:BE:28:89,10.202.1.24_FA:16:3E:77:C4:50,10.202.1.27_FA:16:3E:6B:24:F9,10.202.2.108_FA:16:3E:B9:B7:B0,10.202.2.109_FA:16:3E:04:F7:80,10.30.66.5_98:40:BB:42:62:AB
            String ipMacs = platformAccount.getIpMac();
            String comma = ",";
            String underScore = "_";
            if (ipMacs != null) {
                if (ipMacs.contains(comma)) {
                    for (String ipMac : ipMacs.split(comma)) {
                        Machine machine = new Machine();
                        if (ipMac.split(underScore).length == 2) {
                            String ip = ipMac.split(underScore)[0];
                            String mac = ipMac.split(underScore)[1];
                            machine.setIp(ip);
                            machine.setMac(mac);
                        } else {
                            machine.setIp(ipMac);
                        }
                        setMachineType(machine);
                        insert2(platformAccount, machine, set);
                    }
                } else {
                    Machine machine = new Machine();
                    if (ipMacs.split(underScore).length == 2) {
                        String ip = ipMacs.split(underScore)[0];
                        String mac = ipMacs.split(underScore)[1];
                        machine.setIp(ip);
                        machine.setMac(mac);
                    } else {
                        machine.setIp(ipMacs);
                    }
                    setMachineType(machine);
                    insert2(platformAccount, machine, set);
                }

            } else {
                String shopName = platformAccount.getShopName();
                if (!StringUtils.isEmpty(shopName)) {
                    // super用户
                    MachineWorkType machineWorkType = new MachineWorkType();
                    machineWorkType.setMachineId(-1);
                    machineWorkType.setPlatform(platformAccount.getPlatform());
                    machineWorkType.setAccount(shopName);
                    machineWorkType.setContinents(Constant.MAP_CONTINENTS.get(platformAccount.getSite()));


//                    List<MachineTaskType> machineShopByMachineShop = machineTaskTypeDao.getMachineTaskTypeByMachineTaskType(shop);
                    List<MachineWorkType> machineShopByMachineShop = machineWorkTypeDao.getMachineWorkTypeByMachineWorkType(machineWorkType);
                    if (machineShopByMachineShop.isEmpty()) {
                        Machine machine = new Machine();
                        machine.setMachineType(0);
                        insert2(platformAccount, machine, set);
                    }
                }
            }
            logger.info("success platformAccount {}", platformAccount);
        }
        initMachineCache.updateMachineCacheMap(null);
    }

    private void setMachineType(Machine machine) {
        String ip = machine.getIp();
        if (ip.startsWith(Constant.INTRANET_VPS_PREFIX)) {
            machine.setMachineType(Constant.MACHINE_TYPE_INTRANET_VPS);
        } else if (ip.startsWith(Constant.CHONGQING_ACCOUNT_MACHINE_0_PREFIX)) {
            machine.setMachineType(Constant.MACHINE_TYPE_CHONGQING_ACCOUNT_MACHINE);
        } else if (ip.startsWith(Constant.CHONGQING_ACCOUNT_MACHINE_1_PREFIX)) {
            machine.setMachineType(Constant.MACHINE_TYPE_CHONGQING_ACCOUNT_MACHINE);
        } else {
            machine.setMachineType(Constant.MACHINE_TYPE_ACCOUNT_MACHINE);
        }

    }


    @SneakyThrows
    private void insert2(PlatformAccount platformAccount, Machine machine, Set<Integer> set) {

        Machine select = null;
        if (!StringUtils.isEmpty(machine.getIp())) {
            select = machineDao.select(machine);
        }
        int machineId = 0;
        if (select == null) {
            machine.setStatus(Constant.STATUS_INVALID);
            machine.setCreatedTime(DateUtils.getCurrentDate());
            machine.setMachineType(Constant.MACHINE_TYPE_ACCOUNT_MACHINE);
            machineDao.insertMachine(machine);
            machineId = machine.getId();
        } else {
            machineId = select.getId();
            select.setMachineType(Constant.MACHINE_TYPE_ACCOUNT_MACHINE);
            if (!StringUtils.isEmpty(machine.getMac())) {
                select.setMac(machine.getMac());
            }
            select.setNetWork(Constant.STATUS_DISABLE);
            machineDao.updateMachine(select);
        }


        //移除所有的店铺,不能多次移除
        if (!set.contains(machineId)) {
            set.add(machineId);
            machineWorkTypeDao.invalidAccountMachine(machineId, Constant.LARGE_TASK_TYPE_ACCOUNT_PLATFORM, Constant.STATUS_REMOVE, Constant.STATUS_IS_UPDATE_RESET);
        }

        String checkKey = platformAccount.getShopName() + "_" + platformAccount.getPlatform() + "_" + Constant.MAP_CONTINENTS.get(platformAccount.getSite());
        boolean isInsertFlag = false;
        List<MachineWorkType> machineWorkTypeByMachineId = machineWorkTypeDao.getMachineWorkTypeByMachineId(machineId);
        Map<String, MachineWorkType> machineWorkTypeMap = new HashMap<>();
        if (machineWorkTypeByMachineId == null || machineWorkTypeByMachineId.isEmpty()) {
            isInsertFlag = true;
        } else {
            for (MachineWorkType machineWorkType : machineWorkTypeByMachineId) {
                if (machineWorkType.getPlatformType() == Constant.LARGE_TASK_TYPE_ACCOUNT_PLATFORM) {
                    String key = machineWorkType.getAccount() + "_" + machineWorkType.getPlatform() + "_" + machineWorkType.getContinents();
                    machineWorkTypeMap.put(key, machineWorkType);
                }
            }
        }
        if (machineWorkTypeMap.isEmpty() || !machineWorkTypeMap.containsKey(checkKey)) {
            isInsertFlag = true;
        } else if (machineWorkTypeMap.containsKey(checkKey)) {
            MachineWorkType machineWorkType = machineWorkTypeMap.get(checkKey);
            if (machineWorkType.getIsUpdate() == Constant.STATUS_IS_UPDATE) {
                machineWorkType.setStatus(machineWorkType.getStatus());
                machineWorkTypeDao.updateMachineWorkTypeStatus(machineWorkType);
            } else {
                machineWorkType.setStatus(Constant.STATUS_VALID);
                machineWorkTypeDao.updateMachineWorkTypeStatus(machineWorkType);
                List<MachineWorkTypeTask> machineWorkTypeTaskByWorkTypeId = machineWorkTypeTaskDao.getMachineWorkTypeTaskByWorkTypeId(machineWorkType.getId());
                if (machineWorkTypeTaskByWorkTypeId == null || machineWorkTypeTaskByWorkTypeId.isEmpty()) {
                    insertWorkTypeTask(machineId, machineWorkType);
                }
            }

        }

        if (isInsertFlag) {
            MachineWorkType machineWorkType = new MachineWorkType();
            machineWorkType.setMachineId(machineId);
            machineWorkType.setMachineIp(machine.getIp());
            machineWorkType.setPlatform(platformAccount.getPlatform());
            machineWorkType.setContinents(Constant.MAP_CONTINENTS.get(platformAccount.getSite()));
            machineWorkType.setAccount(platformAccount.getShopName());
            machineWorkType.setPlatformType(Constant.LARGE_TASK_TYPE_ACCOUNT_PLATFORM);
            machineWorkType.setPlatformId(platformDao.getPlatformByName(platformAccount.getPlatform()).getId());
            machineWorkType.setStatus(Constant.STATUS_VALID);
            machineWorkType.setIsUpdate(Constant.STATUS_IS_UPDATE_RESET);
            machineWorkType.setCreatedTime(DateUtils.getAfterDays(0));
            machineWorkType.setIsBrowser(Constant.STATUS_VALID);

            // 插入机器任务类型
            machineWorkTypeDao.insertMachineWorkType(machineWorkType);

            insertWorkTypeTask(machineId, machineWorkType);
        }
    }

    @SneakyThrows
    private void insertWorkTypeTask(int machineId, MachineWorkType machineWorkType) {
        List<Task> taskList = initTaskCache.getWorkTypesMapCache("Account");
        for (Task task : taskList) {
            MachineWorkTypeTask machineWorkTypeTask = new MachineWorkTypeTask();
            machineWorkTypeTask.setMachineId(machineId);
            machineWorkTypeTask.setWorkTypeId(machineWorkType.getId());
            machineWorkTypeTask.setCreatedTime(DateUtils.getAfterDays(0));
            machineWorkTypeTask.setStatus(Constant.STATUS_VALID);
            machineWorkTypeTask.setTaskId(task.getId());
            machineWorkTypeTask.setTaskName(task.getTaskName());
            machineWorkTypeTaskDao.insertMachineWorkTypeTask(machineWorkTypeTask);
        }
    }


    /**
     * 从mini获取并刷新店铺账号、店铺代理IP、店铺站点
     * todo: 这里需要处理由mini数据变化(包含token数据变化)影响的代理IP相关表，而代理IP的变化 不会影响账号表。是不可逆的
     */
    @Override
    public synchronized void refreshMiNiAccountProxyIpAccountSite() {
        List<PlatformAccount> platformAccounts = refreshMiNi();
        List<StoreAccount> storeAccounts = new ArrayList<>();
        int count = 0;
        int count0 = 0;
        int count_agent_ip = 0;
        for (PlatformAccount platformAccount : platformAccounts) {
            StoreAccount storeAccount = new StoreAccount();
            storeAccount.setAccount(platformAccount.getShopName());
            storeAccount.setPlatform(platformAccount.getPlatform());
            storeAccount.setContinents(Constant.MAP_CONTINENTS.get(platformAccount.getSite()));
            int haveMachine = checkIsHaveMachineByAccountContinents(platformAccount.getShopName(), Constant.MAP_CONTINENTS.get(platformAccount.getSite()));
            storeAccount.setHaveMachine(haveMachine);
            storeAccount.setUsername(platformAccount.getPlatformAccount());
            try {
                storeAccount.setPassword(AesUtils.encrypt(platformAccount.getPlatformPsWord(), Constant.C_KEY, Constant.IV_KEY, Constant.CIPHER_VALUE));
            } catch (Exception e) {
                logger.error("platformAccount {}", platformAccount);
                e.printStackTrace();
            }
            if (!StringUtils.isEmpty(platformAccount.getSite()) && !Constant.MAP_CONTINENTS.keySet().contains(platformAccount.getSite())) {
                logger.error("Constant.MAP_CONTINENTS lost continents {}", platformAccount.getSite());
                continue;
            }
            storeAccount.setArea(Constant.MAP_AREA.get(platformAccount.getSite()));

            String area = Constant.MAP_AREA.get(platformAccount.getSite());
            if (platformAccount.getPlatform().equals("AmazonVC")){
                area = CommonUtils.getVCSiteContinent(Constant.MAP_AREA.get(platformAccount.getSite()));
            }
            String tokenInfo = tokenService.getAccountDetail(platformAccount.getShopName(), platformAccount.getPlatform(), area, "");
            if(StringUtils.isEmpty(tokenInfo)){
                continue;
            }
            JSONObject data = getTokenData(tokenInfo, storeAccount);
            StoreAccount storeAccount1 = refreshAccount(storeAccount, platformAccount);
            refreshAccountSites(storeAccount1, data);
            logger.info("success storeAccounts: {} ", storeAccount1);
        }
        initAccountCache.updateAccountCache();
        logger.info("storeAccounts size: {},count: {},count0: {}, count_agent_ip: {}", platformAccounts.size(), count, count0, count_agent_ip);
    }


    /**
     * 这里是根据token获取的数据，设置代理IP(agent_ip)、线上店铺名字(online_name)、店铺状态(account_status)到StoreAccount
     *
     * @param tokenInfo
     * @param storeAccount
     * @return
     */
    @Override
    public JSONObject getTokenData(String tokenInfo, StoreAccount storeAccount) {
        JSONObject json = JSONObject.parseObject(tokenInfo);
        if (json == null) {
            return null;
        }
        String code = String.valueOf(json.get("code"));
        JSONObject data = new JSONObject();
        if ("1".equalsIgnoreCase(code)) {
            data = (JSONObject) json.get("data");
            String onlineName = String.valueOf(data.get("online_name"));
            String accountStatus = String.valueOf(data.get("account_status"));
            storeAccount.setShopName(onlineName);
            if (StringUtils.isEmpty(accountStatus)) {
                storeAccount.setStatus(Constant.STATUS_INVALID);
            } else {
                storeAccount.setStatus(Integer.parseInt(accountStatus));
            }

            JSONObject areaInfo = (JSONObject) data.get("area_info");
            if (areaInfo != null) {
                String agentIp = String.valueOf(areaInfo.get("agent_ip"));
                Object agentPort = areaInfo.get("agent_port");
                Object loginPlatform = areaInfo.get("login_platform");
                int agent_port = 0;
                int login_platform = 0;
                if (!StringUtils.isEmpty(agentPort)) {
                    agent_port = Integer.parseInt(String.valueOf(areaInfo.get("agent_port")));
                }
                if (!StringUtils.isEmpty(loginPlatform)) {
                    login_platform = Integer.parseInt(String.valueOf(loginPlatform));
                }


                String qrContent = String.valueOf(areaInfo.get("qr_content"));
                storeAccount.setProxyIp(agentIp);
                storeAccount.setQrContent(qrContent);
                storeAccount.setLoginPlatform(login_platform);
                if (agent_port != 0) {
                    storeAccount.setProxyIpPort(agent_port);
                } else if (!StringUtils.isEmpty(agentIp)) {
                    ProxyIp proxyIpByIp = proxyIpDao.getProxyIpByIp(agentIp);
                    if (proxyIpByIp != null) {
                        agent_port = proxyIpByIp.getPort();
                        storeAccount.setProxyIpPort(proxyIpByIp.getPort());
                    }
                }
                String key = PROXY_IP_ACCOUNT_CONTINENTS_PREFIX + storeAccount.getAccount() + "_" + storeAccount.getContinents();
                redisUtils.put(key, agentIp + ":" + agent_port, Long.valueOf(3600 * 24));
                if (StringUtils.isEmpty(agentIp)) {
                    logger.error("getTokenData storeAccount:{} agentIp:{} is null", storeAccount, agentIp);
                }
                if (agent_port != 0) {
                    logger.error("getTokenData storeAccount:{} agent_port:{} is 0", storeAccount, agent_port);
                }


            }
        }
        return data;
    }


    /**
     * 这里主要是刷新 用户名、密码、代理IP、店铺状态
     *
     * @param storeAccount
     * @param platformAccount
     * @return
     */
    @Override
    public StoreAccount refreshAccount(StoreAccount storeAccount, PlatformAccount platformAccount) {
        List<StoreAccount> storeAccountByAccountPlatformContinents = new ArrayList<>();
        if (StringUtils.isEmpty(storeAccount.getAccount()) && StringUtils.isEmpty(storeAccount.getContinents())) {
            // 用户名是super的特殊处理
            storeAccountByAccountPlatformContinents = storeAccountDao.getStoreAccountByUsername(storeAccount);
        } else {
            storeAccountByAccountPlatformContinents = storeAccountDao.getStoreAccountByAccountPlatformContinents(storeAccount);
        }

        if (storeAccountByAccountPlatformContinents.isEmpty()) {
            storeAccountDao.insertStoreAccount(storeAccount);
            updateStoreAndProxyIp(new StoreAccount(), storeAccount);
            logger.debug("refreshAccount insert storeAccount:{} ", storeAccount);
        } else {
            // 如果已经存在 则会查看用户名和密码是否被修改
            StoreAccount storeAccount1 = storeAccountByAccountPlatformContinents.get(0);
            storeAccount.setId(storeAccount1.getId());
            storeAccount.setUsername(platformAccount.getPlatformAccount());
            try {
                storeAccount.setPassword(AesUtils.encrypt(platformAccount.getPlatformPsWord(), Constant.C_KEY, Constant.IV_KEY, Constant.CIPHER_VALUE));
            } catch (Exception e) {
                e.printStackTrace();
            }
            storeAccountDao.refreshAccount(storeAccount);
            updateStoreAndProxyIp(storeAccountByAccountPlatformContinents.get(0), storeAccount);
            logger.debug("refreshAccount update storeAccount:{} ", storeAccount1);
            return storeAccount1;
        }
        return storeAccount;
    }


    /**
     * 刷新站点
     *
     * @param
     * @param storeAccount
     */
    @Override
    public void refreshAccountSites(StoreAccount storeAccount, JSONObject data) {

        // 1、首先 使当前店铺下的所有站点失效
//        storeAccountSitesDao.updateStatusInvalid(Integer.parseInt(storeAccount.getId()));

        JSONArray sites = (JSONArray) data.get("sites");
        if (sites == null) {
            return;
        }
        logger.debug("refreshAccountSites id: {},sites: {}", storeAccount.getId(), sites.toJSONString());
        JSONObject site_infos = (JSONObject) data.get("site_infos");

        // 2、对于站点下的店铺，有则生效 没有则添加
        for (Object site : sites) {
            JSONObject siteInfosStatus = (JSONObject) site_infos.get(site);
            Integer status = Integer.parseInt(String.valueOf(siteInfosStatus.get("status")));
            Integer payment = Integer.parseInt(String.valueOf(siteInfosStatus.get("payment")));
            StoreAccountSites storeAccountSites = new StoreAccountSites();
            storeAccountSites.setAccountId(storeAccount.getId());
            storeAccountSites.setSite(String.valueOf(site));
            storeAccountSites.setPayment(payment);
            List<StoreAccountSites> searchList = storeAccountSitesDao.getStoreAccountSites(storeAccountSites);
            storeAccountSites.setStatusPerson(status);

            storeAccountSites.setContinents(storeAccount.getContinents());
            storeAccountSites.setAccount(storeAccount.getAccount());
            if (searchList == null || searchList.isEmpty()) {
                if (status == 2 || status == -10) {
                    //token状态是2 或 -10 更新为失效状态
                    storeAccountSites.setStatus(Constant.STATUS_INVALID);
                } else {
                    storeAccountSites.setStatus(Constant.STATUS_VALID);
                }
                storeAccountSitesDao.insertStoreAccountSites(storeAccountSites);
                logger.debug("refreshAccountSites insert storeAccountSites {}", storeAccountSites);
            } else {
                if (status == 2 || status == -10) {
                    //token状态是2 或 -10 更新为失效状态
                    storeAccountSites.setStatus(Constant.STATUS_INVALID);
                } else {
                    storeAccountSites.setStatus(searchList.get(0).getStatus());
                }
                storeAccountSites.setId(searchList.get(0).getId());
                storeAccountSitesDao.updateStoreAccountSites(storeAccountSites);
                logger.debug("refreshAccountSites update storeAccountSites {}", storeAccountSites);
            }
        }
    }


    /**
     * 需要根据 更新店铺代理IP前后的变化 判断所影响涉及的代理IP及其相关表(代理IP表、代理IP店铺表、代理IP平台禁用表)
     * 需要重置账号表的代理IP和端口的缓存
     *
     * @param sourceStoreAccount
     * @param descStoreAccount
     */
    @Override
    public void updateStoreAndProxyIp(StoreAccount sourceStoreAccount, StoreAccount descStoreAccount) {

        String sourceProxyIp = sourceStoreAccount.getProxyIp();
        String descProxyIp = descStoreAccount.getProxyIp();
        int accountId;
        if (StringUtils.isEmpty(sourceProxyIp) && StringUtils.isEmpty(descProxyIp)) {
            //更新前后都是空 直接返回
            logger.info("updateStoreAndProxyIp all null sourceProxyIp:{}, descProxyIp:{}", sourceProxyIp, descProxyIp);
            return;
        } else if (StringUtils.isEmpty(sourceProxyIp) || StringUtils.isEmpty(descProxyIp)) {
            //有一个空 源空是新增，目的空是删除
            if (!StringUtils.isEmpty(sourceProxyIp)) {
                //删除
                accountId = Integer.parseInt(sourceStoreAccount.getId());
                deleteProxyIp2(sourceProxyIp, accountId);
                logger.info("updateStoreAndProxyIp delete sourceProxyIp:{}, descProxyIp:{}, sourceStoreAccount{}, accountId:{}", sourceProxyIp, descProxyIp, sourceStoreAccount, accountId);
                String key = Constant.PROXY_IP_ACCOUNT_CONTINENTS_PREFIX + sourceStoreAccount.getAccount() + "_" + sourceStoreAccount.getContinents();
                redisUtils.put(key, ":", Long.valueOf(3600 * 12));
            } else if (!StringUtils.isEmpty(descProxyIp)) {
                //新增
                accountId = Integer.parseInt(descStoreAccount.getId());
                insertProxyIp2(descProxyIp, accountId, descStoreAccount);
                logger.info("updateStoreAndProxyIp insert sourceProxyIp:{}, descProxyIp:{}, sourceStoreAccount{}, accountId:{}", sourceProxyIp, descProxyIp, sourceStoreAccount, accountId);
                String key = Constant.PROXY_IP_ACCOUNT_CONTINENTS_PREFIX + descStoreAccount.getAccount() + "_" + descStoreAccount.getContinents();
                if (redisUtils.exists(key)) {
                    redisUtils.remove(key);
                }
            }
        } else {
            // 都不是空
            accountId = Integer.parseInt(sourceStoreAccount.getId());
            if (sourceStoreAccount.getProxyIp().equals(descStoreAccount.getProxyIp())) {
                ProxyIp proxyIpByIp = proxyIpDao.getProxyIpByIp(sourceProxyIp);
                if (proxyIpByIp == null) {
                    //源代理IP 应该不会不存在
//                    insertProxyIpByIp(descProxyIp, accountId, sourceStoreAccount);
                } else {
                    searchProxyIpShopByProxyIpIdAndAccountId2(proxyIpByIp, accountId, sourceStoreAccount);
                    proxyIpDao.updateLargeTaskType(proxyIpByIp.getId(),Constant.LARGE_TASK_TYPE_ACCOUNT_PLATFORM);
                }
                return;
            } else {
                deleteProxyIp2(sourceProxyIp, accountId);
                insertProxyIp2(descProxyIp, accountId, sourceStoreAccount);
                logger.info("updateStoreAndProxyIp delete and insert sourceProxyIp:{}, descProxyIp:{}, sourceStoreAccount{}, accountId:{}", sourceProxyIp, descProxyIp, sourceStoreAccount, accountId);
                String key = Constant.PROXY_IP_ACCOUNT_CONTINENTS_PREFIX + descStoreAccount.getAccount() + "_" + descStoreAccount.getContinents();
                if (redisUtils.exists(key)) {
                    redisUtils.remove(key);
                }
            }
        }
    }


    @Deprecated
    private void deleteProxyIp(String ProxyIp, int accountId) {
        ProxyIp proxyIpByIp = proxyIpDao.getProxyIpByIp(ProxyIp);
        proxyIpShopDao.updateProxyIpShopStatus(proxyIpByIp.getId(), accountId, Constant.STATUS_REMOVE);
    }

    @Deprecated
    private void insertProxyIp(String descProxyIp, int accountId, StoreAccount sourceStoreAccount) {
        ProxyIp descProxyIpByIp = proxyIpDao.getProxyIpByIp(descProxyIp);
        if (descProxyIpByIp == null) {
            // 代理IP表 不存在当前IP
            insertProxyIpByIp(descProxyIp, accountId, sourceStoreAccount);
        } else {
            searchProxyIpShopByProxyIpIdAndAccountId(descProxyIpByIp, accountId, sourceStoreAccount);
        }
    }

    @Override
    public int checkIsHaveMachineByAccountContinents(String account, String continent) {
        if (StringUtils.isEmpty(account) || StringUtils.isEmpty(continent)) {
            return AccountAssociateMachine.INIT;
        }
        List<MachineWorkType> machineByAccount = machineWorkTypeDao.getMachineByAccount(account, continent);
        if (machineByAccount == null || machineByAccount.isEmpty()) {
            return AccountAssociateMachine.INIT;
        }
        int haveMachine = AccountAssociateMachine.INIT;
        for (MachineWorkType machineWorkType : machineByAccount) {
            if (haveMachine == AccountAssociateMachine.ENABLE_AVAILABLE) {
                continue;
            }
            boolean workTypeEnable = false;
            boolean machineEnable = false;
            if (machineWorkType.getStatus() == Constant.STATUS_VALID) {
                workTypeEnable = true;
            }
            int machineId = machineWorkType.getMachineId();
            Machine machineById = machineDao.getMachineById(machineId);
            if (machineById.getStatus() == Constant.STATUS_VALID) {
                machineEnable = true;
            }
            if (machineEnable && workTypeEnable) {
                haveMachine = AccountAssociateMachine.ENABLE_AVAILABLE;
            } else if (machineEnable && !workTypeEnable) {
                haveMachine = AccountAssociateMachine.ENABLE_UNAVAILABLE;
            } else if (!machineEnable && workTypeEnable) {
                haveMachine = AccountAssociateMachine.NOTENABLE_AVAILABLE;
            } else if (!machineEnable && !workTypeEnable) {
                haveMachine = AccountAssociateMachine.NOTENABLE_UNAVAILABLE;
            }

        }
        initAccountCache.updateAccountContinentsStatusCache(account, continent, haveMachine);
        return haveMachine;
    }


    /**
     * 根据代理IP的id 和 账号Id查询是否存在 不存在则新增 存在则更新为有效状态
     *
     * @param proxyIp
     * @param accountId
     * @param sourceStoreAccount
     */
    private void searchProxyIpShopByProxyIpIdAndAccountId(ProxyIp proxyIp, int accountId, StoreAccount sourceStoreAccount) {
        ProxyIpShop proxyIpShopBySearch = proxyIpShopDao.selectProxyIpShop(proxyIp.getId(), accountId);
        if (proxyIpShopBySearch == null) {
            ProxyIpShop proxyIpShop = new ProxyIpShop();
            proxyIpShop.setProxyIpId(proxyIp.getId());
            proxyIpShop.setAccountId(accountId);
            proxyIpShop.setAccount(sourceStoreAccount.getAccount());
            proxyIpShop.setPlatform(sourceStoreAccount.getPlatform());
            proxyIpShop.setContinents(sourceStoreAccount.getContinents());
            proxyIpShop.setStatus(Constant.STATUS_VALID);
            proxyIpShopDao.insertProxyIpShop(proxyIpShop);
            logger.info("insertProxyIpShop proxyIpShop{}", proxyIpShop);
        } else {
            proxyIpShopDao.updateProxyIpShopStatus(proxyIpShopBySearch.getId(), accountId, Constant.STATUS_VALID);
        }
    }


    @SneakyThrows
    private void insertAccountProxyAndPlatform(ProxyIp descProxyIpByIp, StoreAccount sourceStoreAccount) {
        // 2、插入当前店铺的
        AccountProxy accountProxy = new AccountProxy();
        accountProxy.setProxyIpId(descProxyIpByIp.getId());
        accountProxy.setProxyIp(descProxyIpByIp.getIp());
        accountProxy.setPort(sourceStoreAccount.getProxyIpPort());
        accountProxy.setAccountId(Integer.parseInt(sourceStoreAccount.getId()));
        accountProxy.setAccount(sourceStoreAccount.getAccount());
        accountProxy.setPlatform(sourceStoreAccount.getPlatform());
        accountProxy.setContinents(sourceStoreAccount.getContinents());
        accountProxy.setStatus(Constant.STATUS_VALID);
        accountProxy.setCreatedTime(DateUtils.getAfterDays(0));
        accountProxyDao.insertAccountProxy(accountProxy);

        // 3、插入当前IP的 平台状态
//        for (String platform : Constant.PLATFORMS) {
//            if (platform.equals(sourceStoreAccount.getPlatform())) {
//                continue;
//            }
//            ProxyIpPlatform proxyIpPlatform = new ProxyIpPlatform();
//            proxyIpPlatform.setProxyIpId(descProxyIpByIp.getId());
//            proxyIpPlatform.setStatus(Constant.STATUS_VALID);
//            proxyIpPlatform.setPlatform(platform);
//            proxyIpPlatformDao.insertProxyIpPlatform(proxyIpPlatform);
//        }
    }


    private ProxyIp insertProxyIpByIp(String descProxyIp, Integer accountId, StoreAccount sourceStoreAccount) {
        // 代理IP表 不存在当前IP
        // 1、插入代理IP
        ProxyIp descProxyIpByIp = new ProxyIp();
        descProxyIpByIp.setIp(descProxyIp);
        descProxyIpByIp.setPort(sourceStoreAccount.getProxyIpPort());
        descProxyIpByIp.setValidStatus(Constant.STATUS_VALID);
        proxyIpDao.insertProxyIp(descProxyIpByIp);

        // 2、插入当前店铺的
        ProxyIpShop proxyIpShop = new ProxyIpShop();
        proxyIpShop.setProxyIpId(descProxyIpByIp.getId());
        proxyIpShop.setAccountId(accountId);
        proxyIpShop.setAccount(sourceStoreAccount.getAccount());
        proxyIpShop.setPlatform(sourceStoreAccount.getPlatform());
        proxyIpShop.setContinents(sourceStoreAccount.getContinents());
        proxyIpShop.setStatus(Constant.STATUS_VALID);
        proxyIpShopDao.insertProxyIpShop(proxyIpShop);

        // 3、插入当前IP的 平台状态
//        for (String platform : Constant.PLATFORMS) {
//            ProxyIpPlatform proxyIpPlatform = new ProxyIpPlatform();
//            proxyIpPlatform.setProxyIpId(descProxyIpByIp.getId());
//            proxyIpPlatform.setStatus(Constant.STATUS_VALID);
//            proxyIpPlatform.setPlatform(platform);
//            proxyIpPlatformDao.insertProxyIpPlatform(proxyIpPlatform);
//        }

        return descProxyIpByIp;
    }


    private void deleteProxyIp2(String ProxyIp, int accountId) {
        ProxyIp proxyIpByIp = proxyIpDao.getProxyIpByIp(ProxyIp);
//        proxyIpShopDao.updateProxyIpShopStatus(proxyIpByIp.getId(), accountId, Constant.STATUS_REMOVE);
        accountProxyDao.updateAccountProxyStatus(proxyIpByIp.getId(), accountId, Constant.STATUS_INVALID);
    }

    private void insertProxyIp2(String descProxyIp, int accountId, StoreAccount sourceStoreAccount) {
        ProxyIp descProxyIpByIp = proxyIpDao.getProxyIpByIp(descProxyIp);
        if (descProxyIpByIp == null) {
            // 代理IP表 不存在当前IP
            insertProxyIpByIp2(descProxyIp, accountId, sourceStoreAccount);
        } else {
            searchProxyIpShopByProxyIpIdAndAccountId2(descProxyIpByIp, accountId, sourceStoreAccount);
        }
    }

    /**
     * 根据代理IP的id 和 账号Id查询是否存在 不存在则新增 存在则更新为有效状态
     *
     * @param proxyIp
     * @param accountId
     * @param sourceStoreAccount
     */
    private void searchProxyIpShopByProxyIpIdAndAccountId2(ProxyIp proxyIp, int accountId, StoreAccount sourceStoreAccount) {
        if(proxyIp.getPort() == 0 && sourceStoreAccount.getProxyIpPort() != 0){
            proxyIp.setPort(sourceStoreAccount.getProxyIpPort());
            proxyIpDao.updateProxyIpPort(proxyIp.getId(), sourceStoreAccount.getProxyIpPort());
        }
        AccountProxy accountProxy = accountProxyDao.selectAccountProxy(proxyIp.getId(), accountId);
        if (accountProxy == null) {
            insertAccountProxyAndPlatform(proxyIp, sourceStoreAccount);
            logger.info("insertProxyIpShop proxyIpShop{}", proxyIp);
        } else {
            if (accountProxy.getStatus() == Constant.STATUS_INVALID) {
                accountProxyDao.updateAccountProxyStatusById(accountProxy.getId(), Constant.STATUS_VALID);
            }else if(accountProxy.getPort() == 0 && sourceStoreAccount.getProxyIpPort() != 0){
                accountProxyDao.updateAccountProxyPortById(accountProxy.getId(), sourceStoreAccount.getProxyIpPort());
            }
        }
    }

    @SneakyThrows
    private ProxyIp insertProxyIpByIp2(String descProxyIp, Integer accountId, StoreAccount sourceStoreAccount) {
        // 代理IP表 不存在当前IP
        // 1、插入代理IP
        ProxyIp descProxyIpByIp = new ProxyIp();
        descProxyIpByIp.setIp(descProxyIp);
        descProxyIpByIp.setPort(sourceStoreAccount.getProxyIpPort());
        descProxyIpByIp.setValidStatus(Constant.STATUS_VALID);
        descProxyIpByIp.setLargeTaskType(Constant.LARGE_TASK_TYPE_ACCOUNT_PLATFORM);
        proxyIpDao.insertProxyIp(descProxyIpByIp);
        descProxyIpByIp = proxyIpDao.getProxyIpByIp(descProxyIp);

        insertAccountProxyAndPlatform(descProxyIpByIp, sourceStoreAccount);

        return descProxyIpByIp;
    }

    @Override
    public PageDataResult getStoreAccountBySiteStatus(int pageNum, int pageSize, StoreAccount storeAccount, Integer accountSiteStatus) {
        PageHelper.startPage(pageNum, pageSize);
        List<StoreAccount> res = storeAccountDao.getStoreAccountByStoreAccountAndSite(storeAccount, accountSiteStatus);
        for (StoreAccount account : res) {
            List<MachineWorkType> machineWorkType = machineWorkTypeDao.getMachineByAccount(account.getAccount(), account.getContinents());
            account.setMachineWorkTypeList(machineWorkType);
        }
        PageDataResult pageDataResult = new PageDataResult();
        if (res.size() != 0) {
            PageInfo<StoreAccount> pageInfo = new PageInfo<>(res);
            pageDataResult.setList(res);
            pageDataResult.setTotals((int) pageInfo.getTotal());
            pageDataResult.setPageNum(pageNum);
        }
        return pageDataResult;
    }

    @Override
    public StoreAccount getStoreAccountByAccountContinents(String account, String continents) {
        return storeAccountDao.getStoreAccountByAccountContinents(account, continents);
    }

    public PageDataResult getStoreAccountAndMachine(StoreAccount storeAccount, Integer pageNum, Integer pageSize) {

        List<MachineWorkType> machineByAccount = machineWorkTypeDao.getMachineByAccount(storeAccount.getAccount(), storeAccount.getContinents());
        PageDataResult pageDataResult = new PageDataResult();
        if (machineByAccount.size() != 0) {
            PageInfo<MachineWorkType> pageInfo = new PageInfo<>(machineByAccount);
            pageDataResult.setList(machineByAccount);
            pageDataResult.setTotals((int) pageInfo.getTotal());
            pageDataResult.setPageNum(pageNum);
        }
        return pageDataResult;

    }
}

