package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.ProxyTrend;

import java.util.List;
import java.util.Map;

public interface ProxyTrendService {

    int insertProxyTrend(ProxyTrend proxyTrend);

    void insertProxyTrend(int proxyId, String workType, int isUsed, String openTime);

    List<Map<String,String>> listProxySituation(String beginTime, String endTime, String workType);

    int countInvalidProxy(String beginTime, String endTime, String workType);

    int countValidProxy(String beginTime, String endTime, String workType);

    int deleteByTime(String createdTime);

}

