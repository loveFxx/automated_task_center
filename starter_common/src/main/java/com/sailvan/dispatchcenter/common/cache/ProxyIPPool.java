package com.sailvan.dispatchcenter.common.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.sailvan.dispatchcenter.common.domain.AccountProxy;
import com.sailvan.dispatchcenter.common.domain.Platform;
import com.sailvan.dispatchcenter.common.domain.ProxyIp;
import com.sailvan.dispatchcenter.common.pipe.AccountProxyService;
import com.sailvan.dispatchcenter.common.pipe.PlatformService;
import com.sailvan.dispatchcenter.common.pipe.ProxyIpService;
import com.sailvan.dispatchcenter.common.pipe.ProxyTrendService;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.math.BigInteger;
import java.util.*;

@Component
public class ProxyIPPool {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    ProxyIpService proxyIpService;

    @Autowired
    PlatformService platformService;

    @Autowired
    AccountProxyService accountProxyService;

    @Autowired
    InitPlatformCache initPlatformCache;

    @Autowired
    ProxyTrendService proxyTrendService;

    String prefix = "proxyPool:";

    String PLATFORM_PREFIX = "platform:platformCache:";


    /**
     * 入redis队列
     * @param largeTaskType 大类型
     * @param timestamp 时间戳-毫秒级
     * @param proxyId 代理IP主键ID
     */
    public void addQueue(String largeTaskType, long timestamp,int proxyId){
        String key = prefix+largeTaskType;
        redisUtils.add(key,proxyId,timestamp);
    }

    /**
     * 出redis队列
     * @param largeTaskType 大类型
     * @return 代理IP主键ID
     */
    public int popQueue(String largeTaskType){
        synchronized (largeTaskType.intern()){
            String key = prefix+largeTaskType;

            Set range = redisUtils.rangeWithScores(key, 0, 0);

            if (range != null && !range.isEmpty()) {
                DefaultTypedTuple next = (DefaultTypedTuple) range.iterator().next();
                Double score = next.getScore();
                Object value = next.getValue();

                if (score != null && System.currentTimeMillis() > score){
                    int proxyId = Integer.parseInt(String.valueOf(value));
                    long l = System.currentTimeMillis();
                    //取代理IP的日志
                    proxyTrendService.insertProxyTrend(proxyId,largeTaskType,1,null);
                    //重置代理IP开放时间的日志
                    proxyTrendService.insertProxyTrend(proxyId,largeTaskType,0, DateUtils.convertTimestampToDate(BigInteger.valueOf(l)));
                    redisUtils.add(key,value,l);
                    return proxyId;
                }
            }
        }

        return 0;
    }

