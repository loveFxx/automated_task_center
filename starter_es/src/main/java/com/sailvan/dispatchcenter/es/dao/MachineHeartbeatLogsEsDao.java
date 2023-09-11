package com.sailvan.dispatchcenter.es.dao;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.sailvan.dispatchcenter.common.domain.MachineHeartbeatLogs;
import com.sailvan.dispatchcenter.common.pipe.MachineHeartbeatLogsService;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.es.config.EsMarkerConfiguration;
import com.sailvan.dispatchcenter.es.config.MapConfig;
import com.sailvan.dispatchcenter.es.util.ESConnection;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Primary
@ConditionalOnBean(EsMarkerConfiguration.EsMarker.class)
public class MachineHeartbeatLogsEsDao implements MachineHeartbeatLogsService {

    @Autowired
    ESConnection esConnection;

    @Override
    public PageDataResult getMachineHeartbeatLogsByMachineIdList(int machineId)  {

        MachineHeartbeatLogs machineHeartbeatLogSearch = new MachineHeartbeatLogs();
        machineHeartbeatLogSearch.setMachineId(machineId);
        machineHeartbeatLogSearch.setCreatedTime(DateUtils.getCurrentDateStart());

        List<MachineHeartbeatLogs> machineHeartbeatLogs = getMachineHeartbeatLogsByMachineId(machineHeartbeatLogSearch);
        PageInfo<MachineHeartbeatLogs> pageInfoOld = new PageInfo<>(machineHeartbeatLogs);

        PageDataResult pageDataResult = new PageDataResult();
        if(machineHeartbeatLogs.size() != 0){
            pageDataResult.setList(machineHeartbeatLogs);
            pageDataResult.setTotals((int) pageInfoOld.getTotal());
        }

        return pageDataResult;
    }

    public List<MachineHeartbeatLogs> getMachineHeartbeatLogsByMachineId(MachineHeartbeatLogs machineHeartbeatLogs){
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("machineId", machineHeartbeatLogs.getMachineId());
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(termQueryBuilder);
        ESConnection.ESSearchRequest esSearchRequest = esConnection.new ESSearchRequest("wb_machine_heartbeat_logs",
                "_doc").setQuery(boolQuery).setSort("createdTime", "desc").size(10);
        SearchResponse response = esSearchRequest.getResponse();
        List<MachineHeartbeatLogs> logs = new ArrayList<>();
        for (SearchHit hit : response.getHits()){
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            logs.add(JSON.parseObject(JSON.toJSONString(sourceAsMap), MachineHeartbeatLogs.class));
        }
        return logs;
    }

    @Override
    public int insertMachineHeartbeatLogs(MachineHeartbeatLogs machineHeartbeatLogs){
        return 1;
    }

    @Override
    public ArrayList<String[]> getLatestMachineHeartbeatLogsAll() {
        return null;
    }

    @Override
    public List<String> getLatestIds(String start, String end){
        return null;
    }

    @Override
    public List<MachineHeartbeatLogs> getLatestHeartByIds(String start, String end, String idList){
        return null;
    }
}
