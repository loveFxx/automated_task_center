package com.sailvan.dispatchcenter.core.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.github.pagehelper.util.StringUtil;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.constant.Event;
import com.sailvan.dispatchcenter.common.domain.TaskBufferMeta;
import com.sailvan.dispatchcenter.common.domain.TaskMetadata;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.core.collection.LambdaCollection;
import com.sailvan.dispatchcenter.core.domain.TaskBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

@Service
public class LambdaService {

    @Autowired
    TaskBufferService taskBufferService;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    TaskLogsService taskLogsService;

    private static Logger logger = LoggerFactory.getLogger(LambdaService.class);

    public LambdaClient buidLambdaClient(String accessKey, String accessSecret, String region){
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, accessSecret);
        return LambdaClient.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .region(Region.of(region)).build();
    }

    public InvokeResponse asyncInvokeFunction(LambdaClient awsLambda, String functionName, String json) {

        InvokeResponse res = null ;
        try {
            SdkBytes payload = SdkBytes.fromUtf8String(json) ;

            //Setup an InvokeRequest
            InvokeRequest request = InvokeRequest.builder()
                    .invocationType("Event")
                    .functionName(functionName)
                    .payload(payload)
                    .build();


            res = awsLambda.invoke(request);
            return res;

        } catch(LambdaException e) {
            logger.error("请求Lambda异常:{}",e.getMessage());
        }
        return null;
    }

    public String invokeFunction(LambdaClient awsLambda, String functionName) {

        InvokeResponse res = null ;
        try {
            //Need a SdkBytes instance for the payload
            String json = "{\"content\":\"test\"}";
            SdkBytes payload = SdkBytes.fromUtf8String(json) ;

            //Setup an InvokeRequest
            InvokeRequest request = InvokeRequest.builder()
                    .functionName(functionName)
                    .payload(payload)
                    .build();

            res = awsLambda.invoke(request);
            if (res.statusCode() == 200){
                return res.payload().asUtf8String();
            }

        } catch(LambdaException e) {
            logger.error("请求Lambda异常:{}",e.getMessage());
        }
        return "";
    }

    public void listFunctions(LambdaClient awsLambda) {
        try {
            ListFunctionsResponse functionResult = awsLambda.listFunctions();
            List<FunctionConfiguration> list = functionResult.functions();

            for (FunctionConfiguration config: list) {
                System.out.println("The function name is "+config.functionName());
            }

        } catch(LambdaException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public void deleteLambdaFunction(LambdaClient awsLambda, String functionName ) {
        try {
            //Setup an DeleteFunctionRequest
            DeleteFunctionRequest request = DeleteFunctionRequest.builder()
                    .functionName(functionName)
                    .build();

            awsLambda.deleteFunction(request);
            System.out.println("The "+functionName +" function was deleted");

        } catch(LambdaException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public void requestLambda(){
        String taskBufferId = LambdaCollection.instance().getTaskBufferId();
        if (StringUtil.isEmpty(taskBufferId)){
            return;
        }
        TaskBuffer taskBuffer = taskBufferService.findById(taskBufferId);

        if (taskBuffer != null) {
            if (Constant.taskTypeInLambdaNum.containsKey(taskBuffer.getType())){
                Constant.taskTypeInLambdaNum.get(taskBuffer.getType()).decrementAndGet();
            }
            Object message = redisUtils.get(Constant.TASK_PREFIX + taskBuffer.getId());

            if (Constant.TASK_LAMBDA_MAP.containsKey(taskBuffer.getType())) {
                String jsonString = String.valueOf(message);
                JSONObject jsonObject = JSONObject.parseObject(jsonString, Feature.OrderedField);
                TaskMetadata metadata = JSONObject.toJavaObject(jsonObject, TaskMetadata.class);
                LinkedHashMap clientParams = metadata.getClient_params();
                if (metadata.getWork_type().equals("AmazonDaemon") && redisUtils.exists(Constant.COOKIE)){
                    clientParams.put(Constant.COOKIE,redisUtils.get(Constant.COOKIE));
                }
                String resultJson = JSONObject.toJSONString(metadata);
                HashMap<String, String> relationMap = Constant.TASK_LAMBDA_MAP.get(taskBuffer.getType());
                LambdaClient awsLambda = buidLambdaClient(relationMap.get("access_key"),
                        relationMap.get("access_secret"), relationMap.get("region"));
                InvokeResponse res = asyncInvokeFunction(awsLambda, relationMap.get("function_name"), resultJson);
                if (res != null) {
                    if (res.statusCode() != 202) {
                        //重新入池
                        LambdaCollection.instance().incrementQueue(taskBuffer);
                    } else {
                        redisUtils.remove(Constant.TASK_PREFIX + taskBufferId);
                        Constant.LAMBDA_REQUEST_LIMIT.decrementAndGet();
                        taskBufferService.updateIsInPoolById(2, taskBuffer.getId());
                        taskLogsService.addTaskLogs(taskBuffer, Event.OUT_POOL, "",Constant.LAMBDA_CONTAINER);
                    }
                }else {
                    LambdaCollection.instance().incrementQueue(taskBuffer);
                }
            } else {
                logger.error("类型[{}]未配置关键的Lambda信息", taskBuffer.getType());
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {

        LambdaCollection.instance().offerPriorityBlockingQueue(3,"ss");
        LambdaCollection.instance().offerPriorityBlockingQueue(3,"ddd");
        LambdaCollection.instance().offerPriorityBlockingQueue(1,"11");
        LambdaCollection.instance().offerPriorityBlockingQueue(2,"11");
        PriorityBlockingQueue<TaskBufferMeta> lambdaQueue = LambdaCollection.getLambdaQueue();
        String a = "";
//        new Thread(){
//            @Override
//            public void run() {
//                for (int i=0;i<20000;i++){
//                    LambdaService lambdaService = new LambdaService();
//                    lambdaService.test();
//                    try {
//                        Thread.sleep(3000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }.start();
//
////         方法二
//        ExecutorService executeService = Executors.newCachedThreadPool();
//        Set<Callable<String>> callables = new HashSet<Callable<String>>();
//        long startTime = System.currentTimeMillis();
//        for (int i = 0;i < 1000;i ++) {
//            callables.add(new Callable<String>() {
//                public String call() throws Exception {
//                    LambdaService lambdaService = new LambdaService();
//                    return lambdaService.test();
//                }
//            });
//        }

    }
}
