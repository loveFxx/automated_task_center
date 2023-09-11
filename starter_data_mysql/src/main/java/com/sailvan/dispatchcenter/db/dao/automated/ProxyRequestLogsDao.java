package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.ProxyRequestLogs;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProxyRequestLogsDao {

    int insertRequestLogs(ProxyRequestLogs proxyRequestLogs);


    List<Map> sumGroupByProxyId(String minTime, String maxTime, String workType);

    int deleteByTime(String createdTime);

    List<Map> sumGroupByProxyIdByWorkType(String minTime, String maxTime,String workType);

    List<Map> listOverBannedRate(String minTime,String maxTime,String workType,float bannedRate);

    List<Map> sumGroupByTimeBlock(String minTime, String maxTime, String workType, int proxyId);

    Map<String,Integer> sumRangeTime(String minTime, String maxTime, String workType);

    List<ProxyRequestLogs> listRangeTime(String minTime, String maxTime, String workType);
}

