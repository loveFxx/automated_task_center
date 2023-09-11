package com.sailvan.dispatchcenter.stat.monitor.scheduler;

import com.sailvan.dispatchcenter.common.cache.ProxyIPPool;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.pipe.AccountProxyService;
import com.sailvan.dispatchcenter.common.pipe.StoreAccountService;
import com.sailvan.dispatchcenter.common.pipe.StoreAccountSitesService;
import com.sailvan.dispatchcenter.common.util.ExcelUtils;
import com.sailvan.dispatchcenter.db.service.ProxyIpService;
import com.sailvan.dispatchcenter.stat.monitor.config.WeChatRobotTokenConfig;
import com.sailvan.dispatchcenter.stat.monitor.util.WeChatRobotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

/**
 * 测试代理IP是否有效
 *
 * @author yyj
 * @date 2021-12
 */
@Component
public class ProxyIPMonitorScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ProxyIPMonitorScheduler.class);

    @Autowired
    ProxyIpService proxyIpService;

    @Autowired
    AccountProxyService accountProxyService;

    @Autowired
    WeChatRobotTokenConfig weChatRobotTokenConfig;

    @Autowired
    StoreAccountService storeAccountService;

    @Autowired
    StoreAccountSitesService storeAccountSitesService;

    @Autowired
    ProxyIPPool proxyIPPool;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();


    @Scheduled(cron = "0 0 6,15,22 * * ?")
    public void monitorAndRecordProxyIP() throws Exception {
        logger.info("monitorAndRecordProxyIP start...");
        String ids = "";
        List<ProxyIp> proxyIpAll = proxyIpService.listProxyByLargeTaskType(Constant.LARGE_TASK_TYPE_CRAWL_PLATFORM);
        for (ProxyIp proxyIp : proxyIpAll) {
            //跳过AmazonDaemon代理
            if (proxyIp.getPort() == 0 || proxyIp.getCrawlPlatform().equals("7")) {
                continue;
            }
            boolean b = newCheckProxyIp(proxyIp.getIp(), proxyIp.getPort());
            if (b) {
                //有效
                proxyIpService.setProxyIpValidStatus(1,proxyIp.getId());

                //有效重置
                proxyIpService.updateValidateTimes(0,0,proxyIp.getId());
                proxyIPPool.pushProxy(proxyIp.getId());

            } else {
                //无效
                proxyIpService.setProxyIpValidStatus(0,proxyIp.getId());
                proxyIPPool.removeProxy(proxyIp.getId());

                int validateTimes = proxyIp.getValidateTimes();
                validateTimes++;
                //校验失效达到10次做软删除
                if (validateTimes == 10){
                    proxyIpService.updateValidateTimes(validateTimes,1,proxyIp.getId());
                }else {
                    proxyIpService.updateValidateTimes(validateTimes,0,proxyIp.getId());
                }

                logger.info("无效ip--{}", proxyIp.getIp());
                //无效还要去查出对应的账号 大洲 报警
                ids = ids + proxyIp.getId() + ",";
            }
        }
        //有失效的去报警
        if (!ids.equals("")) {
            alarmRobot(ids);
        }
        logger.info("monitorAndRecordProxyIP over...");
        EXECUTOR.shutdown();
    }

    public boolean newCheckProxyIp(String proxyHost, Integer port) {

        HttpURLConnection oConn = null;
        try {
            Proxy oProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, port));

            URL url = new URL("http://www.baidu.com");
            oConn = (HttpURLConnection) url.openConnection(oProxy);
            oConn.setConnectTimeout(5000);
            oConn.connect();

            return true;
        } catch (Exception e) {
            logger.error("代理失效异常:" + e.getMessage());
            return false;
        } finally {
            if (oConn != null) {
                oConn.disconnect();
            }
        }
    }

    //通过失效IP获取对应店铺 并报警

    public int alarmRobot(String ids) throws Exception {

        String[] idsStr = ids.split(",");
        List<AccountProxy> accountProxyAll = new ArrayList<>();
        for (int i = 0; i < idsStr.length; i++) {
            List<AccountProxy> accountProxyByProxyIpId = accountProxyService.getAccountProxyByProxyIpId(Integer.parseInt(idsStr[i]));
            for (AccountProxy proxyIpShop : accountProxyByProxyIpId) {
                accountProxyAll.add(proxyIpShop);
            }
        }
        if (accountProxyAll == null || accountProxyAll.size() == 0) {
            return 0;
        }

        WeChatRobotUtils weChatRobotUtils = new WeChatRobotUtils(weChatRobotTokenConfig.getWechatRobotToken());
        String[][] msgArr = new String[accountProxyAll.size() + 1][6];
        msgArr[0] = new String[]{"ip", "account", "continents", "店铺状态", "信用卡状态(站点)", "机器状态"};
        for (int i = 0; i < accountProxyAll.size(); i++) {
            AccountProxy accountProxy = accountProxyAll.get(i);
            String account = accountProxy.getAccount();
            String continents = accountProxy.getContinents();
            //查询对应店铺 获取店铺状态和对应机器状态
            StoreAccount storeAccount = storeAccountService.getStoreAccountByAccountContinents(account, continents);
            String storeAccountStatus = setStoreAccountStatus(storeAccount.getStatus());
            String accountMachineStatus = setMachineStatus(storeAccount.getHaveMachine());
            StoreAccountSites storeAccountSites = new StoreAccountSites();
            //查询对应站点信息 获取其中信用卡状态
            storeAccountSites.setAccount(account);
            storeAccountSites.setContinents(continents);
            List<StoreAccountSites> storeAccountSitesList =
                    storeAccountSitesService.getStoreAccountSitesByAccountContinents(storeAccountSites);
            //判断并获取对应站点 信用卡绑定状态
            String payStatus = setPaymantStatus(storeAccountSitesList);
            msgArr[i + 1] = new String[]{accountProxy.getProxyIp(), account, continents, storeAccountStatus, payStatus, accountMachineStatus};
        }
        StringBuilder sb = new StringBuilder();
        sb.append("代理IP失效报警\n");
        sb.append(weChatRobotUtils.alignColumn(msgArr));
        ArrayList<String> string1 = ExcelUtils.splitLongString(sb.toString(), '\n', 5000);
        for (String string : string1) {
            weChatRobotUtils.text(string, new String[]{}, new String[]{});
        }
        return 1;

    }

    //判断店铺对应机器状态
    private String setMachineStatus(int haveMachine) {
        //是否有关联机器 0 初始化或账号大洲是空 1有且开启 2有没有开启 3 没有开启且有可用大类型 4没有开启无可用大类型
        String accountMachineStatus = "";
        if (haveMachine == 0) {
            accountMachineStatus = "无机器";
        } else if (haveMachine == 1) {
            accountMachineStatus = "机器正常";
        } else {
            accountMachineStatus = "机器异常";
        }
        return accountMachineStatus;
    }

    //判断信用卡状态
    private String setPaymantStatus(List<StoreAccountSites> storeAccountSitesList) {
        String payStatus = "";
        String str = "";
        for (StoreAccountSites sas : storeAccountSitesList) {
            int payment = sas.getPayment();
            if (payment == 0) {
                if ("".equals(str)) {
                    str = sas.getSite();
                } else {
                    str = str + "," + sas.getSite();
                }
            }
        }
        if ("".equals(str)) {
            payStatus = "信用卡已绑定";
        } else {
            payStatus = "信用卡以解绑" + "(" + str + ")";
        }
        return payStatus;
    }

    //判断店铺状态
    private String setStoreAccountStatus(int status) {
        String storeAccountStatus = "";
        if (status == 0) {
            storeAccountStatus = "正常（未运行）";
        } else if (status == 1) {
            storeAccountStatus = "正常（运营中）";
        } else if (status == 2) {
            storeAccountStatus = "关店（不可登录）";
        } else if (status == 3) {
            storeAccountStatus = "关店（可登录）";
        } else if (status == 4) {
            storeAccountStatus = "暂停运营(假期模式)";
        } else if (status == -10) {
            storeAccountStatus = "无效店铺";
        } else if (status == -2) {
            storeAccountStatus = "未验证";
        }
        return storeAccountStatus;
    }

}
