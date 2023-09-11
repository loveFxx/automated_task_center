package com.sailvan.dispatchcenter.core.log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.sailvan.dispatchcenter.common.domain.ClientRequestLog;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClientRequestLogsPrintUtil {

    private static Logger logger = LoggerFactory.getLogger(ClientRequestLogsPrintUtil.class);

    public void printLog(ClientRequestLog clientRequestLog){
        logger.info("{}", JSON.toJSONString(clientRequestLog, SerializerFeature.WriteMapNullValue));
    }
}
