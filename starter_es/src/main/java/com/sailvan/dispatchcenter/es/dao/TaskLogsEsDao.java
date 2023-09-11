package com.sailvan.dispatchcenter.es.dao;

import com.alibaba.fastjson.JSON;
import com.sailvan.dispatchcenter.common.constant.EsConstant;
import com.sailvan.dispatchcenter.common.domain.MachineHeartbeatLogs;
import com.sailvan.dispatchcenter.common.domain.TaskLogs;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.db.dao.automated.TaskLogsDao;
import com.sailvan.dispatchcenter.es.config.EsMarkerConfiguration;
import com.sailvan.dispatchcenter.es.util.ESConnection;
import com.sailvan.dispatchcenter.es.util.EsUtils;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.util.*;

@Primary
@ConditionalOnBean(EsMarkerConfiguration.EsMarker.class)
public class TaskLogsEsDao implements TaskLogsDao {

    @Autowired
    ESConnection esConnection;

//    final static int size = 1000;
//    final static String flag = "flag";

    @Override
    public int insertTaskLogs(TaskLogs taskLogs) {
        return 1;
    }

    @Override
    public List<TaskLogs> getTaskLogsByTaskLogs(TaskLogs taskLogs, String startTime, String endTime) {
        BoolQueryBuilder matchQueryBuilder = QueryBuilders.boolQuery();
        BoolQueryBuilder shouldQueryAccount= QueryBuilders.boolQuery();
        if (!"".equals(taskLogs.getAccount()) && taskLogs.getAccount()!=null){
            String accounts = taskLogs.getAccount();
            String[] split = accounts.split(",");
            List<String> accountList = new ArrayList<>();
            for (int i = 0; i <split.length ; i++) {
                accountList.add(split[i]);
            }

            accountList.forEach((account) -> shouldQueryAccount.should(QueryBuilders.termQuery(EsConstant.TASK_LOG_ACCOUNT, account)));

//            matchQueryBuilder
//                    .must(QueryBuilders.termQuery(EsConstant.TASK_LOG_ACCOUNT, taskLogs.getAccount()));
        }
        matchQueryBuilder.must(shouldQueryAccount);
        if (!"".equals(taskLogs.getTaskSourceId())&&taskLogs.getTaskSourceId()!=null){
            matchQueryBuilder
                    .must(QueryBuilders.termQuery(EsConstant.TASK_LOG_TASK_SOURCE_ID, taskLogs.getTaskSourceId()));
        }
        if (!"".equals(taskLogs.getClientParams()) && taskLogs.getClientParams()!=null){
            matchQueryBuilder
                    .must(QueryBuilders.termQuery(EsConstant.TASK_LOG_CLIENT_PARAMS, taskLogs.getClientParams()));
        }
        if (!"".equals(taskLogs.getExplain()) && taskLogs.getExplain() != null){
            matchQueryBuilder
                    .must(QueryBuilders.termQuery(EsConstant.TASK_LOG_EXPLAIN, taskLogs.getExplain()));
        }
        if (taskLogs.getResultHashKey()!=0){
            matchQueryBuilder
                    .must(QueryBuilders.termQuery(EsConstant.TASK_LOG_RESULT_HASH_KEY,taskLogs.getResultHashKey()));
        }
        if (taskLogs.getHashKey()!=0){
            matchQueryBuilder
                    .must(QueryBuilders.termQuery(EsConstant.TASK_LOG_HASH_KEY,taskLogs.getHashKey()));
        }
        BoolQueryBuilder shouldQueryTaskName= QueryBuilders.boolQuery();
        if (!"".equals(taskLogs.getTaskName())&&taskLogs.getTaskName()!=null){
            String taskNames = taskLogs.getTaskName();
            String[] split = taskNames.split(",");
            List<String> taskNameList = new ArrayList<>();
            for (int i = 0; i <split.length ; i++) {
                taskNameList.add(split[i]);
            }

            taskNameList.forEach((taskName) -> shouldQueryAccount.should(QueryBuilders.termQuery(EsConstant.TASK_LOG_TASK_NAME, taskName)));
//            matchQueryBuilder
//                    .must(QueryBuilders.termQuery(EsConstant.TASK_LOG_TASK_NAME,taskLogs.getTaskName()));
        }
        matchQueryBuilder.must(shouldQueryTaskName);
        if (taskLogs.getRetryTimes()!=0){
            matchQueryBuilder
                    .must(QueryBuilders.termQuery(EsConstant.TASK_LOG_RETRY_TIMES,taskLogs.getRetryTimes()));
        }
        if (taskLogs.getEvent()!=-1){
            matchQueryBuilder
                    .must(QueryBuilders.termQuery(EsConstant.TASK_LOG_EVENT,taskLogs.getEvent()));
        }
        if (!"".equals(taskLogs.getContinent())&&taskLogs.getContinent()!=null){
            matchQueryBuilder
                    .must(QueryBuilders.termQuery(EsConstant.TASK_LOG_CONTINENT,taskLogs.getContinent()));
        }
        if (!"".equals(taskLogs.getPlatform())&&taskLogs.getPlatform()!=null){
            matchQueryBuilder
                    .must(QueryBuilders.termQuery(EsConstant.TASK_LOG_PLATFORM,taskLogs.getPlatform()));
        }
        if (!"".equals(taskLogs.getRemoteIp())&&taskLogs.getRemoteIp()!=null){
            matchQueryBuilder
                    .must(QueryBuilders.termQuery(EsConstant.TASK_LOG_REMOTE_IP,taskLogs.getRemoteIp()));
        }
        if (!"".equals(taskLogs.getProxyIp())&&taskLogs.getProxyIp()!=null){
            matchQueryBuilder
                    .must(QueryBuilders.termQuery("proxyIp",taskLogs.getProxyIp()));
        }
        if (!"".equals(taskLogs.getRefreshTime())&&taskLogs.getRefreshTime()!=null){
            matchQueryBuilder
                    .must(QueryBuilders.termQuery("refreshTime",taskLogs.getRefreshTime()));
        }
        if (!"".equals(taskLogs.getDate())&&taskLogs.getDate()!=null){
            matchQueryBuilder
                    .must(QueryBuilders.termQuery(EsConstant.TASK_LOG_DATE,taskLogs.getDate()));
        }
        if (!"".equals(taskLogs.getCreatedTime())&&taskLogs.getCreatedTime()!=null){
            matchQueryBuilder
                    .must(QueryBuilders.termQuery(EsConstant.TASK_LOG_CREATED_TIME,taskLogs.getCreatedTime()));
        }
        if (taskLogs.getHasFile()!=0){
            matchQueryBuilder
                    .must(QueryBuilders.termQuery(EsConstant.TASK_LOG_HAS_FILE,taskLogs.getHasFile()));
        }

        try {
            Object startObject = EsUtils.typeConvert(DateUtils.convertDate(startTime), EsConstant.TASK_LOG_DATE);
            QueryBuilder rangeQueryBuilder = null;
            if (!StringUtils.isEmpty(endTime)) {
                Object endObject = EsUtils.typeConvert(DateUtils.convertDate(endTime), EsConstant.TASK_LOG_DATE);
                rangeQueryBuilder = QueryBuilders.rangeQuery(EsConstant.TASK_LOG_CREATED_TIME).from(startObject).to(endObject);
            } else {
                rangeQueryBuilder = QueryBuilders.rangeQuery(EsConstant.TASK_LOG_CREATED_TIME).from(startObject);
            }
            matchQueryBuilder.must(rangeQueryBuilder);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(matchQueryBuilder);

        ESConnection.ESSearchRequest esSearchRequest = esConnection.new ESSearchRequest(EsConstant.TASK_LOGS_INDEX,
                EsConstant.QUERY_TYPE_DOC).setQuery(matchQueryBuilder).setSort(EsConstant.TASK_LOG_CREATED_TIME, EsConstant.QUERY_SORT_DESC).size(200);
        SearchResponse response = esSearchRequest.getResponse();
        List<TaskLogs> logs = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            logs.add(JSON.parseObject(JSON.toJSONString(sourceAsMap), TaskLogs.class));
        }
        return logs;
    }

