package com.sailvan.dispatchcenter.core.log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.sailvan.dispatchcenter.common.domain.MachineHeartbeatLogs;
import com.sailvan.dispatchcenter.common.domain.TaskLogs;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.es.config.EsMarkerConfiguration;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.text.ParseException;

/**
 *  这里设置ConditionalOnMissingBean目的是 可以让插入数据库和打印日志并行
 *  @author menghui
 *  @date 2022-03
 */
@Component
public class MachineHeartbeatLogsPrintUtils {
    private static Logger logger = LoggerFactory.getLogger(MachineHeartbeatLogsPrintUtils.class);
    public void printLog(MachineHeartbeatLogs machineHeartbeatLogs){
        MachineHeartbeatLogs machineHeartbeatLogsTmp = new MachineHeartbeatLogs();
        BeanUtils.copyProperties(machineHeartbeatLogs, machineHeartbeatLogsTmp);
        try {
            String pattern = "yyyy-MM-dd'T'HH:mm:ssZZ";
            machineHeartbeatLogsTmp.setCreatedTime(DateFormatUtils.format(DateUtils.convertDate(machineHeartbeatLogs.getCreatedTime()), pattern));
            machineHeartbeatLogsTmp.setHeartbeat(DateFormatUtils.format(DateUtils.convertDate(machineHeartbeatLogs.getHeartbeat()), pattern));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        logger.info("{}", JSON.toJSONString(machineHeartbeatLogsTmp, SerializerFeature.WriteMapNullValue));
    }
}
