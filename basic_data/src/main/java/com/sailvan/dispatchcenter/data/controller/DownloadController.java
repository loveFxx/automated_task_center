package com.sailvan.dispatchcenter.data.controller;

import com.sailvan.dispatchcenter.common.constant.MonitorConstant;
import com.sailvan.dispatchcenter.common.domain.Machine;
import com.sailvan.dispatchcenter.common.domain.ProxyIp;
import com.sailvan.dispatchcenter.common.domain.StoreAccount;
import com.sailvan.dispatchcenter.common.domain.StoreAccountSites;
import com.sailvan.dispatchcenter.common.pipe.ProxyIpService;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.ExcelUtils;
import com.sailvan.dispatchcenter.common.util.HttpDownloadUtils;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.data.statistics.ContinentAccountStat;
import com.sailvan.dispatchcenter.data.statistics.MachineHeartBeatLogStat;
import com.sailvan.dispatchcenter.data.statistics.TaskLogStat;
import com.sailvan.dispatchcenter.db.dao.automated.StoreAccountDao;
import com.sailvan.dispatchcenter.db.dao.automated.StoreAccountSitesDao;
import com.sailvan.dispatchcenter.db.service.MachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @program: automated_task_center10
 * @description:
 * @author: Wu Xingjian
 * @create: 2021-11-08 17:25
 **/
@RestController
public class DownloadController {

    @Autowired
    private MachineService machineService;

    @Autowired
    private TaskLogStat taskLogStat;

    @Autowired
    private MachineHeartBeatLogStat machineHeartBeatLogStat;

    @Autowired
    private ContinentAccountStat continentAccountStat;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    private StoreAccountDao storeAccountDao;

    @Autowired
    private StoreAccountSitesDao storeAccountSiteDao;

    @Autowired
    private StoreAccountSiteMonitorController storeAccountSiteMonitorController;

    @Autowired
    private MachineMonitorController machineMonitorController;

    @Autowired
    private ProxyIpService proxyIpService;


    /**
     * 任务流水页的几个excel下载
     */
    @RequestMapping(value = "/download/{statType}/{date}", method = RequestMethod.GET)
    public String fileDownLoad(HttpServletResponse response, @PathVariable String statType, @PathVariable String date) throws IOException, InterruptedException {

        String filePath = System.getProperty("user.dir") + "/stat";
        Map<String, String> startEndOfDay = null;
        try {
            startEndOfDay = DateUtils.getStartEndOfDay(date);
        } catch (ParseException e) {
            return e.getMessage();
        }
        String oneDayStart = startEndOfDay.get("oneDayStart");
        String oneDayEnd = startEndOfDay.get("oneDayEnd");
        String secondDayStart = startEndOfDay.get("secondDayStart");
        String secondDayEnd = startEndOfDay.get("secondDayEnd");

        if (MonitorConstant.HEARTBEAT_STAT.equals(statType)) {
            filePath += "/" + MonitorConstant.HEARTBEAT_STAT + date + ".xlsx";
            machineHeartBeatLogStat.heartBeatStat(oneDayStart, oneDayEnd, filePath);
        } else if (MonitorConstant.TASK_SUCCESS_STAT.equals(statType)) {
            filePath += "/" + MonitorConstant.TASK_SUCCESS_STAT + date + ".xlsx";
            taskLogStat.taskSuccessStat(oneDayStart, oneDayEnd, secondDayStart, secondDayEnd, filePath);
        } else if (MonitorConstant.TASK_FAILURE_REASON_STAT.equals(statType)) {
            filePath += "/" + MonitorConstant.TASK_FAILURE_REASON_STAT + date + ".xlsx";
            taskLogStat.taskFailureReasonStat(oneDayStart, oneDayEnd, filePath);
        } else if (MonitorConstant.CONTINENT_ACCOUNT_TASK_STAT.equals(statType)) {
            filePath += "/" + MonitorConstant.CONTINENT_ACCOUNT_TASK_STAT + date + ".xlsx";
            continentAccountStat.continentAccountStat(oneDayStart, oneDayEnd, filePath);
        } else {
            return statType + "类型错误";
        }

        File file = new File(filePath);
        if (!file.exists()) {
            return "下载文件不存在";
        }
        return HttpDownloadUtils.httpDownload(response,file);
    }


    /**
     * 店铺监控页下载店铺excel
     * @param response
     * @param idList
     * @return
     */
    @RequestMapping(value = "/downloadAccountExcelFromMonitor", method = RequestMethod.POST)
    public String downloadAccountExcelFromMonitor(HttpServletResponse response,String idList) {

        ArrayList<String[]> infoList = new ArrayList<>();
        String[] split = idList.split(",");
        for (String id : split) {
            StoreAccount storeAccount = storeAccountDao.getStoreAccountById(Integer.parseInt(id));
            if (storeAccount != null) {
                infoList.add(new String[]{storeAccount.getId(),storeAccount.getAccount(),storeAccount.getContinents()});
            }
        }
        String []stringArr = new String[]{ "id","account","continents"};
        String filePath = System.getProperty("user.dir") + "/stat" + "/accountMonitor.xlsx";
        ExcelUtils.createExcelFile3(filePath, stringArr, infoList);
        File file = new File(filePath);
        if (!file.exists()) {
            return "下载文件不存在";
        }
        return HttpDownloadUtils.httpDownload(response, file);
    }


