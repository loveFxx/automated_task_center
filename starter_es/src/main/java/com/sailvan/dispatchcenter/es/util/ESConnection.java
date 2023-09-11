package com.sailvan.dispatchcenter.es.util;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.sailvan.dispatchcenter.es.config.EsMarkerConfiguration;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import java.io.IOException;
import java.util.Map;


/**
 * ES 连接器
 * @date 2022-03
 * @author menghui
 */
@ConditionalOnBean(EsMarkerConfiguration.EsMarker.class)
public class ESConnection {

    private static final Logger logger = LoggerFactory.getLogger(ESConnection.class);

    public enum ESClientMode {
        TRANSPORT, REST
    }

    private ESClientMode mode;

    private TransportClient transportClient;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public void close() {
        if (mode == ESClientMode.TRANSPORT) {
            transportClient.close();
        } else {
            try {
                restHighLevelClient.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public MappingMetaData getMapping(String index, String type) {
        MappingMetaData mappingMetaData = null;
        if (mode == ESClientMode.TRANSPORT) {
            ImmutableOpenMap<String, MappingMetaData> mappings;
            try {
                mappings = transportClient.admin()
                        .cluster()
                        .prepareState()
                        .execute()
                        .actionGet()
                        .getState()
                        .getMetaData()
                        .getIndices()
                        .get(index)
                        .getMappings();
            } catch (NullPointerException e) {
                throw new IllegalArgumentException("Not found the mapping info of index: " + index);
            }
            mappingMetaData = mappings.get(type);

        } else {
            ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings;
            try {
                GetMappingsRequest request = new GetMappingsRequest();
                request.indices(index);
                GetMappingsResponse response;
                // try {
                // response = restHighLevelClient
                // .indices()
                // .getMapping(request, RequestOptions.DEFAULT);
                // // 6.4以下版本直接使用该接口会报错
                // } catch (Exception e) {
                // logger.warn("Low ElasticSearch version for getMapping");
//                response = RestHighLevelClientExt.getMapping(restHighLevelClient, request, RequestOptions.DEFAULT);
                // }
                response = restHighLevelClient
                 .indices()
                 .getMapping(request, RequestOptions.DEFAULT);
                mappings = response.mappings();
            } catch (NullPointerException e) {
                throw new IllegalArgumentException("Not found the mapping info of index: " + index);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return null;
            }
            String indexTmp = "";
            for (ObjectCursor<String> key : mappings.keys()) {
                String s = String.valueOf(key.value);
                String prefix = index+"-";
                if (s.startsWith(prefix)) {
//                    String after = s.replaceAll(prefix, "");
//                    EsUtils.regularFormat(after,after);
                    indexTmp = s;
                }
            }
            mappingMetaData = mappings.get(indexTmp).get(type);
        }
        return mappingMetaData;
    }


    public class ESIndexRequest {

        private IndexRequestBuilder indexRequestBuilder;

        private IndexRequest indexRequest;

        public ESIndexRequest(String index, String type, String id){
            if (mode == ESClientMode.TRANSPORT) {
                indexRequestBuilder = transportClient.prepareIndex(index, type, id);
            } else {
                indexRequest = new IndexRequest(index, type, id);
            }
        }

        public ESIndexRequest setSource(Map<String, ?> source) {
            if (mode == ESClientMode.TRANSPORT) {
                indexRequestBuilder.setSource(source);
            } else {
                indexRequest.source(source);
            }
            return this;
        }

        public ESIndexRequest setRouting(String routing) {
            if (mode == ESClientMode.TRANSPORT) {
                indexRequestBuilder.setRouting(routing);
            } else {
                indexRequest.routing(routing);
            }
            return this;
        }

        public IndexRequestBuilder getIndexRequestBuilder() {
            return indexRequestBuilder;
        }

        public void setIndexRequestBuilder(IndexRequestBuilder indexRequestBuilder) {
            this.indexRequestBuilder = indexRequestBuilder;
        }

        public IndexRequest getIndexRequest() {
            return indexRequest;
        }

        public void setIndexRequest(IndexRequest indexRequest) {
            this.indexRequest = indexRequest;
        }
    }

    public class ESUpdateRequest {

        private UpdateRequestBuilder updateRequestBuilder;

        private UpdateRequest updateRequest;

        public ESUpdateRequest(String index, String type, String id){
            if (mode == ESClientMode.TRANSPORT) {
                updateRequestBuilder = transportClient.prepareUpdate(index, type, id);
            } else {
                updateRequest = new UpdateRequest(index, type, id);
                updateRequest.retryOnConflict(3);
            }
        }

        public ESUpdateRequest setDoc(Map source) {
            if (mode == ESClientMode.TRANSPORT) {
                updateRequestBuilder.setDoc(source);
            } else {
                updateRequest.doc(source);
            }
            return this;
        }

        public ESUpdateRequest setScript(Script script) {
            if (mode == ESClientMode.TRANSPORT) {
//                updateRequestBuilder.setScript(script);
            } else {
                updateRequest.script(script);
            }
            return this;
        }

        public ESUpdateRequest setDocAsUpsert(boolean shouldUpsertDoc) {
            if (mode == ESClientMode.TRANSPORT) {
                updateRequestBuilder.setDocAsUpsert(shouldUpsertDoc);
            } else {
                updateRequest.docAsUpsert(shouldUpsertDoc);
            }
            return this;
        }

        public ESUpdateRequest setRouting(String routing) {
            if (mode == ESClientMode.TRANSPORT) {
                updateRequestBuilder.setRouting(routing);
            } else {
                updateRequest.routing(routing);
            }
            return this;
        }

        public ESUpdateRequest setUpsert(Map upsert)
        {
            if (mode == ESClientMode.TRANSPORT) {
                updateRequestBuilder.setUpsert(upsert);
            } else {
                updateRequest.upsert(upsert);
            }
            return this;
        }

        public ESUpdateRequest setScriptedUpsert(boolean shouldScriptedUpsert)
        {
            if (mode == ESClientMode.TRANSPORT) {
                updateRequestBuilder.setScriptedUpsert(shouldScriptedUpsert);
            } else {
                updateRequest.scriptedUpsert(shouldScriptedUpsert);
            }
            return this;
        }

        public UpdateRequestBuilder getUpdateRequestBuilder() {
            return updateRequestBuilder;
        }

        public void setUpdateRequestBuilder(UpdateRequestBuilder updateRequestBuilder) {
            this.updateRequestBuilder = updateRequestBuilder;
        }

        public UpdateRequest getUpdateRequest() {
            return updateRequest;
        }

        public void setUpdateRequest(UpdateRequest updateRequest) {
            this.updateRequest = updateRequest;
        }
    }

    public class ESDeleteRequest {

        private DeleteRequestBuilder deleteRequestBuilder;

        private DeleteRequest deleteRequest;

        public ESDeleteRequest(String index, String type, String id){
            if (mode == ESClientMode.TRANSPORT) {
                deleteRequestBuilder = transportClient.prepareDelete(index, type, id);
            } else {
                deleteRequest = new DeleteRequest(index, type, id);
            }
        }

        public DeleteRequestBuilder getDeleteRequestBuilder() {
            return deleteRequestBuilder;
        }

        public void setDeleteRequestBuilder(DeleteRequestBuilder deleteRequestBuilder) {
            this.deleteRequestBuilder = deleteRequestBuilder;
        }

        public DeleteRequest getDeleteRequest() {
            return deleteRequest;
        }

        public void setDeleteRequest(DeleteRequest deleteRequest) {
            this.deleteRequest = deleteRequest;
        }

        public ESDeleteRequest setRouting(String routing) {
            if (mode == ESClientMode.TRANSPORT) {
                deleteRequestBuilder.setRouting(routing);
            } else {
                deleteRequest.routing(routing);
            }
            return this;
        }
    }

    public class ESSearchRequest {

        private SearchRequestBuilder searchRequestBuilder;

        private SearchRequest searchRequest;

        private SearchSourceBuilder sourceBuilder;

        public ESSearchRequest(String index, String... types){
            if (mode == ESClientMode.TRANSPORT) {
                searchRequestBuilder = transportClient.prepareSearch(index).setTypes(types);
            } else {
                searchRequest = new SearchRequest(index).types(types);
                sourceBuilder = new SearchSourceBuilder();
            }
        }

        public ESSearchRequest setQuery(QueryBuilder queryBuilder) {
            if (mode == ESClientMode.TRANSPORT) {
                searchRequestBuilder.setQuery(queryBuilder);
            } else {
                sourceBuilder.query(queryBuilder);
            }
            return this;
        }

        public ESSearchRequest setScroll(Long time) {
            searchRequest.scroll(TimeValue.timeValueMinutes(time));
            return this;
        }
        public ESSearchRequest setAggregation(String field, int size) {
            sourceBuilder.aggregation(AggregationBuilders.terms("count").field(field).size(size));
            return this;
        }

        public ESSearchRequest setSort(String field, String rule) {
            if (mode == ESClientMode.TRANSPORT) {
//                searchRequestBuilder.setQuery(queryBuilder);
            } else {
                if(!StringUtils.isEmpty(rule) && rule.equals("desc")){
                    sourceBuilder.sort(field, SortOrder.DESC);
                }else {
                    sourceBuilder.sort(field, SortOrder.ASC);
                }

            }
            return this;
        }

        public ESSearchRequest size(int size) {
            if (mode == ESClientMode.TRANSPORT) {
                searchRequestBuilder.setSize(size);
            } else {
                sourceBuilder.size(size);
            }
            return this;
        }

        public ESSearchRequest form(int offset) {
            if (mode == ESClientMode.TRANSPORT) {
                searchRequestBuilder.setFrom(offset);
            } else {
                sourceBuilder.from(offset);
            }
            return this;
        }

        public SearchResponse getResponse() {
            if (mode == ESClientMode.TRANSPORT) {
                return searchRequestBuilder.get();
            } else {
                searchRequest.source(sourceBuilder);
                try {
                    return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public SearchResponse getResponseScroll(String scrollId, Long time) {
            if (mode == ESClientMode.TRANSPORT) {
                return searchRequestBuilder.get();
            } else {
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(TimeValue.timeValueMinutes(time));
                try {
                    return restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public ClearScrollResponse  clearScroll(String scrollId) {
            ClearScrollRequest scrollRequest = new ClearScrollRequest();
            scrollRequest.addScrollId(scrollId);
            try {
                return restHighLevelClient.clearScroll(scrollRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public SearchRequestBuilder getSearchRequestBuilder() {
            return searchRequestBuilder;
        }

        public void setSearchRequestBuilder(SearchRequestBuilder searchRequestBuilder) {
            this.searchRequestBuilder = searchRequestBuilder;
        }

        public SearchRequest getSearchRequest() {
            return searchRequest;
        }

        public void setSearchRequest(SearchRequest searchRequest) {
            this.searchRequest = searchRequest;
        }
    }

    public class ESUpdateByQueryRequest
    {
        private UpdateByQueryRequest updateByQueryRequest;

        public ESUpdateByQueryRequest(String index, String... types){
            if (mode == ESClientMode.TRANSPORT) {
//                searchRequestBuilder = transportClient.prepareSearch(index).setTypes(types);
            } else {
                updateByQueryRequest = new UpdateByQueryRequest(index);
                updateByQueryRequest.setDocTypes(types);
                updateByQueryRequest.setConflicts("proceed");
            }
        }

        public ESUpdateByQueryRequest setQuery(QueryBuilder query)
        {
            updateByQueryRequest.setQuery(query);
            return this;
        }

        public ESUpdateByQueryRequest setSize(int size) {
            if (mode == ESClientMode.TRANSPORT) {
//                searchRequestBuilder.setSize(size);
            } else {
                updateByQueryRequest.setSize(size);
            }
            return this;
        }

        public ESUpdateByQueryRequest setScript(Script script)
        {
            if (mode == ESClientMode.TRANSPORT) {
//                searchRequestBuilder.setScript(script);
            } else {
                updateByQueryRequest.setScript(script);
            }
            return this;
        }

        public BulkByScrollResponse getResponse() {
            if (mode == ESClientMode.TRANSPORT) {
//                return searchRequestBuilder.get();
                return null;
            } else {
                try {
                    return restHighLevelClient.updateByQuery(updateByQueryRequest, RequestOptions.DEFAULT);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void getResponseAsync() {
            if (mode == ESClientMode.TRANSPORT) {
//                return searchRequestBuilder.get();
                return;
            } else {
                try {
                    restHighLevelClient.updateByQueryAsync(updateByQueryRequest, RequestOptions.DEFAULT, new ActionListener<BulkByScrollResponse>() {
                        @Override
                        public void onResponse(BulkByScrollResponse bulkResponse) {
                            //成功的时候执行
                        }
                        @Override
                        public void onFailure(Exception e) {
                            //失败的时候执行
                            throw new RuntimeException(e);
                        }
                    });
                    return ;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    public class ESBulkRequest {

        private BulkRequestBuilder bulkRequestBuilder;

        private BulkRequest bulkRequest;

        public ESBulkRequest(){
            if (mode == ESClientMode.TRANSPORT) {
                bulkRequestBuilder = transportClient.prepareBulk();
            } else {
                bulkRequest = new BulkRequest();
//                bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
            }
        }

        public void resetBulk() {
            if (mode == ESClientMode.TRANSPORT) {
                bulkRequestBuilder = transportClient.prepareBulk();
            } else {
                bulkRequest = new BulkRequest();
//                bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
            }
        }

        public ESBulkRequest add(ESIndexRequest esIndexRequest) {
            if (mode == ESClientMode.TRANSPORT) {
                bulkRequestBuilder.add(esIndexRequest.indexRequestBuilder);
            } else {
                bulkRequest.add(esIndexRequest.indexRequest);
            }
            return this;
        }

        public ESBulkRequest setRefreshPolicy(String policy) {
            if (mode == ESClientMode.TRANSPORT) {
                bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
            } else {
                if(StringUtils.isNotEmpty(policy) && policy.equalsIgnoreCase("wait_for")){
                    bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
                }else if(StringUtils.isNotEmpty(policy) && policy.equalsIgnoreCase("false")){
                    bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.NONE);
                }else if(StringUtils.isNotEmpty(policy) && policy.equalsIgnoreCase("true")){
                    bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
                }
            }
            return this;
        }

        public ESBulkRequest add(ESUpdateRequest esUpdateRequest) {
            if (mode == ESClientMode.TRANSPORT) {
                bulkRequestBuilder.add(esUpdateRequest.updateRequestBuilder);
            } else {
                bulkRequest.add(esUpdateRequest.updateRequest);
            }
            return this;
        }

        public ESBulkRequest add(ESDeleteRequest esDeleteRequest) {
            if (mode == ESClientMode.TRANSPORT) {
                bulkRequestBuilder.add(esDeleteRequest.deleteRequestBuilder);
            } else {
                bulkRequest.add(esDeleteRequest.deleteRequest);
            }
            return this;
        }

        public int numberOfActions() {
            if (mode == ESClientMode.TRANSPORT) {
                return bulkRequestBuilder.numberOfActions();
            } else {
                return bulkRequest.numberOfActions();
            }
        }

        public BulkResponse bulk() {
            if (mode == ESClientMode.TRANSPORT) {
                return bulkRequestBuilder.execute().actionGet();
            } else {
                try {
                    return restHighLevelClient.bulk(bulkRequest);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public BulkRequestBuilder getBulkRequestBuilder() {
            return bulkRequestBuilder;
        }

        public void setBulkRequestBuilder(BulkRequestBuilder bulkRequestBuilder) {
            this.bulkRequestBuilder = bulkRequestBuilder;
        }

        public BulkRequest getBulkRequest() {
            return bulkRequest;
        }

        public void setBulkRequest(BulkRequest bulkRequest) {
            this.bulkRequest = bulkRequest;
        }

        public void commit() {
            if (bulkRequest.numberOfActions() > 0) {
                BulkResponse response = bulk();
                if (response.hasFailures()) {
                    for (BulkItemResponse itemResponse : response.getItems()) {
                        if (!itemResponse.isFailed()) {
                            continue;
                        }

                        if (itemResponse.getFailure().getStatus() == RestStatus.NOT_FOUND) {
                            logger.error(itemResponse.getFailureMessage());
                        } else {
                            throw new RuntimeException("ES sync commit error" + itemResponse.getFailureMessage());
                        }
                    }
                }
                resetBulk();
            }
        }
    }

    // ------ get/set ------
    public ESClientMode getMode() {
        return mode;
    }

    public void setMode(ESClientMode mode) {
        this.mode = mode;
    }

    public TransportClient getTransportClient() {
        return transportClient;
    }

    public void setTransportClient(TransportClient transportClient) {
        this.transportClient = transportClient;
    }

    public RestHighLevelClient getRestHighLevelClient() {
        return restHighLevelClient;
    }

    public void setRestHighLevelClient(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }
}
