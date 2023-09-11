package com.sailvan.dispatchcenter.data.controller;

import com.alibaba.fastjson.JSONArray;
import com.sailvan.dispatchcenter.common.constant.CacheKey;

import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.pipe.*;
import com.sailvan.dispatchcenter.common.remote.RemotePushTaskUpdate;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.CommonUtils;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.db.dao.automated.StoreAccountDao;
import com.sailvan.dispatchcenter.db.service.ProxyRequestLogsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 监控入口
 *
 * @author mh
 * @date 2021-12
 **/
@RestController
public class MonitorController {

    private static Logger logger = LoggerFactory.getLogger(MonitorController.class);

    @Autowired
    RequestCountService requestCountService;

    @Autowired
    TaskInPoolMetricService taskInPoolMetricService;

    @Autowired
    TaskExecutedExceptionService taskExecutedExceptionService;

    @Autowired
    TaskIOMetricService taskIOMetricService;

    @Autowired
    TaskMetricService taskMetricService;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    RemotePushTaskUpdate remotePushTaskUpdate;

    @Autowired
    ProxyIpService proxyIpService;

    @Autowired
    StoreAccountDao storeAccountDao;

    @Autowired
    ProxyRequestLogsService proxyRequestLogsService;

    @Autowired
    ProxyTrendService proxyTrendService;

    /**
     * 获取{taskName:taskName}类型下拉选择框
     */
    @RequestMapping(value = "/getRequestCountMonitor")
    @ResponseBody
    public Map<String, int[]> getDropDownTaskName(@RequestParam("systemName") String systemName,@RequestParam("method") String method,@RequestParam("showType") String showType) {
//        String lastTime = DateUtils.getHourBeforeDate(0).substring(0, 10)+"-00";
        String lastTime = DateUtils.getHourBeforeDate(24).substring(0, 13).replace(" ", "-");
        if(!StringUtils.isEmpty(showType) && "2".equals(showType)){
            lastTime = DateUtils.getDayBeforeDate(7).substring(0, 10)+"-00";
        }
        List<RequestCount> requestCountByPeriod = requestCountService.getRequestCountByPeriod(lastTime);
        Map<String, int[]> result = new HashMap<>();
        List<String> periodMap = new ArrayList<>();
        Map<String, String> requestNumMap = new TreeMap<>((String obj1, String obj2)->obj1.compareTo(obj2));
        Map<String, String> requestSuccessMap = new TreeMap<>((String obj1, String obj2)->obj1.compareTo(obj2));
        Map<String, String> requestSuccessRatingMap = new TreeMap<>((String obj1, String obj2)->obj1.compareTo(obj2));
        Map<String, String> requestTimeoutMap = new TreeMap<>((String obj1, String obj2)->obj1.compareTo(obj2));
        Map<String, String> requestExceptionMap = new TreeMap<>((String obj1, String obj2)->obj1.compareTo(obj2));
        Map<String, String> requestLimitMap = new TreeMap<>((String obj1, String obj2)->obj1.compareTo(obj2));
        for (RequestCount requestCount : requestCountByPeriod) {
            if(!StringUtils.isEmpty(systemName) && !systemName.equals(requestCount.getSystemName())){
                continue;
            }
            if(!StringUtils.isEmpty(method) && !method.equals(requestCount.getRequestMethod())){
                continue;
            }
            String period = requestCount.getPeriod();
            if(!StringUtils.isEmpty(showType) && "2".equals(showType)){
                period = period.substring(0, 10);
            }

            if(!periodMap.contains(period)){
                periodMap.add(period);
            }
            getCount(requestNumMap, period, requestCount.getRequestNum());
            getCount(requestSuccessMap, period, requestCount.getRequestSuccess());
            getCount(requestTimeoutMap, period, requestCount.getRequestTimeout());
            getCount(requestExceptionMap, period, requestCount.getRequestException());
            getCount(requestLimitMap, period, requestCount.getRequestLimit());
        }
        List<String> timeList = new ArrayList<>();

        for (String current : periodMap) {
//            timeList.add(current.substring(current.lastIndexOf("-")+1));
            timeList.add(current.replaceAll("-",""));
            String success = requestSuccessMap.get(current);
            String num = requestNumMap.get(current);
            int rating = 0;
            if(Integer.parseInt(num) != 0){
                rating = (Integer.parseInt(success)*100)/Integer.parseInt(num);
            }
            requestSuccessRatingMap.put(current, String.valueOf(rating));
        }
        int[] periods = timeList.stream().mapToInt(Integer::valueOf).toArray();

        result.put("periods", periods);
        result.put("requestNum", getMapToIntegers(requestNumMap));
        result.put("requestSuccess", getMapToIntegers(requestSuccessMap));
        result.put("requestTimeout", getMapToIntegers(requestTimeoutMap));
        result.put("requestException", getMapToIntegers(requestExceptionMap));
        result.put("requestLimit", getMapToIntegers(requestLimitMap));
        result.put("requestSuccessRating", getMapToIntegers(requestSuccessRatingMap));

        return result;
    }

