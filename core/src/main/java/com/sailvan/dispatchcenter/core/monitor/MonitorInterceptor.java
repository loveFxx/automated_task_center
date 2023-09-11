package com.sailvan.dispatchcenter.core.monitor;

import com.sailvan.dispatchcenter.common.constant.Constant;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.io.Serializable;
import java.lang.reflect.Method;

@Aspect
@Configuration
public class MonitorInterceptor {

    private final RedisTemplate<String, Serializable> redisTemplate;

    @Autowired
    public MonitorInterceptor(RedisTemplate<String, Serializable> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     *
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.sailvan.dispatchcenter.core.monitor.Monitor)")
    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Monitor annotation = method.getAnnotation(Monitor.class);

        String type = annotation.type();

        ImmutableList<String> keys = ImmutableList.of(StringUtils.join(type, Constant.SUFFIX_AFTER));
        Number count = getCount(3600, keys);
        return pjp.proceed();
    }


    /**
     *  redis存储的次数
     * @param limitPeriod 间隔 单位秒
     * @param keys  redis键
     * @return
     */
    private Number getCount(int limitPeriod, ImmutableList<String> keys){
        String luaScript = buildLuaScript();
        RedisScript<Number> redisScript = new DefaultRedisScript<>(luaScript, Number.class);
        Number count = redisTemplate.execute(redisScript, keys, limitPeriod);
        return count;
    }

    /**
     * 限流 脚本
     * @return lua脚本
     */
    private String buildLuaScript() {
        StringBuilder lua = new StringBuilder();
        lua.append("local c");
        lua.append("\nc = redis.call('get',KEYS[1])");
        // 执行计算器自加
        lua.append("\nc = redis.call('incr',KEYS[1])");
        lua.append("\nif tonumber(c) == 1 then");
        // 从第一次调用开始限流，设置对应键值的过期
        lua.append("\nredis.call('expire',KEYS[1],ARGV[2])");
        lua.append("\nend");
        lua.append("\nreturn c;");
        return lua.toString();
    }

}