    @Override
    public int getTaskLogsCountById(int id, String startTime, String endTime) {
        SearchResponse logsByStartEnd = getLogsByStartEnd(startTime, endTime);
        int totalHits = (int) logsByStartEnd.getHits().totalHits;
        return totalHits;
        //return (int) getQuery("totalHits", null, startTime, endTime, null, null, 0, size);

    }

    @Override
    public List<TaskLogs> getTaskLogsById(Integer id, String startTime, String endTime, int offset, Integer size) {
        return (List<TaskLogs>) getQuery("logsAll", null, startTime, endTime, null, null, offset, size);
    }

    @Override
    public List<TaskLogs> getTaskLogsByTaskSourceId(String taskSourceId, String startTime, String endTime) {
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(EsConstant.TASK_LOG_TASK_SOURCE_ID, taskSourceId);
        Object startObject = null;
        Object endObject = null;
        try {
            startObject = EsUtils.typeConvert(DateUtils.convertDate(startTime), EsConstant.TASK_LOG_DATE);
            endObject = EsUtils.typeConvert(DateUtils.convertDate(endTime), EsConstant.TASK_LOG_DATE);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        QueryBuilder queryBuilder = QueryBuilders.rangeQuery(EsConstant.TASK_LOG_CREATED_TIME).from(startObject).to(endObject);
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(termQueryBuilder);
        boolQuery.must(queryBuilder);
//        esConnection.getMapping("wb_atc_machine_heartbeat_logs", "_doc");
        ESConnection.ESSearchRequest esSearchRequest = esConnection.new ESSearchRequest(EsConstant.TASK_LOGS_INDEX,
                EsConstant.QUERY_TYPE_DOC).setQuery(boolQuery).setSort(EsConstant.TASK_LOG_CREATED_TIME, EsConstant.QUERY_SORT_DESC).size(50);
        SearchResponse response = esSearchRequest.getResponse();
        List<TaskLogs> logs = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            logs.add(JSON.parseObject(JSON.toJSONString(sourceAsMap), TaskLogs.class));
        }
        return logs;
    }

