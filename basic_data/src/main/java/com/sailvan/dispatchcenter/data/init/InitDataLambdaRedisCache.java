package com.sailvan.dispatchcenter.data.init;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.AwsLambdaFunction;
import com.sailvan.dispatchcenter.common.domain.AwsRegion;
import com.sailvan.dispatchcenter.common.domain.LambdaUser;
import com.sailvan.dispatchcenter.common.domain.LambdaUserMap;
import com.sailvan.dispatchcenter.common.pipe.AwsLambdaFunctionService;
import com.sailvan.dispatchcenter.common.pipe.LambdaUserMapService;
import com.sailvan.dispatchcenter.common.pipe.LambdaUserService;
import com.sailvan.dispatchcenter.common.pipe.RegionService;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.redis.init.InitLambdaRedisCache;
import netscape.javascript.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@Primary
public class InitDataLambdaRedisCache extends InitLambdaRedisCache {

    @Autowired
    RegionService regionService;

    @Autowired
    AwsLambdaFunctionService lambdaFunctionService;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    LambdaUserService lambdaUserService;

    @Autowired
    LambdaUserMapService lambdaUserMapService;

    /**
     * 初始化lambda区域名 和 lambda函数名
     */

    @Override
    @PostConstruct
    public void init() {

        /**
         * {regionName:regionId}
         */
        JSONArray regionIdMap = new JSONArray();
        List<AwsRegion> regionList = regionService.getRegionAll();
        redisUtils.put(REGION_NAME_MAP_PREFIX, "", 1L);
        for (AwsRegion awsRegion: regionList){
            JSONObject jsonObjectTaskId = new JSONObject();
            String awsRegionName = awsRegion.getRegion();
            jsonObjectTaskId.put("name",awsRegionName);
            jsonObjectTaskId.put("value",awsRegionName);
            if (!StringUtils.isEmpty(awsRegionName) && !regionIdMap.contains(jsonObjectTaskId)) {
                regionIdMap.add(jsonObjectTaskId);
            }
            redisUtils.put(REGION_NAME_MAP_PREFIX,regionIdMap.toJSONString(), Constant.EFFECTIVE);
        }

        /**
         * {functionName:functionName}
         */
        JSONArray functionNameMap = new JSONArray();
        redisUtils.put(LAMBDA_FUNCTION_MAP_PREFIX, "", 1L);
        List<AwsLambdaFunction> lambdaFunctions =  lambdaFunctionService.getAllLambdaFunction();
        for (AwsLambdaFunction function: lambdaFunctions){
            JSONObject jsonObjectFunction = new JSONObject();
            String functionName = function.getFunctionName();
            jsonObjectFunction.put("name",functionName);
            jsonObjectFunction.put("value",functionName);
            if (!StringUtils.isEmpty(functionName) && !functionNameMap.contains(jsonObjectFunction)) {
                functionNameMap.add(jsonObjectFunction);
            }
            redisUtils.put(LAMBDA_FUNCTION_MAP_PREFIX,functionNameMap.toJSONString(), Constant.EFFECTIVE);
        }

        /**
         * {accountName:accountName}
         */
        JSONArray accountNameArray = new JSONArray();
        redisUtils.put(LAMBDA_USER_NAME_PREFIX, "", 1L);
        List<LambdaUser> lambdaUserAll = lambdaUserService.getLambdaUserAll();
        for (LambdaUser lambdaUser:lambdaUserAll){
            JSONObject jsonObjectLambdaUser = new JSONObject();
            String accountName = lambdaUser.getAccountName();
            jsonObjectLambdaUser.put("name",accountName);
            jsonObjectLambdaUser.put("value",accountName);
            if (!StringUtils.isEmpty(accountName) && !accountNameArray.contains(jsonObjectLambdaUser)) {
                accountNameArray.add(jsonObjectLambdaUser);
            }
            redisUtils.put(LAMBDA_USER_NAME_PREFIX,accountNameArray.toJSONString(), Constant.EFFECTIVE);
        }

        /**
         * {lambdaUserMapName:lambdaUserMapName}
         * 保存region名与function名拼接的字符串 用于配置任务的region和function
         */
        JSONArray regionFunctionNameArray = new JSONArray();
        redisUtils.put(REGION_FUNCTION_NAME_PREFIX, "", 1L);
        List<LambdaUserMap> allLambdaUserMap = lambdaUserMapService.getAllLambdaUserMap();
        if (!allLambdaUserMap.isEmpty()){
            for (LambdaUserMap userMap : allLambdaUserMap){
                JSONObject jsonRegionFunctionName = new JSONObject();
                String region = userMap.getRegion();
                String functionName = userMap.getFunctionName();
                jsonRegionFunctionName.put("name",region+"/"+functionName);
                jsonRegionFunctionName.put("value",region+"/"+functionName);
                if (!StringUtils.isEmpty(region) && !StringUtils.isEmpty(functionName)
                        && !regionFunctionNameArray.contains(jsonRegionFunctionName)) {
                    regionFunctionNameArray.add(jsonRegionFunctionName);
                }
                redisUtils.put(REGION_FUNCTION_NAME_PREFIX,regionFunctionNameArray.toJSONString(),Constant.EFFECTIVE);
            }
        }
        System.out.println("region init, lambdaFunction init,lambdaUserName init");

    }


}
