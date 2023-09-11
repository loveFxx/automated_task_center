package com.sailvan.dispatchcenter.data.controller;

import com.google.common.base.Joiner;
import com.sailvan.dispatchcenter.common.cache.InitMachineCache;
import com.sailvan.dispatchcenter.common.cache.ProxyIPPool;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.pipe.*;
import com.sailvan.dispatchcenter.common.util.CommonUtils;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import lombok.SneakyThrows;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 从文件导入解析代理IP
 *
 * @author menghui
 * @date 2021-06
 */
@Controller
public class ImportController {

    private static Logger logger = LoggerFactory.getLogger(ImportController.class);

    @Autowired
    private ImportService importService;

    @Autowired
    private ProxyIpService proxyIpService;

    @Autowired
    StoreAccountService storeAccountService;

    @Autowired
    ProxyIpShopService proxyIpShopService;

    @Autowired
    AccountProxyService accountProxyService;

    @Autowired
    MachineWorkTypeService machineWorkTypeService;

    @Autowired
    InitMachineCache initMachineCache;



    @Autowired
    RedisUtils redisUtils;

    @Autowired
    MachineService machineService;

    @Autowired
    PlatformService platformService;

    @Autowired
    ProxyIpPlatformService proxyIpPlatformService;

    @Autowired
    ProxyIPPool proxyIPPool;

    /**
     * 不是以下几种的 平台(platform)、账号(account)、大洲 都是空
     */
    final static String[] PLATFORMS = {"亚马逊", "沃尔玛", "Facebook", "速卖通"};

    /**
     * 只挂平台 (包含亚马逊中东)。账号(account)、大洲 都是空
     */
    final static String[] PLATFORMS_PLATFORM = {"沃尔玛", "Facebook", "速卖通"};
    final static String MIDDLE_EAST = "中东";

    /**
     * 亚马逊平台所有的都不是空(除去亚马逊中东)
     */
    final static String AMAZON_PLATFORM = "亚马逊";


    /**
     * 新增的导入代理IP文件
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/upload")
    @ResponseBody
    public Map<String,Object> uploadExcel(HttpServletRequest request) throws Exception {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultiValueMap<String, MultipartFile> multiFileMap = multipartRequest.getMultiFileMap();
        MultipartFile file = null;
        String content = "content";
        if (multiFileMap.containsKey(content)) {
            file = multiFileMap.getFirst(content);
        }
        Map<String,Object> map = new HashMap<>();
        if (file == null || file.isEmpty()) {
            map.put("code",0);
            map.put("msg","空文件");
            return map;
        }
        InputStream inputStream = file.getInputStream();
        List<List<Object>> list = importService.parseExcel(inputStream, file.getOriginalFilename());
        inputStream.close();

        List<ProxyIp> ipLists = new ArrayList<>();
        insertProxyIp(list);
        System.out.println(list.size() + "  insert success:" + ipLists.size());
        map.put("code",1);
        map.put("msg","上传成功");
        return map;
    }

    /**
     * row--ip或者域名，端口号，可爬取平台（all表示所有可爬，多个平台以英文逗号分隔），服务商
     * @param rowLists
     */
    private void insertProxyIp(List<List<Object>> rowLists){
        for (List<Object> row : rowLists){
            if (row.size() == 4){
                List<String> platformList = new ArrayList<>();
                HashMap schema = new HashMap();
                String router = String.valueOf(row.get(0)); //ip或域名
                int port = Integer.parseInt(String.valueOf(row.get(1)));
                String platforms = String.valueOf(row.get(2));
                String isp = String.valueOf(row.get(3));
//                String expireTime = String.valueOf(row.get(4));
                ProxyIp proxyIp = new ProxyIp();
                proxyIp.setIp(router);
                proxyIp.setPort(port);
                proxyIp.setCrawlPlatform(platformsSchema(platforms,platformList,schema));
                proxyIp.setValidStatus(1);
                proxyIp.setIsp(isp);

                //过期时间
//                Calendar calendar = new GregorianCalendar(1900,0,-1);
//                int intDay = Integer.parseInt(expireTime);
//                Date dd = DateUtils.addDays(calendar.getTime(),intDay);
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                proxyIp.setExpireTime(simpleDateFormat.format(dd));

                int id = 0;
                ProxyIp proxyIpByUniqueKey = proxyIpService.getProxyIpByUniqueKey(router, port);
                if (proxyIpByUniqueKey == null){
                    proxyIpService.insert(proxyIp);
                    id = proxyIp.getId();
                }else {
                    proxyIp.setId(proxyIpByUniqueKey.getId());
                    proxyIpService.update(proxyIp);
                    id = proxyIpByUniqueKey.getId();
                }

                proxyIpPlatformService.deleteByProxyId(id);
                for (String o:platformList){
                    ProxyIpPlatform proxyIpPlatform = new ProxyIpPlatform();
                    proxyIpPlatform.setProxyIpId(id);
                    proxyIpPlatform.setPlatform(o);
                    proxyIpPlatform.setStatus(1);

                    proxyIpPlatformService.insert(proxyIpPlatform);
                    proxyIPPool.addQueue(o,System.currentTimeMillis(),id);
                }

            }
        }
    }