    /**
     * <select id="getTaskNameTotalCount" resultType="java.util.Map">
     * select task_name,count(*) from atc_task_logs
     * where
     * created_time
     * between #{start} and #{end}
     * and event=1 group by task_name
     * </select>
     *
     * @param start
     * @param end
     * @return
     */
    @Override
    public List<Map> getTaskNameTotalCount(String start, String end) {
        List<Map> result = new ArrayList<>();
        Map<String, Object> resultMap = (Map<String, Object>) getAggregationQuery("count", EsConstant.TASK_LOG_TASK_NAME, 100, "1", start, end);
        for (Map.Entry<String, Object> stringIntegerEntry : resultMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("task_name", stringIntegerEntry.getKey());
            map.put("count(*)", stringIntegerEntry.getValue());
            result.add(map);
        }
        return result;
    }

    /**
     * <select id="getHashKeysList" resultType="java.lang.String">
     * select distinct hash_key from atc_task_logs
     * where
     * <![CDATA[ created_time >= #{start}  ]]>
     * and
     * <![CDATA[  created_time <=#{end}   ]]>
     * and
     * event=1 and hash_key is not null
     * </select>
     *
     * @param start
     * @param end
     * @return
     */
    @Override
    public List<String> getHashKeysList(String start, String end) {

        List<String> list = new ArrayList<>();
        list.add(EsConstant.FLAG);
        return list;
//        return (List<String>) getAggregationQuery("HashKeysList", "hashKey", 10000000, "1", start, end);


    }


    /**
     * <select id="getTaskNameForceFailedCount"  resultType="java.util.Map">
     * select task_name,count(*),group_concat(task_source_id) from atc_task_logs
     * where
     * <![CDATA[ created_time >= #{start}  ]]>
     * and
     * <![CDATA[  created_time <=#{end}   ]]>
     * and event=7
     * and hash_key in ${hashKeyList}
     * group by task_name
     * </select>
     *
     * @param start
     * @param end
     * @param hashKeyList
     * @return
     */
    @Override
    public List<Map> getTaskNameForceFailedCount(String start, String end, String hashKeyList) {
        return getResultByEvent("7", start, end, null, null);

    }


    /**
     * <select id="getTaskNameSuccessCount"  resultType="java.util.Map">
     * select task_name,count(*) from atc_task_logs
     * where
     * <![CDATA[ created_time >= #{start}  ]]>
     * and
     * <![CDATA[  created_time <=#{end}   ]]>
     * and event=4
     * and hash_key in ${hashKeyList}
     * group by task_name
     * </select>
     *
     * @param start
     * @param end
     * @param hashKeyList
     * @return
     */
    @Override
    public List<Map> getTaskNameSuccessCount(String start, String end, String hashKeyList) {

        List<String> list = hashKeyToList(hashKeyList);
        if (null == list){
            return getResultByEvent("4", start, end, list, null);
        }
        if(list.contains(EsConstant.FLAG)){
            return getResultByEvent("4", start, end, null, null);
        }

        return getResultByEvent("4", start, end, hashKeyToList(hashKeyList), null);
    }

    private List<String> hashKeyToList(String hashKeyList) {
        List<String> list = null;
        if (!StringUtils.isEmpty(hashKeyList)) {
            String str = hashKeyList.replaceAll("\\(", "").replaceAll("\\)", "");
            list = Arrays.asList(str.split(","));
        }
        return list;
    }


    /**
     * <select id="getTaskNameFileCount"  resultType="java.util.Map">
     * select task_name,count(*) from atc_task_logs
     * where
     * <![CDATA[ created_time >= #{start}  ]]>
     * and
     * <![CDATA[  created_time <=#{end}   ]]>
     * and event=4
     * and hash_key in ${hashKeyList} and has_file=1
     * group by task_name
     * </select>
     *
     * @param start
     * @param end
     * @param hashKeyList
     * @return
     */
    @Override
    public List<Map> getTaskNameFileCount(String start, String end, String hashKeyList) {

        //return getResultByEvent("4", start, end, hashKeyList, "1");
        return getResultByEvent("4", start, end, null, "1");
    }


    /**
     * <select id="getMaxIdOfHashKey"  resultType="java.lang.String">
     * select max(id) from atc_task_logs
     * where
     * <![CDATA[ created_time >= #{start}  ]]>
     * and
     * <![CDATA[  created_time <=#{end}   ]]>
     * and hash_key in ${hashKeyList}
     * group by hash_key
     * </select>
     *
     * @param start
     * @param end
     * @param hashKeyList
     * @return
     */
    @Override
    public List<String> getMaxIdOfHashKey(String start, String end, String hashKeyList) {
        List<String> list = hashKeyToList(hashKeyList);
        if(list.contains(EsConstant.FLAG)){
            return list;
        }

        return getMaxIdOfHashKeyAll( start,  end,  hashKeyList);
    }


