package com.sailvan.dispatchcenter.shard.algorithm;

import com.sailvan.dispatchcenter.common.constant.CacheKey;
import com.sailvan.dispatchcenter.shard.config.MysqlShardMarkerConfiguration;
import com.sailvan.dispatchcenter.shard.utils.ShardingToolUtils;
import com.sailvan.dispatchcenter.shard.utils.TableCapacityUtils;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;
import com.google.common.collect.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 分表精确算法 根据整形ID和is_single多个字段
 * @date 2021-10
 * @author menghui
 */
@ConditionalOnBean(MysqlShardMarkerConfiguration.MysqlShardMarker.class)
public class TblPreComplexByUuidAlgo implements ComplexKeysShardingAlgorithm {


    @Autowired
    ShardingToolUtils shardingToolUtils;

    @Override
    public Collection<String> doSharding(Collection collection, ComplexKeysShardingValue complexKeysShardingValue) {
        Map columnNameAndShardingValuesMap = complexKeysShardingValue.getColumnNameAndShardingValuesMap();

        Collection<Integer> id = (Collection<Integer>) columnNameAndShardingValuesMap.get("id");
        Collection<Integer> isSingle = (Collection<Integer>) columnNameAndShardingValuesMap.get("is_single");
        Range<Integer> valueRange = (Range<Integer>)complexKeysShardingValue.getColumnNameAndRangeValuesMap().get("id");
        if (valueRange != null && !valueRange.isEmpty() && (valueRange.hasUpperBound() || valueRange.hasLowerBound())) {
            //范围查询
            Collection<String> tables = null;
            if (valueRange.hasLowerBound() && valueRange.hasUpperBound()) {
                Integer lowerValue = valueRange.lowerEndpoint();
                Integer upperValue = valueRange.upperEndpoint();
                if (isSingle.contains(0)) {
                    tables = getRoutTable(collection, complexKeysShardingValue.getLogicTableName(), lowerValue, upperValue, CacheKey.CIRCLE);
                } else if (isSingle.contains(1)) {
                    tables = getRoutTable(collection, complexKeysShardingValue.getLogicTableName(), lowerValue, upperValue, CacheKey.SINGLE);
                }
            } else if (valueRange.hasLowerBound() && !valueRange.hasUpperBound()){
                Integer lowerValue = valueRange.lowerEndpoint();
                Integer upperValue = TableCapacityUtils.getUpperValue(lowerValue);
                if (isSingle.contains(0)) {
                    tables = getRoutTable(collection, complexKeysShardingValue.getLogicTableName(), lowerValue, upperValue, CacheKey.CIRCLE);
                } else if (isSingle.contains(1)) {
                    tables = getRoutTable(collection, complexKeysShardingValue.getLogicTableName(), lowerValue, upperValue, CacheKey.SINGLE);
                }
            }else if (!valueRange.hasLowerBound() && valueRange.hasUpperBound()){
                Integer upperValue = valueRange.upperEndpoint();
                Integer lowerValue = TableCapacityUtils.getLowerValue(upperValue);
                if (isSingle.contains(0)) {
                    tables = getRoutTable(collection, complexKeysShardingValue.getLogicTableName(), lowerValue, upperValue, CacheKey.CIRCLE);
                } else if (isSingle.contains(1)) {
                    tables = getRoutTable(collection, complexKeysShardingValue.getLogicTableName(), lowerValue, upperValue, CacheKey.SINGLE);
                }
            }else if (!valueRange.hasLowerBound() && !valueRange.hasUpperBound()){
                throw new RuntimeException("任务库存id前缀 没有最大值 也没有最小值");
            }
            return tables;
        }else if (id != null) {
            //精确查询
            Iterator it = id.iterator();
            List<String> list = new ArrayList<>();
            if(it == null){
                return list;
            }
            while(it.hasNext()) {
                int idPre = (int) it.next();
                int tab = TableCapacityUtils.getTableSuffix(idPre);
                String tableName = "";
                if(isSingle.contains(0)){
                    tableName = complexKeysShardingValue.getLogicTableName()+"_"+CacheKey.CIRCLE+"_"+tab;
                }else if(isSingle.contains(1)){
                    tableName = complexKeysShardingValue.getLogicTableName()+"_"+CacheKey.SINGLE+"_"+tab;
                }
                String resultTableName = shardingToolUtils.shardingTablesCheckAndCreatAndReturn(complexKeysShardingValue.getLogicTableName(), tableName);
                if(!StringUtils.isEmpty(resultTableName)){
                    list.add(resultTableName);
                }
//                if (collection.contains(tableName)) {
//                    list.add(tableName);
//                }
            }
            return list;
        }
        throw new RuntimeException("任务库存在不支持的id前缀,只能是:"+CacheKey.CIRCLE+" 和 "+CacheKey.SINGLE);
    }

    public Collection<String> getRoutTable(Collection<String> availableTargetNames, String logicTable, Integer lowerEnd, Integer upperEnd, String type) {
        Set<String> routTables = new HashSet<>();
        if (lowerEnd != null && upperEnd != null) {
            List<String> rangeNameList = getRangeNameList(lowerEnd, upperEnd);
            for (String string : rangeNameList) {
                String table = logicTable + "_" + type + "_" + string;
                String resultTableName = shardingToolUtils.shardingTablesCheckAndCreatAndReturn(logicTable, table);
                if(!StringUtils.isEmpty(resultTableName)){
                    routTables.add(resultTableName);
                }
            }
        }
        return routTables;
    }

    public List<String> getRangeNameList(Integer start, Integer end) {
        List<String> result = new ArrayList<>();

        int low = TableCapacityUtils.getTableSuffix(start);
        int up = TableCapacityUtils.getTableSuffix(end);
        for (int i = low; i <= up; i++) {
            result.add(String.valueOf(i));
        }
        return result;
    }

    private String getAvailableTable(Collection<String> availableTargetNames, String shardingColumn, String value, String type){
        int id = Integer.parseInt(value.replaceAll(type+"_",""));
        int tab = TableCapacityUtils.getTableSuffix(id);
        String tableName = shardingColumn+"_"+type+"_"+tab;
        if (availableTargetNames.contains(tableName)) {
            return tableName ;
        }
        return null;
    }
}
