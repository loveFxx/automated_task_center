package com.sailvan.dispatchcenter.stat.monitor.scheduler;

import com.sailvan.dispatchcenter.common.util.ExcelUtils;
import com.sailvan.dispatchcenter.db.dao.automated.MachineDao;
import com.sailvan.dispatchcenter.db.dao.automated.MachineWorkTypeDao;
import com.sailvan.dispatchcenter.db.dao.automated.TaskLogsDao;
import com.sailvan.dispatchcenter.stat.monitor.config.WeChatRobotTokenConfig;
import com.sailvan.dispatchcenter.stat.monitor.statistics.TaskLogStat;
import com.sailvan.dispatchcenter.stat.monitor.util.WeChatRobotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @program: automated_task_center
 * @description: 任务成功率每日统计
 * @author: Wu Xingjian
 * @create: 2021-10-08 11:07
 **/
@Component
public class MachineLastHeartBeatStatScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ProxyIPMonitorScheduler.class);

    @Autowired
    TaskLogsDao taskLogsDao;

    @Autowired
    WeChatRobotTokenConfig wechatRobotTokenConfig;

    @Autowired
    TaskLogStat taskLogStat;

    @Autowired
    MachineDao machineDao;

    @Autowired
    MachineWorkTypeDao machineWorkTypeDao;



//    @Scheduled(cron = "0 0/30 * * * ?")
    public void sendTaskStatToWechatGroup() throws IOException, InterruptedException, ParseException {
        logger.info("sendTaskStatToWechatGroup start...");
        int interval = 30;
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        List<LinkedHashMap> accountSiteWithHeartbeatTimeOut = machineDao.getAccountSiteWithHeartbeatTimeOut(interval);
        if (accountSiteWithHeartbeatTimeOut.size() == 0) {
            return;
        }
        WeChatRobotUtils weChatRobotUtils = new WeChatRobotUtils(wechatRobotTokenConfig.getWechatRobotToken());

        Map<String,String> ips = new HashMap<>();
        Map<String,String> ipTimes = new HashMap<>();
        for (int i = 0; i < accountSiteWithHeartbeatTimeOut.size(); i++) {
            LinkedHashMap map= accountSiteWithHeartbeatTimeOut.get(i);
            String ip = String.valueOf(map.get("ip"));
            if(StringUtils.isEmpty(ip)){
                continue;
            }
            Object account = map.get("account");
            Object site = map.get("site");
            String s;
            if(ips.containsKey(ip)){
                ips.put(ip,ips.get(ip)+","+account+"_"+site);
            }else {
                ips.put(ip,account+"_"+site);
                ipTimes.put(ip,  ""+map.get("last_heartbeat"));
            }
        }
        String[][] msgArr = new String[ips.size() + 1][3];
        msgArr[0] = new String[]{"last_heartbeat","ip","account_site"};

        int i = 0;
        for (Map.Entry<String, String> stringStringEntry : ips.entrySet()) {
            String key = stringStringEntry.getKey();
            String[] strArr = new String[3];
            String value = stringStringEntry.getValue();
            if (StringUtils.isEmpty(ipTimes.get(key))) {
                strArr[0]=  null;
            }else {
                int index = ipTimes.get(key).lastIndexOf(":");
                strArr[0]=  ipTimes.get(key).substring(5,index);
            }
            strArr[1]=  key;
            strArr[2]=  value;
            msgArr[i + 1] = strArr;
            i++;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(sdf.format(date) + "可用机器上次心跳超过" + interval + "分钟\n");
        //消息对齐后再发
        sb.append(weChatRobotUtils.alignColumn(msgArr));
        //消息过长分段发
        ArrayList<String> strings = ExcelUtils.splitLongString(sb.toString(), '\n', 4000);
        for (String string : strings) {
            weChatRobotUtils.text(string, new String[]{}, new String[]{});
        }
        logger.info("sendTaskStatToWechatGroup over...");
    }

}