    /**
     * 店铺监控页下载站点excel
     * @param response
     * @param idList
     * @return
     */
    @RequestMapping(value = "/downloadSiteExcelFromMonitor")
    public String downloadSiteExcelFromMonitor(HttpServletResponse response,String idList) {

        ArrayList<String[]> infoList = new ArrayList<>();
        String[] split = idList.split(",");
        for (String id : split) {
            StoreAccountSites site = storeAccountSiteDao.getStoreAccountSitesById(id);
            if (site != null) {
                infoList.add(new String[]{site.getId(),site.getAccount(),site.getSite()});
            }
        }
        String []stringArr = new String[]{ "id","account","site"};
        String filePath = System.getProperty("user.dir") + "/stat" + "/sitesMonitor.xlsx";
        ExcelUtils.createExcelFile3(filePath, stringArr, infoList);
        File file = new File(filePath);
        if (!file.exists()) {
            return "下载文件不存在";
        }
        return HttpDownloadUtils.httpDownload(response, file);
    }


    /**
     *
     * downloadMachineSummaryExcel:机器监控页导出 机器异常汇总的excel
     * 店铺监控页导出 无关联/可用机的店铺 二次验证对不上的店铺
     * @param response
     * @return
     */
    @RequestMapping(value = "/downloadMachineSummaryExcel", method = RequestMethod.GET)
    public String downloadMachineSummaryExcel(HttpServletResponse response) {

        ArrayList<String[]> infoList = new ArrayList<>();
        String[] stringArr = null;
        String filePath = "";
        List<Machine> res = machineMonitorController.summaryMachine();
        for (Machine machine : res) {
            //网络初始状态-1不显示了
            String network = machine.getNetWork() == 0 ? String.valueOf(machine.getNetWork()) : "";
            infoList.add(new String[]{machine.getIp(), machine.getLastHeartbeat(), network, machine.getDiskSpace(), machine.getMemory(), machine.getMachineLocalTime()});
        }
        stringArr = new String[]{ "机器ip","心跳超时","网络不可用","磁盘空间不足 已用/全部","内存空间不足","心跳/机器时间差异"};
        filePath = System.getProperty("user.dir") + "/stat" + "/machineMonitor.xlsx";
        ExcelUtils.createExcelFile3(filePath, stringArr, infoList);
        File file = new File(filePath);
        if (!file.exists()) {
            return "下载文件不存在";
        }
        return HttpDownloadUtils.httpDownload(response, file);
    }


    /**
     *
     * downloadMachineSummaryExcel:机器监控页导出 机器异常汇总的excel
     * 店铺监控页导出 无关联/可用机的店铺 二次验证对不上的店铺
     * @param response
     * @param excelType
     * @return
     */
    @RequestMapping(value = "/downloadExcelFromMonitor/{excelType}")
    public String downloadExcelFromMonitor(HttpServletResponse response, @PathVariable String excelType) {

        ArrayList<String[]> infoList = new ArrayList<>();

        String[] stringArr = null;
        String filePath = "";

        switch (excelType) {

            case "downloadInvalidIpExcel":
                List<ProxyIp> proxyIpAll = proxyIpService.getProxyIpAll();
                for (ProxyIp proxyIp : proxyIpAll) {
                    //在所有代理IP中找出失效的
                    if (proxyIp.getValidStatus() == 0){
                        List<StoreAccount> storeAccountByIp = storeAccountDao.getStoreAccountByIp(proxyIp.getIp());
                        if (storeAccountByIp.size() != 0){
                            //把有对应账号站点的代理IP放在前面
                            for (StoreAccount storeAccount:storeAccountByIp){
                                infoList.add(new String[]{storeAccount.getProxyIp(), String.valueOf(proxyIp.getPort()),storeAccount.getAccount(),storeAccount.getContinents() });
                            }
                        }
                    }
                }
                for (ProxyIp proxyIp : proxyIpAll) {
                    if (proxyIp.getValidStatus() == 0){
                        List<StoreAccount> storeAccountByIp = storeAccountDao.getStoreAccountByIp(proxyIp.getIp());
                        if (storeAccountByIp.size() == 0){
                            //把没有对应账号站点的代理IP放在后面
                            infoList.add(new String[]{proxyIp.getIp(), String.valueOf(proxyIp.getPort()),"~","~" });
                        }
                    }
                }
                stringArr = new String[]{ "代理ip","端口","账号","站点"};
                filePath = System.getProperty("user.dir") + "/stat" + "/invalidIp.xlsx";
                ExcelUtils.createExcelFile3(filePath, stringArr, infoList);
                break;
            default:
        }


        ExcelUtils.createExcelFile3(filePath, stringArr, infoList);

        File file = new File(filePath);
        if (!file.exists()) {
            return "下载文件不存在";
        }

        return HttpDownloadUtils.httpDownload(response, file);

    }




}