    /**
     * 禁用代理IP时频率控制逻辑
     * @param largeWorkType 大类型
     * @param proxyId 代理IP主键ID
     */
    @Deprecated
    public void updateLastBannedTime(String largeWorkType,int proxyId){

        //若是没有达到禁用次数的条件，将时间戳调整为当前时间戳
        long timestamp = System.currentTimeMillis();
        long l = System.currentTimeMillis();
        Platform platform = platformService.getPlatformByName(largeWorkType);

        if (!StringUtils.isEmpty(platform.getConfig())){
            //config key必须由小到大
            JSONObject config = JSON.parseObject(platform.getConfig(),Feature.OrderedField);
            HashMap<String,Object> map = new HashMap<>();
            int currentBannedTimes = 0;
            long currentBannedTime;
            Iterator iter = config.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String,JSONObject> entry = (Map.Entry<String,JSONObject>) iter.next();
                String key = prefix + largeWorkType+":"+proxyId+":"+entry.getKey();
                Integer maxBannedTimes = entry.getValue().getInteger("max_banned_times");
                Long delayTime = entry.getValue().getLong("delay_time")*1000;
                //在一定时间内达到次数上限将banned_times清0，并且当时间戳超过规定时间时，重置为当前时间戳
                if (redisUtils.exists(key)){
                    Object o = redisUtils.get(key);
                    JSONObject redisConfig = JSON.parseObject(String.valueOf(o));

                    Integer bannedTimes = redisConfig.getInteger("banned_times");
                    Long lastBannedTime = redisConfig.getLong("last_banned_time");

                    //在一定时间内达到次数上限，清0，更新为当前时间戳，否则+1不更新时间戳
                    if (maxBannedTimes <= bannedTimes+1){
                        timestamp = l+delayTime;
                        currentBannedTimes = 0;
                        currentBannedTime = l;
                    }else {
                        currentBannedTimes = bannedTimes+1;
                        currentBannedTime = lastBannedTime;
                    }
                    //当存储的时间戳+key时间戳小于当前时间戳，更新为当前时间戳，并且清0，注：不在规定时间内的重置操作

                    if ((lastBannedTime+Long.parseLong(entry.getKey())*1000) < l){
                        currentBannedTimes = 0;
                        currentBannedTime = l;
                    }
                }else {
                    //如果有规定一次为上限的，初始时需要延迟
                    if (maxBannedTimes == 1){
                        timestamp = l+delayTime;
                    }
                    currentBannedTimes = 1;
                    currentBannedTime = l;
                }
                map.put("banned_times",currentBannedTimes);
                map.put("last_banned_time",currentBannedTime);
                redisUtils.put(key,JSON.toJSONString(map),3600*24*2L);
            }

            addQueue(largeWorkType,timestamp,proxyId);
        }
    }

    /**
     * 初始化代理池（只需要第一次初始化，在上传导表时会入池）
     */
    public void initProxyPool() {
        long timestamp = System.currentTimeMillis();
        List<ProxyIp> proxyIpAll = proxyIpService.getProxyIpAll();
        for (ProxyIp proxyIp:proxyIpAll) {
            if (proxyIp.getValidStatus() != 1 || proxyIp.getIsDeleted() == 1){
                continue;
            }
            
            String crawlPlatform = proxyIp.getCrawlPlatform();
            String[] split = crawlPlatform.split(",");
            List<AccountProxy> accountProxies = accountProxyService.getAccountProxyByProxyIpId(proxyIp.getId());
            Set<String> platforms = new HashSet<>();
            if(accountProxies != null && !accountProxies.isEmpty()){
                for (AccountProxy accountProxy : accountProxies) {
                    Platform platform = initPlatformCache.getPlatformCacheByName(accountProxy.getPlatform());
                    platforms.add(String.valueOf(platform.getId()));
                }
            }
            for (String s:split){
                if (platforms.contains(s)) {
                    logger.error("accountProxies:{} ,proxyIp:{}",accountProxies,proxyIp.getIp());
                    continue;
                }
                Object o = redisUtils.get(PLATFORM_PREFIX + s);
                Platform platform = JSONObject.parseObject(String.valueOf(o), Platform.class);
                addQueue(platform.getPlatformName(),timestamp,proxyIp.getId());
            }
        }
    }

    /**
     * 根据代理IP主键ID将所有配置的爬取平台移除
     * @param id 代理IP主键ID
     */
    public void removeProxy(int id){
        ProxyIp proxyIp = proxyIpService.findProxyIpById(id);
        String[] split = proxyIp.getCrawlPlatform().split(",");
        for (String s:split){
            Object o = redisUtils.get(PLATFORM_PREFIX + s);
            Platform platform = JSONObject.parseObject(String.valueOf(o), Platform.class);
            String key = prefix+platform.getPlatformName();
            redisUtils.remove(key,id);
        }
    }

    /**
     * 根据代理IP主键ID将所有配置的爬取平台添入
     * @param id 代理IP主键ID
     */
    public void pushProxy(int id){
        ProxyIp proxyIp = proxyIpService.findProxyIpById(id);
        String[] split = proxyIp.getCrawlPlatform().split(",");
        for (String s:split){
            Object o = redisUtils.get(PLATFORM_PREFIX + s);
            Platform platform = JSONObject.parseObject(String.valueOf(o), Platform.class);
            addQueue(platform.getPlatformName(),System.currentTimeMillis(),proxyIp.getId());
        }
    }
}