    /**
     *
     * @param platforms  以逗号分隔的平台名
     * @param platformList 平台名List
     * @param schema MAP映射：平台名-->平台ID 所有的
     * @return 以逗号分隔的平台ID
     */
    private String platformsSchema(String platforms,List<String> platformList,HashMap schema){
        List<Platform> platformAll = platformService.getPlatformAll();

        for (Platform platform:platformAll){
            schema.put(platform.getPlatformName(),platform.getId());
        }
        List list = new ArrayList();
        if (platforms.equals("all")){
            for (Platform platform:platformAll){
                list.add(platform.getId());
                platformList.add(platform.getPlatformName());
            }
        }else {
            String[] split = platforms.split(",");
            for (String platform: split){
                list.add(schema.get(platform));
                platformList.add(platform);
            }
        }
        return Joiner.on(",").join(list);
    }

    @RequestMapping(value = "/initProxyPool")
    @ResponseBody
    public String initProxyPool() {
        proxyIPPool.initProxyPool();
        return "SUCCESS";
    }


    @PostMapping(value = "/uploadPort")
    @ResponseBody
    public Map<String,Object> uploadPort(HttpServletRequest request) throws Exception {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultiValueMap<String, MultipartFile> multiFileMap = multipartRequest.getMultiFileMap();
        MultipartFile file = null;
        String content = "content";
        if (multiFileMap.containsKey(content)) {
            file = multiFileMap.getFirst(content);
        }
        Map<String,Object> mapResult = new HashMap<>();
        if (file == null || file.isEmpty()) {
            mapResult.put("code",0);
            mapResult.put("msg","空文件");
            return mapResult;
        }
        InputStream inputStream = file.getInputStream();
        List<List<Object>> list = importService.getBankListByExcel(inputStream, file.getOriginalFilename());
        inputStream.close();

        List<ProxyIp> proxyIpAll = proxyIpService.getProxyIpAll();
        Map<String, Integer> map = new HashMap<>();
        for (ProxyIp proxyIp : proxyIpAll) {
            if (map.containsKey(proxyIp.getIp())) {
                logger.error("repeat proxyIp {}", proxyIp);
            } else {
                map.put(proxyIp.getIp(), proxyIp.getId());
            }
        }
        updatePort(map, list);

        List<StoreAccount> storeAccountAll = storeAccountService.getStoreAccountAll();

        Map<String, List<StoreAccount>> storeAccountMap = new HashMap<>();
        for (StoreAccount storeAccount : storeAccountAll) {
            if (StringUtils.isEmpty(storeAccount.getProxyIp())) {
                continue;
            }
            List<StoreAccount> list1 = new ArrayList<>();
            if (storeAccountMap.containsKey(storeAccount.getProxyIp())) {
                list1 = storeAccountMap.get(storeAccount.getProxyIp());
            }
            list1.add(storeAccount);
            storeAccountMap.put(storeAccount.getProxyIp(), list1);

        }

        updateAccountPort(storeAccountMap, list);

        mapResult.put("code",1);
        mapResult.put("msg","上传成功");
        return mapResult;
    }

