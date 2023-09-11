package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.ProxyTrend;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProxyTrendDao {

    int insertProxyTrend(ProxyTrend proxyTrend);

    List<Map<String,String>> listProxySituation(String beginTime, String endTime, String workType);

    int countInvalidProxy(String beginTime, String endTime, String workType);

    int countValidProxy(String beginTime, String endTime, String workType);

    int deleteByTime(String createdTime);
}

