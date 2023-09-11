package com.sailvan.dispatchcenter.core.limit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.constant.RequestCountCode;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.BusinessSystem;
import com.sailvan.dispatchcenter.common.domain.Task;
import com.sailvan.dispatchcenter.common.pipe.BusinessSystemService;
import com.sailvan.dispatchcenter.common.pipe.TaskService;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.google.common.collect.ImmutableList;
import com.sailvan.dispatchcenter.core.util.RequestCountUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 限流
 *
 * @author mh
 * @date 20-09-08
 */
@Aspect
@Configuration
@Order(10)
public class LimitInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LimitInterceptor.class);

    private final RedisTemplate<String, Serializable> limitRedisTemplate;

    @Autowired
    private TaskService taskService;

    @Autowired
    private BusinessSystemService businessSystemService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    RequestCountUtils requestCountUtils;

    @Autowired
    public LimitInterceptor(RedisTemplate<String, Serializable> limitRedisTemplate) {
        this.limitRedisTemplate = limitRedisTemplate;
    }


    /**
     * 可以实现，限制规则
     * 1、taskNameLimit 根据业务端的请求的任务类型进行频率限制
     * 2、businessInterfaceLimit 根据业务系统的名字进行频率限制
     * 3、businessLimitMonth 根据业务系统名字 每月频率限制
     *
     * @param pjp
     * @return
     */
    @Around("@annotation(com.sailvan.dispatchcenter.core.limit.Limit)")
    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {

        ApiResponse apiResponse = new ApiResponse();

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Limit limitAnnotation = method.getAnnotation(Limit.class);
        String[] types = limitAnnotation.types();
        String prefix = limitAnnotation.prefix();

        // 获取请求参数
        Object[] args = pjp.getArgs();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = JSONObject.parseObject(String.valueOf(args[0]));
            if (jsonObject == null) {
                return apiResponse.error(ResponseCode.ERROR_CODE, "args is null", "");
            }
        } catch (Exception e) {
            Object request_system_name = request.getAttribute(Constant.AUTHORIZATION_BUSINESS_NAME);

            if(!org.springframework.util.StringUtils.isEmpty(request_system_name)){
                jsonObject.put("system_name", request_system_name);
            }
        }

        if (!StringUtils.isEmpty(limitAnnotation.key()) && "client".equals(limitAnnotation.key())) {
            if (jsonObject == null || jsonObject.isEmpty()) {
                if(args[1] instanceof Map){
                    jsonObject = JSONObject.parseObject(JSON.toJSONString(args[1]));
                }else {
                    jsonObject = JSONObject.parseObject(String.valueOf(args[1]));
                }
            }
            return interceptorClientLimit(pjp, jsonObject, limitAnnotation);
        } else {
            if (types != null && types.length == 0) {
                return apiResponse.error(ResponseCode.ERROR_CODE, "limitAnnotation is null", "");
            }
            if (!jsonObject.containsKey("system_name")) {
                Object systemName = request.getAttribute(Constant.AUTHORIZATION_BUSINESS_NAME);
                jsonObject.put("system_name", systemName);
            }
            List<String> typesList = Arrays.asList(types);
            Object result = interceptorTaskNameLimit(pjp, jsonObject, typesList, prefix);
            if (result instanceof String) {
                return apiResponse.error(ResponseCode.ERROR_CODE, String.valueOf(result), "");
            }
            return result;
        }
    }

    /**
     * 客户端captcha_code限流
     *
     * @param jsonObject
     * @return
     */
    private Object interceptorClientLimit(ProceedingJoinPoint pjp, JSONObject jsonObject, Limit limitAnnotation) throws Throwable {
        Object account = jsonObject.get("account");
        ApiResponse apiResponse = new ApiResponse();
        if (account == null) {
            logger.info("interceptorClientLimit captcha_code  account is empty");
            return apiResponse.error(ResponseCode.ERROR_CODE,"account is empty:", null) ;
        }
        ImmutableList<String> keys = ImmutableList.of(StringUtils.join(Constant.CLIENT_CAPTCHA_CODE_LIMIT_PREFIX, String.valueOf(account)));
        Number count = getCount(limitAnnotation.period(), limitAnnotation.count(), keys);
        if (count != null && count.intValue() <= limitAnnotation.count()) {
            return pjp.proceed();
        } else {
            requestCountUtils.recordRequest(RequestCountCode.REQUEST_LIMIT , "client", String.valueOf(request.getAttribute(Constant.AUTHORIZATION_REQUEST_URL_NAME)) );
            logger.info(" interceptorClientLimit  captcha_code limit over: account:{}", account);
            return apiResponse.error(ResponseCode.ERROR_CODE,"interceptorClientLimit captcha_code limit over account:" + account,null);
        }

    }

    /**
     * 任务类型限流
     *
     * @param jsonObject
     * @return
     */
    private Object interceptorTaskNameLimit(ProceedingJoinPoint pjp, JSONObject jsonObject, List<String> typesList, String prefix) throws Throwable {
        Object system_name = jsonObject.get("system_name");
        String taskNameLimit = "taskNameLimit";
        if (!typesList.contains(taskNameLimit)) {
            return interceptorBusinessLimit(pjp, jsonObject, typesList, prefix);
        }
        Object type = jsonObject.get("type");
        if (type == null) {
            logger.info("interceptorTaskNameLimit taskNameLimit type is null system_name:{}", system_name);
            return "type is null";
        } else {
            String typeName = String.valueOf(type);
            Task taskByTaskName = taskService.getTaskByTaskName(typeName);
            if (taskByTaskName == null) {
                logger.info("interceptorTaskNameLimit taskNameLimit typeName db is empty:system_name:{}, typeName{}", system_name, typeName);
                return "typeName is empty:" + typeName;
            }
            ImmutableList<String> keys = ImmutableList.of(StringUtils.join(Constant.BUSINESS_LIMIT_PREFIX_TYPENAME, typeName));
            Number count = getCount(taskByTaskName.getApiTimeLimit() * 60, taskByTaskName.getApiMaxTimes(), keys);
            if (count != null && count.intValue() <= taskByTaskName.getApiMaxTimes()) {
                return interceptorBusinessLimit(pjp, jsonObject, typesList, prefix);
            } else {
                requestCountUtils.recordRequest(RequestCountCode.REQUEST_LIMIT , request.getAttribute(Constant.AUTHORIZATION_BUSINESS_NAME), String.valueOf(request.getAttribute(Constant.AUTHORIZATION_REQUEST_URL_NAME)) );
                logger.info(" interceptorTaskNameLimit taskNameLimit limit over: system_name:{}, typeName{}", system_name, typeName);
                return "taskNameLimit limit over:" + typeName;
            }
        }
    }


    /**
     * 业务系统配置限流
     *
     * @return
     */
    private Object interceptorBusinessLimit(ProceedingJoinPoint pjp, JSONObject jsonObject, List<String> typesList, String prefix) throws Throwable {

        String businessInterfaceLimit = "businessInterfaceLimit";
        if (!typesList.contains(businessInterfaceLimit)) {
            return interceptorBusinessLimitMonth(pjp, jsonObject, typesList);
        }

        Object system_name = jsonObject.get("system_name");
        if (system_name == null) {
            Object systemName = request.getAttribute(Constant.AUTHORIZATION_BUSINESS_NAME);
            jsonObject.put("system_name", systemName);
            system_name = systemName;
        }

        BusinessSystem businessSystem = businessSystemService.checkBusiness(String.valueOf(system_name));
        if (businessSystem == null) {
            logger.info("interceptorBusinessLimit system_name db is empty: system_name{}", system_name);
            return "system_name db is empty:" + system_name;
        }
        String key;
        if (!StringUtils.isEmpty(prefix)) {
            key = StringUtils.join(Constant.BUSINESS_LIMIT_PREFIX + prefix + ":", businessSystem.getSystemName());
        } else {
            key = StringUtils.join(Constant.BUSINESS_LIMIT_PREFIX, businessSystem.getSystemName());
        }
        ImmutableList<String> keys = ImmutableList.of(key);
        Number count = getCount(businessSystem.getInvokeInterval() * 60, businessSystem.getInvokeTimes(), keys);
        if (count != null && count.intValue() <= businessSystem.getInvokeTimes()) {
            return interceptorBusinessLimitMonth(pjp, jsonObject, typesList);
        } else {
            requestCountUtils.recordRequest(RequestCountCode.REQUEST_LIMIT , request.getAttribute(Constant.AUTHORIZATION_BUSINESS_NAME), String.valueOf(request.getAttribute(Constant.AUTHORIZATION_REQUEST_URL_NAME)) );
            logger.info("interceptorBusinessLimit businessSystem limit over: system_name{}", system_name);
            return "businessSystem limit over" + system_name;
        }

    }


    /**
     * 业务系统每月限流
     *
     * @param pjp
     * @param jsonObject
     * @return
     */
    private Object interceptorBusinessLimitMonth(ProceedingJoinPoint pjp, JSONObject jsonObject, List<String> typesList) throws Throwable {

        String businessLimitMonth = "businessLimitMonth";
        if (!typesList.contains(businessLimitMonth)) {
            return pjp.proceed();
        }

        Object system_name = jsonObject.get("system_name");
        if (system_name == null) {
            return "system_name is null";
        } else {
            BusinessSystem businessSystem = businessSystemService.checkBusiness(String.valueOf(system_name));
            if (businessSystem == null) {
                return "system_name db is empty:" + system_name;
            }

            ImmutableList<String> keys = ImmutableList.of(StringUtils.join(Constant.BUSINESS_LIMIT_PREFIX_MONTH, businessSystem.getSystemName()));
            Number count = getCount(DateUtils.getDaysOfCurrentMonth() * 24 * 3600, businessSystem.getInvokeTimesMonth(), keys);
            if (count != null && count.intValue() <= businessSystem.getInvokeTimesMonth()) {

                businessSystem.setInvokeTimesMonthUsed(count.intValue());
                // 更新已经使用次数
                businessSystemService.updateBusinessSystemInvokeTimesMonthUsed(businessSystem);
                return pjp.proceed();
            } else {
                requestCountUtils.recordRequest(RequestCountCode.REQUEST_LIMIT , request.getAttribute(Constant.AUTHORIZATION_BUSINESS_NAME), String.valueOf(request.getAttribute(Constant.AUTHORIZATION_REQUEST_URL_NAME)) );
                logger.info("BusinessLimitMonth limit over: system_name{}", system_name);
                return "BusinessLimitMonth limit over:" + system_name;
            }
        }
    }


    /**
     * redis存储的次数
     *
     * @param limitPeriod 间隔 单位秒
     * @param limitTimes  次数
     * @param keys        redis键
     * @return
     */
    private Number getCount(int limitPeriod, int limitTimes, ImmutableList<String> keys) {
        String luaScript = buildLuaScript();
        RedisScript<Number> redisScript = new DefaultRedisScript<>(luaScript, Number.class);
        Number count = limitRedisTemplate.execute(redisScript, keys, limitTimes, limitPeriod);
        return count;
    }

    /**
     * 限流 脚本
     *
     * @return lua脚本
     */
    private String buildLuaScript() {
        StringBuilder lua = new StringBuilder();
        lua.append("local c");
        lua.append("\nc = redis.call('get',KEYS[1])");
        // 调用不超过最大值，则直接返回
        lua.append("\nif c and tonumber(c) > tonumber(ARGV[1]) then");
        lua.append("\nreturn c;");
        lua.append("\nend");
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