    @PostMapping(value = "/uploadUserPwd")
    @ResponseBody
    public Map<String,Object> uploadUserPwd(HttpServletRequest request) throws Exception {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultiValueMap<String, MultipartFile> multiFileMap = multipartRequest.getMultiFileMap();
        MultipartFile file = null;
        String content = "content";
        if (multiFileMap.containsKey(content)) {
            file = multiFileMap.getFirst(content);
        }
        Map<String,Object> map = new HashMap<>();
        if (file == null || file.isEmpty()) {
            map.put("code",0);
            map.put("msg","空文件");
            return map;
        }
        InputStream inputStream = file.getInputStream();
        List<List<Object>> list = importService.getBankListByExcel(inputStream, file.getOriginalFilename());
        inputStream.close();

        for (int i = 0; i < list.size(); i++) {
            List<Object> lo = list.get(i);
            if (lo.size() == 3) {
                if (StringUtils.isEmpty(lo.get(0)) ){
                    logger.error(" ip: {} null",lo.get(0));
                }
                String ip = String.valueOf(lo.get(0));
                Machine machineByIP = machineService.getMachineByIP(ip);
                if(machineByIP == null){
                    logger.error(" machineByIP ip: {} null",ip);
                    continue;
                }
                String password = String.valueOf(lo.get(1));
                machineService.updateMachineUserPwd(machineByIP.getId(), String.valueOf(lo.get(2)), String.valueOf(lo.get(1)));

                logger.info("  ip {}, password {} ",ip, password);
            }

        }
        map.put("code",1);
        map.put("msg","上传成功");
        return map;
    }

    @PostMapping(value = "/refreshValidMachine")
    @ResponseBody
    public Map<String,Object> refreshValidMachine(HttpServletRequest request) throws Exception {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultiValueMap<String, MultipartFile> multiFileMap = multipartRequest.getMultiFileMap();
        MultipartFile file = null;
        String content = "content";
        if (multiFileMap.containsKey(content)) {
            file = multiFileMap.getFirst(content);
        }
        Map<String,Object> map = new HashMap<>();
        if (file == null || file.isEmpty()) {
            map.put("code",0);
            map.put("msg","空文件");
            return map;
        }
        InputStream inputStream = file.getInputStream();
        List<List<Object>> list = importService.getBankListByExcel(inputStream, file.getOriginalFilename());
        inputStream.close();
        Set<String> set = new HashSet<>();

        List<String> list1 = new ArrayList<>();
        boolean flag = false;
        for (int i = 0; i < list.size(); i++) {
            List<Object> lo = list.get(i);
            if (lo.size() == 3) {
                if(StringUtils.isEmpty(lo.get(0)) || StringUtils.isEmpty(lo.get(1)) || StringUtils.isEmpty(lo.get(2))){
                    logger.info(" account:{}, ip:{}, continent:{} is null",lo.get(0),lo.get(1),lo.get(2));
                    continue;
                }
                String account = String.valueOf(lo.get(0));
                String ip = String.valueOf(lo.get(1));
                String continents = String.valueOf(lo.get(2));
                String key = account+"_"+ip+"_"+continents;
                if(list1.contains(key)){
                   continue;
                }
                list1.add(key);
                List<MachineWorkType> machineWorkTypeByIp = machineWorkTypeService.getMachineWorkTypeByIp(ip);
                if(machineWorkTypeByIp == null || machineWorkTypeByIp.isEmpty()){
                    continue;
                }
                if(!set.contains(ip)){
                    set.add(ip);
                    machineWorkTypeService.updateMachineWorkTypeStatusByIp(ip,Constant.STATUS_INVALID,Constant.STATUS_IS_UPDATE);
                }

                for (MachineWorkType machineWorkType : machineWorkTypeByIp) {
                    if(machineWorkType.getPlatformType() == Constant.LARGE_TASK_TYPE_CRAWL_PLATFORM){
                        continue;
                    }
                    String account1 = machineWorkType.getAccount();
                    String continents1 = machineWorkType.getContinents();
                    if(account.equals(account1) && continents.equals(continents1)){
                        machineWorkTypeService.updatePlatformTypeIsUpdateStatusById(machineWorkType.getId(),Constant.STATUS_VALID,Constant.STATUS_IS_UPDATE_RESET);
                        logger.info(" machineWorkType:{} update success",machineWorkType);
                        flag = true;
                    }
                }

            }

        }
        if(flag){
            initMachineCache.updateMachineCacheMap(null);
        }
        map.put("code",1);
        map.put("msg","上传成功");
        return map;
    }