    private int[] getMapToIntegers(Map<String, String> map){
        List<String> list = new ArrayList(map.values());
        int[] ints = list.stream().mapToInt(Integer::valueOf).toArray();
        return ints;
    }

    private void getCount(Map<String, String> map, String period, String requestNum) {
        if(StringUtils.isEmpty(requestNum)){
            requestNum = ""+0;
        }
        if (!map.containsKey(period)) {
            map.put(period, requestNum);
        } else {
            String integer = map.get(period);
            int count = Integer.parseInt(requestNum) + Integer.parseInt(integer);
            map.put(period, ""+count);
        }
    }

    /**
     * 任务监控页面
     * @return
     */
    @RequestMapping(value = "/getAllTaskInPoolMetric", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult getAllTaskInPoolMetric() {
        PageDataResult pdr = taskInPoolMetricService.getAllTaskInPoolMetric();
        return pdr;
    }

    /**
     * 任务失败监控页面 查出数据分析任务失败原因
     * @return
     */
    @RequestMapping(value = "/getTaskExecutedException", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult getTaskExecutedException(@RequestParam("pageNum") Integer pageNum,
                                                   @RequestParam("pageSize") Integer pageSize,
                                                   @RequestParam(required = false) String taskType) {
        if(null == pageNum ) {
            pageNum = 1;
        }
        if(null == pageSize) {
            pageSize = 10;
        }
        PageDataResult pdr = taskExecutedExceptionService.getTaskExecutedException(pageNum, pageSize,taskType);
        return pdr;
    }

    /**
     * 任务失败页面 任务类型下拉选择框
     *
     */
    @RequestMapping(value = "/getFailTaskType")
    @ResponseBody
    public JSONArray getFailTaskType() {
        JSONArray failTaskType = taskExecutedExceptionService.getFailTaskType();
        return failTaskType;
    }

    /**
     * 任务出入池数量查询
     * @return
     */
    @RequestMapping(value = "/getTaskIoMetric", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult getTaskIoMetric() {
        PageDataResult pdr = taskIOMetricService.getTaskIoMetric();
        return pdr;
    }

    /**
     * 分类任务监控
     * @return
     */
    @RequestMapping(value = "/getTaskMetric", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult getTaskMetric() {
        PageDataResult pdr = taskMetricService.getTaskMetric();
        return pdr;
    }

    /**
     * 延迟队列监控
     * @return
     */
    @RequestMapping(value = "/getDelayQueue", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult getDelayQueue() {
        String listStr = remotePushTaskUpdate.getDelayQueue();
        listStr = listStr.replace("[{", "");
        listStr = listStr.replace("}]","");
        //System.out.println(listStr);
        String[] split = listStr.split("},\\{");
        List<DelayQueueInfo> list = new ArrayList<>();
        for (String str : split){
            DelayQueueInfo delayQueueInfo = new DelayQueueInfo();
            String[] split1 = str.split(",");
            for (int i = 0; i <split1.length ; i++) {
                String[] split2 = split1[i].split(":");
                if (i == 0){
                    String substring = split2[1].substring(1);
                    substring = substring.substring(0,substring.length()-1);
                    delayQueueInfo.setSystemOrTask(substring);
                }else if (i == 1){
                    delayQueueInfo.setOneMinuteDelay(Integer.parseInt(split2[1]));
                }else if (i == 2){
                    delayQueueInfo.setTenMinuteDelay(Integer.parseInt(split2[1]));
                }else if (i == 3){
                    delayQueueInfo.setFortyMinuteDelay(Integer.parseInt(split2[1]));
                }else if (i == 4){
                    delayQueueInfo.setTwoHourDelay(Integer.parseInt(split2[1]));
                }else if (i == 5){
                    delayQueueInfo.setTwentyTwoHourDelay(Integer.parseInt(split2[1]));
                }

            }
            list.add(delayQueueInfo);
        }
        PageDataResult pdr = new PageDataResult();
        pdr.setList(list);
        return pdr;
    }

    /**
     * 失效代理IP详情
     * @return
     */
    @RequestMapping(value = "/getInvalidIp", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult getInvalidIp() {
        PageDataResult pdr = new PageDataResult();
        List<ProxyIp> proxyIpAll = proxyIpService.getProxyIpAll();
        List<StoreAccount> storeAccountList = new ArrayList<>();
        for (ProxyIp proxyIp : proxyIpAll) {
            if (proxyIp.getValidStatus() == 0) {
                List<StoreAccount> storeAccountByIp = storeAccountDao.getStoreAccountByIp(proxyIp.getIp());
                if (storeAccountByIp.size() != 0) {
                    for (StoreAccount storeAccount : storeAccountByIp) {
                        StoreAccount storeAccount1 = new StoreAccount();
                        storeAccount1.setProxyIp(proxyIp.getIp());
                        storeAccount1.setAccount(storeAccount.getAccount());
                        storeAccount1.setContinents(storeAccount.getContinents());
                        storeAccount1.setPlatform(storeAccount.getPlatform());
                        storeAccount1.setProxyIpPort(proxyIp.getPort());
                        storeAccountList.add(storeAccount1);
                    }
                }
            }
        }
        for (ProxyIp proxyIp : proxyIpAll) {
            if (proxyIp.getValidStatus() == 0) {
                List<StoreAccount> storeAccountByIp = storeAccountDao.getStoreAccountByIp(proxyIp.getIp());
                if (storeAccountByIp.size() == 0) {
                    StoreAccount storeAccount = new StoreAccount();
                    storeAccount.setProxyIp(proxyIp.getIp());
                    storeAccount.setProxyIpPort(proxyIp.getPort());
                    storeAccountList.add(storeAccount);
                }
            }
        }
        if (storeAccountList.size()!=0){
            pdr.setList(storeAccountList);
        }
        return pdr;
    }

    /**
     * 亚马逊失效代理IP详情
     * @return
     */
    @RequestMapping(value = "/getAmazonInvalidIp", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult getAmazonInvalidIp() {
        PageDataResult pdr = new PageDataResult();
        List<ProxyIp> proxyIpAll = proxyIpService.getProxyIpAll();
        List<StoreAccount> storeAccountList = new ArrayList<>();
        for (ProxyIp proxyIp : proxyIpAll) {
            //找出所有代理IP中状态为0的 判断是否是对应亚马逊店铺
            if (proxyIp.getValidStatus() == 0) {
                //判断是否为亚马逊代理IP的方法就是去atc_account表中查找代理IP是否有对应店铺
                List<StoreAccount> storeAccountByIp = storeAccountDao.getStoreAccountByIp(proxyIp.getIp());
                if (storeAccountByIp.size() != 0) {
                    for (StoreAccount storeAccount : storeAccountByIp) {
                        StoreAccount storeAccount1 = new StoreAccount();
                        storeAccount1.setProxyIp(proxyIp.getIp());
                        storeAccount1.setAccount(storeAccount.getAccount());
                        storeAccount1.setContinents(storeAccount.getContinents());
                        storeAccount1.setPlatform(storeAccount.getPlatform());
                        storeAccount1.setProxyIpPort(proxyIp.getPort());
                        storeAccountList.add(storeAccount1);
                    }
                }
            }
        }
        if (storeAccountList.size()!=0){
            pdr.setList(storeAccountList);
        }
        return pdr;
    }

    /**
     * AmazonDaemon代理IP监控
     * @param beginTime
     * @param endTime
     * @param workType
     * @return
     */
    @RequestMapping(value = "/getProxyMonitor", method = RequestMethod.POST)
    @ResponseBody
    public Object getProxyMonitor(@RequestParam("barType") Integer barType,
                                          @RequestParam("beginTime") String beginTime,@RequestParam("endTime") String endTime,@RequestParam("workType") String workType) {
        if (StringUtils.isEmpty(beginTime) && StringUtils.isEmpty(endTime)){
            beginTime = DateUtils.getHourBeforeDate(1);
            endTime = DateUtils.getCurrentDate();
        }
        if (barType == null) {
            barType = 1;
        }

        LinkedHashMap<String,LinkedList> linkedHashMap = new LinkedHashMap<>();
        LinkedList ipList = new LinkedList();
        LinkedList succeedNumList = new LinkedList();
        LinkedList bannedNumList = new LinkedList();
        LinkedList bannedRateList = new LinkedList();
        switch (barType){
            case 2:
                List<Map> maps = proxyRequestLogsService.sumGroupByProxyId(beginTime, endTime, workType);
                getTopBannedRateMonitor(maps,ipList,succeedNumList,bannedNumList,bannedRateList);
                break;
            default:
                List<Map> UniformMaps = proxyRequestLogsService.sumGroupByProxyIdByWorkType(beginTime, endTime, workType);
                getUniformMonitor(UniformMaps,ipList,succeedNumList,bannedNumList,bannedRateList);
        }

        linkedHashMap.put("ipList",ipList);
        linkedHashMap.put("succeedNumList",succeedNumList);
        linkedHashMap.put("bannedNumList",bannedNumList);

        linkedHashMap.put("bannedRateList",bannedRateList);
        return linkedHashMap;
    }

    /**
     * AmazonDaemon代理IP监控 top20
     * @param maps
     * @param ipList
     * @param succeedNumList
     * @param bannedNumList
     * @param bannedRateList
     */
    private void getTopBannedRateMonitor(List<Map> maps, LinkedList ipList, LinkedList succeedNumList,
                                         LinkedList bannedNumList, LinkedList bannedRateList){
        for (Map map:maps) {
            String ip = map.get("proxy_id") + "-" + map.get("proxy_ip") + ":" + map.get("port");

            int succeedNum = Integer.parseInt(String.valueOf(map.get("succeed_num")));

            ipList.add(ip);
            succeedNumList.add(succeedNum);
            bannedNumList.add(map.get("banned_num"));
            Object ob = map.get("banned_rate");

            float bannedRate = Float.parseFloat(String.valueOf(ob));
            bannedRateList.add(bannedRate*100);
        }
    }

    /**
     * 均匀分布场景下的各个IP监控
     * @param maps
     * @param ipList
     * @param succeedNumList
     * @param bannedNumList
     * @param bannedRateList
     */
    private void getUniformMonitor(List<Map> maps, LinkedList ipList, LinkedList succeedNumList,
                                         LinkedList bannedNumList, LinkedList bannedRateList){
        int size = maps.size();
        int pointNum = 50;
        int interval = size / pointNum;
        int i = 0;
        int startPoint = 1;
        for (Map map:maps) {
            i++;
            if (i != 1 && i < startPoint+interval){
                continue;
            }
            startPoint = i;
            String ip = map.get("proxy_id") + "-" + map.get("proxy_ip") + ":" + map.get("port");

            int succeedNum = Integer.parseInt(String.valueOf(map.get("succeed_num")));

            ipList.add(ip);
            succeedNumList.add(succeedNum);
            bannedNumList.add(map.get("banned_num"));

            String divide = CommonUtils.divide(Integer.parseInt(String.valueOf(map.get("banned_num"))), Integer.parseInt(String.valueOf(map.get("used_num"))));

            float bannedRate = Float.parseFloat(divide);
            bannedRateList.add(bannedRate*100);
        }
    }


    @RequestMapping(value = "/getTimeBlockByProxy", method = RequestMethod.POST)
    @ResponseBody
    public Object getTimeBlockByProxy(@RequestParam("beginTime") String beginTime,@RequestParam("endTime") String endTime,
                                      @RequestParam("workType") String workType,@RequestParam("proxyId") Integer proxyId){
        if (StringUtils.isEmpty(beginTime) && StringUtils.isEmpty(endTime)){
            beginTime = DateUtils.getHourBeforeDate(1);
            endTime = DateUtils.getCurrentDate();
        }
        LinkedHashMap<String,LinkedList> linkedHashMap = new LinkedHashMap<>();
        if (proxyId == null){
            ProxyIp proxyIp = proxyIpService.getFirstProxyByPlatform("7");
            proxyId = proxyIp.getId();
        }
        List<Map> maps = proxyRequestLogsService.sumGroupByTimeBlock(beginTime, endTime, workType,proxyId);

        LinkedList timeList = new LinkedList();
        LinkedList succeedNumList = new LinkedList();
        LinkedList bannedNumList = new LinkedList();
        LinkedList bannedRateList = new LinkedList();

        String beforeDate = "";
        int beforeHour = -1;
        for (Map map:maps) {
            int succeedNum = Integer.parseInt(String.valueOf(map.get("succeed_num")));

            String timeBlock = String.valueOf(map.get("time_block"));
            String[] split = timeBlock.split(" ");
            String date = split[0];
            int hour = Integer.parseInt(split[1]);
            //时间段没有的置为0
            if (!StringUtils.isEmpty(beforeDate)){
                if (beforeDate.equals(date) && beforeHour != hour) {
                    for (int i = beforeHour + 1; i < hour; i++) {
                        timeList.add(date + " " + i);
                        succeedNumList.add(0);
                        bannedNumList.add(0);
                        bannedRateList.add(0);
                    }
                } else {
                    for (int i = beforeHour + 1; i < 24; i++) {
                        timeList.add(date + " " + i);
                        succeedNumList.add(0);
                        bannedNumList.add(0);
                        bannedRateList.add(0);
                    }
                    for (int j = 0; j < hour; j++) {
                        timeList.add(date + " " + j);
                        succeedNumList.add(0);
                        bannedNumList.add(0);
                        bannedRateList.add(0);
                    }
                }
            }

            beforeDate = split[0];
            beforeHour = hour;

            timeList.add(timeBlock);
            succeedNumList.add(succeedNum);
            bannedNumList.add(map.get("banned_num"));

            String divide = CommonUtils.divide(Integer.parseInt(String.valueOf(map.get("banned_num"))), Integer.parseInt(String.valueOf(map.get("used_num"))));

            float bannedRate = Float.parseFloat(divide);
            bannedRateList.add(bannedRate*100);
        }
        linkedHashMap.put("timeList",timeList);
        linkedHashMap.put("succeedNumList",succeedNumList);
        linkedHashMap.put("bannedNumList",bannedNumList);

        linkedHashMap.put("bannedRateList",bannedRateList);
        return linkedHashMap;
    }

    @RequestMapping(value = "/getProxyScatter", method = RequestMethod.POST)
    @ResponseBody
    public Object getProxyScatter(@RequestParam("timePoint") String timePoint,@RequestParam("workType") String workType) throws ParseException {

        if (StringUtils.isEmpty(timePoint)){
            timePoint = DateUtils.getCurrentDate();
        }
        String beginTime = DateUtils.getHourBeforeDate(timePoint, 24);
        List<Map<String, String>> maps = proxyTrendService.listProxySituation(beginTime, timePoint, workType);
        LinkedList ipList = new LinkedList();
        LinkedList openTimestampList = new LinkedList();
        int size = maps.size();
        int pointNum = 50;
        int interval = size / pointNum;
        int i = 0;
        int startPoint = 1;
        for (Map<String, String> map:maps) {
            i++;
            if (i != 1 && i < startPoint + interval) {
                continue;
            }
            startPoint = i;
            String proxyIp = map.get("proxy_ip");

            String openTime = map.get("open_time");
            openTimestampList.add(openTime);
            String[] split = String.valueOf(proxyIp).split(":");
            String ip;
            if (split.length == 2){
                ip = proxyIp;
            }else {
                String[] split1 = split[1].split("-");
                ip = split1[6] + ":" + split[3];
            }
            ipList.add(ip);
        }

        LinkedHashMap<String,LinkedList> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("ipList",ipList);
        linkedHashMap.put("openTimestampList",openTimestampList);
        return linkedHashMap;
    }

    @RequestMapping(value = "/getProxyInterval", method = RequestMethod.POST)
    @ResponseBody
    public Object getProxyInterval(@RequestParam("timePoint") String timePoint,@RequestParam("workType") String workType) throws ParseException {
        Date endDate;
        if (StringUtils.isEmpty(timePoint)){
            endDate = DateUtils.getCurrentDateToDate();
            timePoint = DateUtils.getCurrentDate();
        }else {
            endDate = DateUtils.convertDate(timePoint);
        }

        String beginTime = DateUtils.getHourBeforeDate(timePoint, 24);
        Date beginDate = DateUtils.convertDate(beginTime);
        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");
        String timeShort = dateFormat2.format(endDate);
        StringTokenizer st = new StringTokenizer(timeShort, ":");
        List<String> inTime = new ArrayList<String>();
        while (st.hasMoreElements()) {
            inTime.add(st.nextToken());
        }
        int hour = Integer.parseInt(inTime.get(0));
        String beginDateString = dateFormat1.format(beginDate);
        String endDateString = dateFormat1.format(endDate);
        int flag = hour + 1;
        String date = beginDateString;

        LinkedList timeList = new LinkedList();
        LinkedList validNumList = new LinkedList();
        LinkedList invalidNumList = new LinkedList();
        while (flag != hour){
            flag++;
            if (flag == 24){
                flag = 0;
                date = endDateString;
            }
            String time = date + " " + flag+":00:00";
            int validNum = proxyTrendService.countValidProxy(DateUtils.getHourBeforeDate(time, 24),time,workType);
            int invalidNum = proxyTrendService.countInvalidProxy(DateUtils.getHourBeforeDate(time, 24),time,workType);
            timeList.add(time);
            validNumList.add(validNum);
            invalidNumList.add(invalidNum);

        }
        LinkedHashMap<String,LinkedList> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("timeList",timeList);
        linkedHashMap.put("validNumList",validNumList);
        linkedHashMap.put("invalidNumList",invalidNumList);
        return linkedHashMap;
    }

}
