package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.AwsLambdaFunction;

import java.util.List;

public interface AwsLambdaFunctionService {

    List<AwsLambdaFunction> getAllLambdaFunction();

    AwsLambdaFunction getFunctionByFunctionName(String functionName);

    void addFunction(AwsLambdaFunction lambdaFunction);

}
