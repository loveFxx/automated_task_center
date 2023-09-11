package com.sailvan.dispatchcenter.core.util;

import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 *  请求数量工具类
 *  @author mh
 *  @date 2021-12
 */
@Component
public class RequestCountUtils {

    @Autowired
    RedisUtils redisUtils;


    public void recordRequest(String stage, Object system, String method){
        if(StringUtils.isEmpty(system) || StringUtils.isEmpty(method)){
            return;
        }
        String time = DateUtils.getHourBeforeDate(0).substring(0,13).replace(" ","-");
        String key = "dispatch_center:request:"+time+":"+system+":"+method+":"+stage;
        synchronized (key.intern()){
            Object o = redisUtils.get(key);
            if(!StringUtils.isEmpty(o)){
                redisUtils.put(key, ""+(Integer.parseInt(String.valueOf(o))+1), 7600L);
            }else {
                redisUtils.put(key, ""+1, 7600L);
            }
        }

    }


}
