package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.AccountExeTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AccountExeTaskDao {


    void insertAccountExeTask(AccountExeTask accountExeTask);

    List<AccountExeTask> getAccountExeTask(String account, String continent,String period);
}
