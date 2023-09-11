package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.domain.AccountExeTask;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.db.dao.automated.AccountExeTaskDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


@Service
public class AccountExeTaskService implements com.sailvan.dispatchcenter.common.pipe.AccountExeTaskService {

    @Autowired
    AccountExeTaskDao accountExeTaskDao;


    @Override
    public void insertAccountExeTask(AccountExeTask accountExeTask) {
        accountExeTaskDao.insertAccountExeTask(accountExeTask);
    }

    @Override
    public PageDataResult getAccountExeTask(String account, String continent) {
        SimpleDateFormat sdfHour = new SimpleDateFormat("yyyy-MM-dd-HH");
        Date yesterday = DateUtils.minusDay(1, new Date());
        String period = sdfHour.format(yesterday);
        List<AccountExeTask> accountExeTasks = accountExeTaskDao.getAccountExeTask(account,continent,period);
        PageDataResult pageDataResult = new PageDataResult();
        pageDataResult.setList(accountExeTasks);
        return pageDataResult;
    }
}