    @PostMapping(value = "/clientCheckScreen")
    @ResponseBody
    public Map<String,Object> clientCheckScreen(HttpServletRequest request) throws Exception {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultiValueMap<String, MultipartFile> multiFileMap = multipartRequest.getMultiFileMap();
        MultipartFile file = null;
        String content = "content";
        if (multiFileMap.containsKey(content)) {
            file = multiFileMap.getFirst(content);
        }
        Map<String,Object> map = new HashMap<>();
        if (file == null || file.isEmpty()) {
            map.put("code",0);
            map.put("msg","空文件");
            return map;
        }
        InputStream inputStream = file.getInputStream();
        List<List<Object>> list = importService.getBankListByExcel(inputStream, file.getOriginalFilename());
        inputStream.close();
        Set<String> set = new HashSet<>();

        List<String> vaildList = new ArrayList<>();
        boolean flag = false;
        for (int i = 0; i < list.size(); i++) {
            List<Object> lo = list.get(i);
            if (lo.size() == 1) {
                String ip = String.valueOf(lo.get(0));
                if(StringUtils.isEmpty(ip)){
                    continue;
                }
                if (!vaildList.contains(ip)) {
                    vaildList.add(ip);
                }
            }
        }
        if(vaildList.size() < 1){
            redisUtils.remove("clientCheckScreen");
            map.put("code",1);
            map.put("msg","清空成功");
            return map;
        }
        String joinStr = String.join(",", vaildList);
        logger.info("clientCheckScreen:"+joinStr);
        redisUtils.put("clientCheckScreen",joinStr,12*3600L);
        map.put("code",1);
        map.put("msg","上传成功");
        return map;
    }


