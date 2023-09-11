package com.sailvan.dispatchcenter.core.log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.sailvan.dispatchcenter.common.domain.ClientJobLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClientJobLogsPrintUtil {

    private static Logger logger = LoggerFactory.getLogger(ClientJobLogsPrintUtil.class);

    public void printLog(ClientJobLog clientJobLog){

        logger.info("{}", JSON.toJSONString(clientJobLog, SerializerFeature.WriteMapNullValue));
    }
}
