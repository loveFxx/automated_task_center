package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.AccountExeTask;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

/**
 * @author yyj
 * @date 2021-12
 */
public interface AccountExeTaskService {


    void insertAccountExeTask(AccountExeTask accountExeTask);

    PageDataResult getAccountExeTask(String account, String continent);
}