    @PostMapping(value = "/offInValidMachine")
    @ResponseBody
    public Map<String,Object> offInValidMachine(HttpServletRequest request) throws Exception {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultiValueMap<String, MultipartFile> multiFileMap = multipartRequest.getMultiFileMap();
        MultipartFile file = null;
        String content = "content";
        if (multiFileMap.containsKey(content)) {
            file = multiFileMap.getFirst(content);
        }
        Map<String,Object> map = new HashMap<>();
        if (file == null || file.isEmpty()) {
            map.put("code",0);
            map.put("msg","空文件");
            return map;
        }
        InputStream inputStream = file.getInputStream();
        List<List<Object>> list = importService.getBankListByExcel(inputStream, file.getOriginalFilename());
        inputStream.close();
        Set<String> set = new HashSet<>();

        List<String> vaildList = new ArrayList<>();
        boolean flag = false;
        for (int i = 0; i < list.size(); i++) {
            List<Object> lo = list.get(i);
            if (lo.size() == 3) {
                String ip = String.valueOf(lo.get(2));
                if(StringUtils.isEmpty(ip)){
                    continue;
                }
                if (!vaildList.contains(ip)) {
                    vaildList.add(ip);
                }
            }
        }
        if(vaildList.size() < 1000){
            map.put("code",1);
            map.put("msg","数量太少");
            return map;
        }
        List<Integer> addList = new ArrayList<>();
        List<Machine> machineAll = machineService.getMachineAll();
        for (Machine machine : machineAll) {
            String ip = machine.getIp();
            if(StringUtils.isEmpty(ip)){
                continue;
            }
            if(vaildList.contains(ip) && machine.getStatus() == Constant.STATUS_INVALID){
                if(!addList.contains(ip)){
                    addList.add(machine.getId());
                }
            }

        }
        List<MachineWorkType> machineWorkTypeByIp = machineWorkTypeService.getMachineWorkTypeByPlatformType(Constant.LARGE_TASK_TYPE_ACCOUNT_PLATFORM);

        List<Integer> removeList = new ArrayList<>();


        for (MachineWorkType machineWorkType : machineWorkTypeByIp) {
            if(machineWorkType.getPlatformType() == Constant.LARGE_TASK_TYPE_CRAWL_PLATFORM){
                continue;
            }
            String machineIp = machineWorkType.getMachineIp();
            if(StringUtils.isEmpty(machineIp)){
                continue;
            }
            if(!removeList.contains(machineIp) && !vaildList.contains(machineIp)){
                removeList.add(machineWorkType.getMachineId());
                logger.info("offInValidMachine machineIp:{} is need remove", machineIp);
            }
        }

        if(!removeList.isEmpty()){
            for (Integer integer : removeList) {
                machineService.updateStatus(integer, Constant.STATUS_INVALID);
                flag = true;
            }
        }
        if(!addList.isEmpty()){
            for (Integer integer : addList) {
                machineService.updateStatus(integer, Constant.STATUS_VALID);
                flag = true;
            }
        }
        if(flag){
            initMachineCache.init();
        }

        map.put("code",1);
        map.put("msg","上传成功");
        return map;
    }


    private void updateAccountPort(Map<String, List<StoreAccount>> storeAccountMap, List<List<Object>> list) {
        for (int i = 0; i < list.size(); i++) {
            List<Object> lo = list.get(i);
            if (lo.size() == 3) {
                String ip = String.valueOf(lo.get(1));
                int port = Integer.parseInt(String.valueOf(lo.get(2)));
                if (!storeAccountMap.keySet().contains(ip)) {
                    logger.error("not exist ip {}", ip);
                    continue;
                }
                if (StringUtils.isEmpty(port)) {
                    logger.error("ip {}, port is null {}", ip, port);
                    continue;
                }
                List<StoreAccount> list1 = storeAccountMap.get(ip);
                for (StoreAccount storeAccount : list1) {
                    storeAccount.setProxyIpPort(port);
                    storeAccountService.updateProxyIpById(storeAccount);
                    // 导入代理IP时,更新账号表的代理IP和端口 需要重置缓存
                    String key = Constant.PROXY_IP_ACCOUNT_CONTINENTS_PREFIX+storeAccount.getAccount()+"_"+storeAccount.getContinents();
                    redisUtils.put(key,ip+":"+port,Long.valueOf(3600*12));
                    logger.info("update port  success id {}, ip {}, port {} ",storeAccount.getId(), ip, port);
                }

            }

        }

    }


    private void updatePort(Map<String, Integer> map, List<List<Object>> list) {
        for (int i = 0; i < list.size(); i++) {
            List<Object> lo = list.get(i);
            if (lo.size() == 3) {
                String ip = String.valueOf(lo.get(1));
                int port = Integer.parseInt(String.valueOf(lo.get(2)));
                if (!map.keySet().contains(ip)) {
                    logger.error("not exist ip {}", ip);
                    continue;
                }
                if (StringUtils.isEmpty(port)) {
                    logger.error("ip {}, port is null {}", ip, port);
                    continue;
                }
                proxyIpService.updateProxyIpPort(map.get(ip), port);
                logger.info("update port  success id {}, ip {}, port {} ",map.get(ip), ip, port);
            }

        }

    }

}