    public List<String> getMaxIdOfHashKeyAll(String start, String end, String hashKeyList) {
        QueryBuilder rangeQueryBuilder = null;
        List<String> result = new ArrayList<>();
        try {
            Object startObject = EsUtils.typeConvert(DateUtils.convertDate(start), EsConstant.TASK_LOG_DATE);
            if (!StringUtils.isEmpty(end)) {
                Object endObject = EsUtils.typeConvert(DateUtils.convertDate(end), EsConstant.TASK_LOG_DATE);
                rangeQueryBuilder = QueryBuilders.rangeQuery(EsConstant.TASK_LOG_CREATED_TIME).from(startObject).to(endObject);
            } else {
                rangeQueryBuilder = QueryBuilders.rangeQuery(EsConstant.TASK_LOG_CREATED_TIME).from(startObject);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        List<String> list = hashKeyToList(hashKeyList);
        boolQuery.must(rangeQueryBuilder);
        if (list != null && !list.isEmpty()) {
            int i = 0 ;
            for (String s : list) {
                i++;
//                if(i >10){
//                    continue;
//                }
                boolQuery.should(QueryBuilders.matchPhraseQuery(EsConstant.TASK_LOG_HASH_KEY, s));
            }
            boolQuery.minimumShouldMatch(1);
        }

        ESConnection.ESSearchRequest esSearchRequest = esConnection.new ESSearchRequest(EsConstant.TASK_LOGS_INDEX,
                EsConstant.QUERY_TYPE_DOC).setQuery(boolQuery).setScroll(1L).size(EsConstant.SIZE);
        SearchResponse response = esSearchRequest.getResponse();
        String scrollId = response.getScrollId();
        Map<String, String> map = new HashMap<>();
        Map<String, String> mapId = new HashMap<>();
        for (SearchHit hit : response.getHits().getHits()) {
            String id = hit.getId();
            String createTime = String.valueOf(hit.getSourceAsMap().get(EsConstant.TASK_LOG_CREATED_TIME));
            String hashKey = String.valueOf(hit.getSourceAsMap().get(EsConstant.TASK_LOG_HASH_KEY));
            if (map.containsKey(hashKey)) {
                if (map.get(hashKey).compareTo(String.valueOf(createTime)) < 0) {
                    map.put(hashKey, createTime);
                    map.put(hashKey, id);
                }
            } else {
                map.put(hashKey, createTime);
                mapId.put(hashKey, id);
            }
        }
        while (true) {
            SearchResponse scrollResponse = esSearchRequest.getResponseScroll(scrollId, 1L);
            SearchHit[] hits = scrollResponse.getHits().getHits();
            if (hits != null && hits.length > 0) {
                for (SearchHit hit : hits) {
                    String id = hit.getId();
                    String createTime = String.valueOf(hit.getSourceAsMap().get(EsConstant.TASK_LOG_CREATED_TIME));
                    String hashKey = String.valueOf(hit.getSourceAsMap().get(EsConstant.TASK_LOG_HASH_KEY));
                    if (map.containsKey(hashKey)) {
                        if (map.get(hashKey).compareTo(String.valueOf(createTime)) < 0) {
                            map.put(hashKey, createTime);
                            map.put(hashKey, id);
                        }
                    } else {
                        map.put(hashKey, createTime);
                        mapId.put(hashKey, id);
                    }
                }

            } else {
                break;
            }
        }
        esSearchRequest.clearScroll(scrollId);
        result = new ArrayList<>(mapId.values());
        return result;
    }

    /**
     * select hash_key from atc_task_logs
     * where
     * <![CDATA[ created_time >= #{start}  ]]>
     * and
     * <![CDATA[  created_time <=#{end}   ]]>
     * and hash_key in ${hashKeyList}
     * group by hash_key
     *
     * @param start
     * @param end
     * @param hashKeyList
     * @return
     */
    @Override
    public List<String> getHashKeyFromSecondDay(String start, String end, String hashKeyList) {
        List<String> list = hashKeyToList(hashKeyList);
        if(list.contains(EsConstant.FLAG)){
            return new ArrayList<>();
        }
        return (List<String>) getAggregationQuery("FromSecondDay", EsConstant.TASK_LOG_HASH_KEY, 10000000, hashKeyToList(hashKeyList), null, start, end, null, null);

    }

    /**
     * <select id="getTaskNameFailedCount"  resultType="java.util.Map">
     * select task_name,count(*) from atc_task_logs
     * where
     * <![CDATA[ created_time >= #{start}  ]]>
     * and
     * <![CDATA[  created_time <=#{end}   ]]>
     * and event=5
     * and id in ${idList}
     * group by task_name
     * </select>
     * todo: idList无法获取
     *
     * @param start
     * @param end
     * @param idList
     * @return
     */
    @Override
    public List<Map> getTaskNameFailedCount(String start, String end, String idList) {
        //return getResultByEvent("5",  start,  end,  null, null);
        Map<String, Object> resultMap = (Map<String, Object>) getAggregationQuery("count", EsConstant.TASK_LOG_TASK_NAME, EsConstant.SIZE, null, "5", start, end, null, "1");
        List<Map> result = new ArrayList<>();
        for (Map.Entry<String, Object> stringIntegerEntry : resultMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("task_name", stringIntegerEntry.getKey());
            map.put("count(*)", stringIntegerEntry.getValue());
            result.add(map);
        }
        return result;
    }

    /**
     * <select id="tooMuchFailureReasonsStat"  resultType="java.util.Map">
     * select hash_key,task_name,account,continent,center_params,  client_params , ifnull(retry_times, 0) as retry_times,`explain`
     * from atc_task_logs where
     * <![CDATA[ created_time >= #{start}  ]]>
     * and
     * <![CDATA[  created_time <=#{end}   ]]>
     * and
     * retry_times>=#{retryTimes} and event=5
     * </select>
     *
     * @param start
     * @param end
     * @param retryTimes
     * @param splitSign
     * @return
     */
    @Override
    public List<Map> tooMuchFailureReasonsStat(String start, String end, int retryTimes, String splitSign) {
        return (List<Map>) getQuery("FailureReasonsStat", "5", start, end, null, String.valueOf(retryTimes), 0, EsConstant.SIZE);
    }

    /**
     * <select id="getTaskNameTotalSuccessCount"  resultType="java.util.Map">
     * SELECT task_name,count(1) as `count` FROM atc_task_logs
     * where
     * <![CDATA[ created_time >= #{start}  ]]>
     * and event=4
     * group by task_name
     * </select>
     *
     * @param start
     * @return
     */
    @Override
    public List<Map> getTaskNameTotalSuccessCount(String start) {
        Map<String, Object> resultMap = (Map<String, Object>) getAggregationQuery("count", EsConstant.TASK_LOG_TASK_NAME, 100, "4", start, null);
        List<Map> result = new ArrayList<>();
        for (Map.Entry<String, Object> stringIntegerEntry : resultMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("task_name", stringIntegerEntry.getKey());
            map.put("count", stringIntegerEntry.getValue());
            result.add(map);
        }
        return result;
    }


    /**
     * <select id="getTaskMameListByContinentsAccount"  resultType="java.util.Map">
     * <p>
     * <p>
     * select concat(continent,',',account),group_concat(task_name)   from atc_task_logs
     * where
     * <![CDATA[ created_time >= #{start}  ]]>
     * and
     * <![CDATA[  created_time <=#{end}   ]]>
     * <p>
     * group by continent,account
     *
     * </select>
     *
     * @param start
     * @param end
     * @return
     */
    @Override
    public List<Map> getTaskMameListByContinentsAccount(String start, String end) {
        //SearchResponse response = getQuery(null,  start,  end, null, null, 0, size);
        Map<String, Object> resultMap = (Map<String, Object>) getQuery("continentsAccount", null, start, end, null, null, 0, EsConstant.SIZE);
        List<Map> result = new ArrayList<>();
        for (Map.Entry<String, Object> stringIntegerEntry : resultMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("concat(continent,',',account)", stringIntegerEntry.getKey());
            map.put("group_concat(task_name)", stringIntegerEntry.getValue());
            result.add(map);
        }
        return result;
    }

    /**
     * 没用到
     *
     * @param taskSourceId
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public List<TaskLogs> getEventById(String taskSourceId, String startTime, String endTime) {
        return null;
    }

    /**
     * <select id="getTaskLogsListInOneHour" resultMap="taskLogsMap" resultType="com.sailvan.dispatchcenter.common.domain.TaskLogs">
     * select * from atc_task_logs
     * where
     * <![CDATA[ created_time >= #{startTime}  ]]>
     * and
     * <![CDATA[  created_time < #{endTime}   ]]>
     * and id > ${startId}
     * and account != ""
     * LIMIT  #{offset},#{limit}
     * </select>
     *
     * @param startId
     * @param startTime
     * @param endTime
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public List<TaskLogs> getTaskLogsListInOneHour(int startId, String startTime, String endTime, int offset, int limit) {

        return (List<TaskLogs>) getQuery("logs", null, startTime, endTime, null, null, offset, limit);

    }


    @Override
    public List<Map> getTaskNameOutPoolCount(String oneDayStart, String oneDayEnd) {
        List<Map> result = new ArrayList<>();
        Map<String, Object> resultMap = (Map<String, Object>) getAggregationQuery("count", EsConstant.TASK_LOG_TASK_NAME, 100,null,"3", oneDayStart, oneDayEnd,null,null);
        for (Map.Entry<String, Object> stringIntegerEntry : resultMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("task_name", stringIntegerEntry.getKey());
            map.put("count(*)", stringIntegerEntry.getValue());
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map> getTaskNameInPoolCount(String oneDayStart, String oneDayEnd) {
        List<Map> result = new ArrayList<>();
        Map<String, Object> resultMap = (Map<String, Object>) getAggregationQuery("count", EsConstant.TASK_LOG_TASK_NAME, 100,null,"2", oneDayStart, oneDayEnd,null,null);
        for (Map.Entry<String, Object> stringIntegerEntry : resultMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("task_name", stringIntegerEntry.getKey());
            map.put("count(*)", stringIntegerEntry.getValue());
            result.add(map);
        }
        return result;
    }

    private Object getQuery(String type, String event, String start, int offset, int size) {
        return getQuery(type, event, start, null, offset, size);
    }

    private Object getQuery(String type, String event, String start, String end, int offset, int size) {
        return getQuery(type, null, event, start, end, null, null, offset, size);
    }

    private Object getQuery(String type, String event, String start, String end, String hasFile, String retryTimes, int offset, int size) {
        return getQuery(type, null, event, start, end, hasFile, retryTimes, offset, size);
    }

    private Object getQuery(String type, String hashKeyList, String event, String start, String end, String hasFile, String retryTimes, int offset, int size) {
        return getQuery(type, hashKeyList, null, event, start, end, hasFile, retryTimes, offset, size);
    }

    /**
     * 条件查询
     *
     * @param type
     * @param hashKeyList
     * @param taskSourceId
     * @param event
     * @param start
     * @param end
     * @param hasFile
     * @param retryTimes
     * @param offset
     * @param size
     * @return
     */
    private Object getQuery(String type, String hashKeyList, String taskSourceId, String event, String start, String end, String hasFile, String retryTimes, int offset, int size) {
        Map<String, Object> resultMap = new HashMap<>();
        List<TaskLogs> logs = new ArrayList<>();
        List<Map> result = new ArrayList<>();
        List<String> resultString = new ArrayList<>();
        Long total = 0L;
        List<String> strs = null;
        if (!StringUtils.isEmpty(hashKeyList)) {
            String str = hashKeyList.replaceAll("\\(", "").replaceAll("\\)", "");
            strs = Arrays.asList(str.split(","));
        }

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(event)) {
            boolQuery.must(QueryBuilders.termQuery(EsConstant.TASK_LOG_EVENT, event));
        }
        if (!StringUtils.isEmpty(taskSourceId)) {
            boolQuery.must(QueryBuilders.termQuery(EsConstant.TASK_LOG_TASK_SOURCE_ID, taskSourceId));
        }
        if (!StringUtils.isEmpty(hasFile)) {
            boolQuery.must(QueryBuilders.termQuery(EsConstant.TASK_LOG_HAS_FILE, hasFile));
        }
        if (!StringUtils.isEmpty(retryTimes)) {
            boolQuery.must(QueryBuilders.rangeQuery(EsConstant.TASK_LOG_RETRY_TIMES).from(retryTimes));
        }
        try {
            Object startObject = EsUtils.typeConvert(DateUtils.convertDate(start), EsConstant.TASK_LOG_DATE);
            QueryBuilder rangeQueryBuilder = null;
            if (!StringUtils.isEmpty(end)) {
                Object endObject = EsUtils.typeConvert(DateUtils.convertDate(end), EsConstant.TASK_LOG_DATE);
                rangeQueryBuilder = QueryBuilders.rangeQuery(EsConstant.TASK_LOG_CREATED_TIME).from(startObject).to(endObject);
            } else {
                rangeQueryBuilder = QueryBuilders.rangeQuery(EsConstant.TASK_LOG_CREATED_TIME).from(startObject);
            }
            boolQuery.must(rangeQueryBuilder);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        TermsAggregationBuilder teamAgg = AggregationBuilders.terms("count").field("task_name.keyword");

        ESConnection.ESSearchRequest esSearchRequest = null;
        if ("logs".equals(type)||"logsAll".equals(type)){
             esSearchRequest = esConnection.new ESSearchRequest(EsConstant.TASK_LOGS_INDEX,
                     EsConstant.QUERY_TYPE_DOC).setQuery(boolQuery).setSort(EsConstant.TASK_LOG_CREATED_TIME, EsConstant.QUERY_SORT_DESC).form(offset).size(EsConstant.SIZE);
            SearchResponse response = esSearchRequest.getResponse();
            for (SearchHit hit : response.getHits().getHits()){
                if ("logsAll".equals(type)) {
                    getTaskLogs(hit, logs, false);
                } else if ("logs".equals(type)) {
                    getTaskLogs(hit, logs, true);
                }
            }
            return logs;
        }else {
            esSearchRequest = esConnection.new ESSearchRequest(EsConstant.TASK_LOGS_INDEX,
                    EsConstant.QUERY_TYPE_DOC).setScroll(1L).setQuery(boolQuery).setAggregation(EsConstant.TASK_LOG_TASK_NAME, 100)
                    .setSort(EsConstant.TASK_LOG_CREATED_TIME, EsConstant.QUERY_SORT_DESC).size(EsConstant.SIZE);
        }


        SearchResponse response = esSearchRequest.getResponse();
        Map<String, Aggregation> aggregationMap = response.getAggregations().getAsMap();
        Aggregation count = aggregationMap.get("count");

        String scrollId = response.getScrollId();

        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println(hit.getSourceAsString());
            if ("logsAll".equals(type)) {
                getTaskLogs(hit, logs, false);
//            } else if ("logs".equals(type)) {
//                getTaskLogs(hit, logs, true);
            } else if ("continentsAccount".equals(type)) {
                getResultMapByContinentAccount(hit, resultMap);
            } else if ("FailureReasonsStat".equals(type)) {
                getFailureReasonsStat(hit, resultMap, result);
            } else if ("FromSecondDay".equals(type)) {
                getFromSecondDay(hit, hashKeyList, strs, resultString);
            } else if ("HashKeysList".equals(type)) {
                getHashKeysList(hit, resultString);
            }
        }


        while (true) {
            SearchResponse scrollResponse = esSearchRequest.getResponseScroll(scrollId, 1L);
            SearchHit[] hits = scrollResponse.getHits().getHits();
            if (hits != null && hits.length > 0) {

                for (SearchHit hit : hits) {
                    if ("logsAll".equals(type)) {
                        getTaskLogs(hit, logs, false);
//                    } else if ("logs".equals(type)) {
//                        getTaskLogs(hit, logs, true);
                    } else if ("continentsAccount".equals(type)) {
                        getResultMapByContinentAccount(hit, resultMap);
                    } else if ("FailureReasonsStat".equals(type)) {
                        getFailureReasonsStat(hit, resultMap, result);
                    } else if ("FromSecondDay".equals(type)) {
                        getFromSecondDay(hit, hashKeyList, strs, resultString);
                    } else if ("HashKeysList".equals(type)) {
                        getHashKeysList(hit, resultString);
                    }
                }

            } else {
                break;
            }
        }
        ClearScrollResponse scrollResponse = esSearchRequest.clearScroll(scrollId);
        System.out.println(scrollResponse.status());
        if ("continentsAccount".equals(type)) {
            return resultMap;
        } else if ("logs".equals(type) || "logsAll".equals(type)) {
            return logs;
        } else if ("FailureReasonsStat".equals(type)) {
            return result;
        } else if ("FromSecondDay".equals(type) || "HashKeysList".equals(type)) {
            return resultString;
        }
        return null;
    }

    //新增只用时间查询接口
    private SearchResponse getLogsByStartEnd(String startTime,String endTime){
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        try {
            Object startObject = EsUtils.typeConvert(DateUtils.convertDate(startTime), EsConstant.TASK_LOG_DATE);
            QueryBuilder rangeQueryBuilder = null;
            if (!StringUtils.isEmpty(endTime)) {
                Object endObject = EsUtils.typeConvert(DateUtils.convertDate(endTime), EsConstant.TASK_LOG_DATE);
                rangeQueryBuilder = QueryBuilders.rangeQuery(EsConstant.TASK_LOG_CREATED_TIME).from(startObject).to(endObject);
            } else {
                rangeQueryBuilder = QueryBuilders.rangeQuery(EsConstant.TASK_LOG_CREATED_TIME).from(startObject);
            }
            boolQuery.must(rangeQueryBuilder);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ESConnection.ESSearchRequest esSearchRequest = esConnection.new ESSearchRequest(EsConstant.TASK_LOGS_INDEX,
                EsConstant.QUERY_TYPE_DOC).setQuery(boolQuery).setSort(EsConstant.TASK_LOG_CREATED_TIME, EsConstant.QUERY_SORT_DESC);
        SearchResponse response = esSearchRequest.getResponse();
        return response;
    }


    private Object getAggregationQuery(String type, String aggregationField, int aggregationSize, String event, String start, String end) {
        return getAggregationQuery(type, aggregationField, aggregationSize, null, event, start, end, null, null);
    }

    private Object getAggregationQuery(String type, String aggregationField, int aggregationSize, List<String> hashKeyList, String event, String start, String end, String hasFile, String retryTimes) {
        Map<String, Object> resultMap = new HashMap<>();
        List<String> keys = new ArrayList<>();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(event)) {
            boolQuery.must(QueryBuilders.termQuery(EsConstant.TASK_LOG_EVENT, event));
        }
//        if (!StringUtils.isEmpty(taskSourceId)) {
//            boolQuery.must(QueryBuilders.termQuery("taskSourceId", taskSourceId));
//        }
        if (!StringUtils.isEmpty(hasFile)) {
            boolQuery.must(QueryBuilders.termQuery(EsConstant.TASK_LOG_HAS_FILE, hasFile));
        }
        if (!StringUtils.isEmpty(retryTimes)) {
            boolQuery.must(QueryBuilders.termQuery(EsConstant.TASK_LOG_RETRY_TIMES,retryTimes));
        }

        if (hashKeyList != null && !hashKeyList.isEmpty()) {
            hashKeyList.forEach((hashKey) -> boolQuery.should(QueryBuilders.matchPhraseQuery(EsConstant.TASK_LOG_HASH_KEY, hashKey)));
            boolQuery.minimumShouldMatch(1);
        }

        try {
            Object startObject = EsUtils.typeConvert(DateUtils.convertDate(start), EsConstant.TASK_LOG_DATE);
            QueryBuilder rangeQueryBuilder = null;
            if (!StringUtils.isEmpty(end)) {
                Object endObject = EsUtils.typeConvert(DateUtils.convertDate(end), EsConstant.TASK_LOG_DATE);
                rangeQueryBuilder = QueryBuilders.rangeQuery(EsConstant.TASK_LOG_CREATED_TIME).from(startObject).to(endObject);
            } else {
                rangeQueryBuilder = QueryBuilders.rangeQuery(EsConstant.TASK_LOG_CREATED_TIME).from(startObject);
            }
            boolQuery.must(rangeQueryBuilder);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ESConnection.ESSearchRequest esSearchRequest = esConnection.new ESSearchRequest(EsConstant.TASK_LOGS_INDEX,
                EsConstant.QUERY_TYPE_DOC).setScroll(1L).setQuery(boolQuery).setAggregation(aggregationField, aggregationSize).setSort(EsConstant.TASK_LOG_CREATED_TIME, EsConstant.QUERY_SORT_DESC).size(1);
        SearchResponse response = esSearchRequest.getResponse();
        Map<String, Aggregation> aggregationMap = response.getAggregations().getAsMap();
        if ("totalHits".equals(type)) {
            return response.getHits().totalHits;
        }
        Aggregation count = aggregationMap.get("count");
        if ("count".equals(type)) {
            ((ParsedStringTerms) count).getBuckets().forEach((bucket) -> {
                resultMap.put(String.valueOf(bucket.getKey()), bucket.getDocCount());
            });
            return resultMap;
        } else if ("HashKeysList".equals(type)) {
            ((ParsedLongTerms) count).getBuckets().forEach((bucket) -> keys.add(String.valueOf(bucket.getKey())));
            return keys;
        } else if ("totalHits".equals(type)) {
            return response.getHits().totalHits;
        }
        return new Object();
    }


    /**
     * 组装task_name、count(*)结果集合
     *
     * @param event
     * @param start
     * @param end
     * @param hashKeyList
     * @param hasFile
     * @return
     */
    private List<Map> getResultByEvent(String event, String start, String end, List<String> hashKeyList, String hasFile) {
        Map<String, Object> resultMap = (Map<String, Object>) getAggregationQuery("count", EsConstant.TASK_LOG_TASK_NAME, EsConstant.SIZE, hashKeyList, event, start, end, hasFile, null);
        List<Map> result = new ArrayList<>();
        for (Map.Entry<String, Object> stringIntegerEntry : resultMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("task_name", stringIntegerEntry.getKey());
            map.put("count(*)", stringIntegerEntry.getValue());
            result.add(map);
        }
        return result;
    }

    private void getHashKeysList(SearchHit hit, List<String> result) {
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        TaskLogs taskLogs = JSON.parseObject(JSON.toJSONString(sourceAsMap), TaskLogs.class);
        String hashKey = String.valueOf(taskLogs.getHashKey());
        if (!result.contains(hashKey)) {
            result.add(hashKey);
        }
    }

    private void getFromSecondDay(SearchHit hit, String hashKeyList, List<String> strs, List<String> result) {
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        TaskLogs taskLogs = JSON.parseObject(JSON.toJSONString(sourceAsMap), TaskLogs.class);
        String hashKey = String.valueOf(taskLogs.getHashKey());
        if (StringUtils.isEmpty(hashKeyList) || strs.contains(hashKey)) {
            if (!result.contains(hashKey)) {
                result.add(hashKey);
            }
        }

    }

    private void getFailureReasonsStat(SearchHit hit, Map<String, Object> resultMap, List<Map> result) {
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        TaskLogs taskLogs = JSON.parseObject(JSON.toJSONString(sourceAsMap), TaskLogs.class);
        resultMap.put("hash_key", taskLogs.getHashKey());
        resultMap.put("task_name", taskLogs.getTaskName());
        resultMap.put(EsConstant.TASK_LOG_ACCOUNT, taskLogs.getAccount());
        resultMap.put(EsConstant.TASK_LOG_CONTINENT, taskLogs.getContinent());
        resultMap.put("center_params", taskLogs.getCenterParams());
        resultMap.put("client_params", taskLogs.getClientParams());
        if (StringUtils.isEmpty(taskLogs.getRefreshTime())) {
            resultMap.put("retry_times", 0);
        } else {
            resultMap.put("retry_times", taskLogs.getRefreshTime());
        }
        resultMap.put(EsConstant.TASK_LOG_EXPLAIN, taskLogs.getExplain());
        result.add(resultMap);
    }


    private void getResultMapByContinentAccount(SearchHit hit, Map<String, Object> resultMap) {
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        TaskLogs taskLogs = JSON.parseObject(JSON.toJSONString(sourceAsMap), TaskLogs.class);
        String key = taskLogs.getContinent() + "," + taskLogs.getAccount();
        if (!resultMap.containsKey(key)) {
            resultMap.put(key, taskLogs.getTaskName());
        } else {
            resultMap.put(key, resultMap.get(key) + "," + taskLogs.getTaskName());
        }
    }


    private void getTaskLogs(SearchHit hit, List<TaskLogs> logs, boolean flag) {
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        TaskLogs taskLogs = JSON.parseObject(JSON.toJSONString(sourceAsMap), TaskLogs.class);
        if (flag) {
            if (!StringUtils.isEmpty(taskLogs.getAccount())) {
                logs.add(taskLogs);
            }
        } else {
            logs.add(taskLogs);
        }
    }

    private void getResultMap(SearchHit hit, String hashKeyList, Map<String, Object> resultMap, List<String> strs) {
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        TaskLogs taskLogs = JSON.parseObject(JSON.toJSONString(sourceAsMap), TaskLogs.class);
        String hashKey = String.valueOf(taskLogs.getHashKey());
        String taskName = String.valueOf(taskLogs.getTaskName());
        if (StringUtils.isEmpty(hashKeyList) || strs.contains(hashKey)) {
            if (!resultMap.containsKey(taskName)) {
                resultMap.put(taskName, 1L);
            } else {
                resultMap.put(taskName, Long.parseLong(String.valueOf(resultMap.get(taskName))) + 1L);
            }
        }
    }

}
