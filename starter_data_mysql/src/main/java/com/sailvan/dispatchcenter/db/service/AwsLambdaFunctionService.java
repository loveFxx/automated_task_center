package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.domain.AwsLambdaFunction;
import com.sailvan.dispatchcenter.db.dao.automated.AwsLambdaFunctionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AwsLambdaFunctionService implements com.sailvan.dispatchcenter.common.pipe.AwsLambdaFunctionService {

    @Autowired
    AwsLambdaFunctionDao awsLambdaFunctionDao;

    @Override
    public List<AwsLambdaFunction> getAllLambdaFunction() {
        return awsLambdaFunctionDao.getAllLambdaFunction();
    }

    @Override
    public AwsLambdaFunction getFunctionByFunctionName(String functionName) {
        return awsLambdaFunctionDao.getFunctionByFunctionName(functionName);
    }

    @Override
    public void addFunction(AwsLambdaFunction lambdaFunction) {
        awsLambdaFunctionDao.addFunction(lambdaFunction);
    }
}
