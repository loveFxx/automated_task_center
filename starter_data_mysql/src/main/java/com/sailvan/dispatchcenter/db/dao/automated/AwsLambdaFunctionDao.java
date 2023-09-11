package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.AwsLambdaFunction;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AwsLambdaFunctionDao {


    AwsLambdaFunction getFunctionByFunctionName(String functionName);

    List<AwsLambdaFunction> getAllLambdaFunction();

    void addFunction(AwsLambdaFunction lambdaFunction);
}
