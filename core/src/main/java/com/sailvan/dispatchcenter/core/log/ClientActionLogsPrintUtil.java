package com.sailvan.dispatchcenter.core.log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.sailvan.dispatchcenter.common.domain.ClientActionLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClientActionLogsPrintUtil {

    private static Logger logger = LoggerFactory.getLogger(ClientActionLogsPrintUtil.class);

    public void printLog(ClientActionLog clientActionLog){
        logger.info("{}", JSON.toJSONString(clientActionLog, SerializerFeature.WriteMapNullValue));
    }
}
