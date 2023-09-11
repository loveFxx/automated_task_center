package com.sailvan.dispatchcenter.shard.algorithm;

import com.sailvan.dispatchcenter.common.constant.CacheKey;
import com.google.common.collect.Range;
import com.sailvan.dispatchcenter.shard.config.MysqlShardMarkerConfiguration;
import com.sailvan.dispatchcenter.shard.utils.TableCapacityUtils;
import lombok.SneakyThrows;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import java.util.*;

/**
 * 分表范围算法 根据整形ID
 * @deprecated 暂时不用
 * @author menghui
 * @date 2021-10
 */
@ConditionalOnBean(MysqlShardMarkerConfiguration.MysqlShardMarker.class)
public class TblRangeShardByUuidAlgo implements RangeShardingAlgorithm<String> {
    @SneakyThrows
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<String> rangeShardingValue) {
        System.out.println("范围-*-*-*-*-*-*-*-*-*-*-*---------------" + availableTargetNames);
        System.out.println("范围-*-*-*-*-*-*-*-*-*-*-*---------------" + rangeShardingValue);
        Range<String> valueRange = rangeShardingValue.getValueRange();
        String lowerEnd = valueRange.lowerEndpoint();
        String upperEndpoint;
        Collection<String> tables = null;
        if (valueRange.hasUpperBound()) {
            upperEndpoint = valueRange.upperEndpoint();
            if (lowerEnd.startsWith(CacheKey.CIRCLE)) {
                Integer lowerValue = Integer.parseInt(lowerEnd.replaceAll(CacheKey.CIRCLE + "_", ""));
                Integer upperValue = Integer.parseInt(upperEndpoint.replaceAll(CacheKey.CIRCLE + "_", ""));
                tables = getRoutTable(availableTargetNames, rangeShardingValue.getLogicTableName(), lowerValue, upperValue, CacheKey.CIRCLE);
            } else if (lowerEnd.startsWith(CacheKey.SINGLE)) {
                Integer lowerValue = Integer.parseInt(lowerEnd.replaceAll(CacheKey.SINGLE + "_", ""));
                Integer upperValue = Integer.parseInt(upperEndpoint.replaceAll(CacheKey.SINGLE + "_", ""));
                tables = getRoutTable(availableTargetNames, rangeShardingValue.getLogicTableName(), lowerValue, upperValue, CacheKey.SINGLE);
            }
        } else {
            if (lowerEnd.startsWith(CacheKey.CIRCLE)) {
                Integer lowerValue = Integer.parseInt(lowerEnd.replaceAll(CacheKey.CIRCLE + "_", ""));
                Integer upperValue = TableCapacityUtils.getUpperValue(lowerValue);
                tables = getRoutTable(availableTargetNames, rangeShardingValue.getLogicTableName(), lowerValue, upperValue, CacheKey.CIRCLE);
            } else if (lowerEnd.startsWith(CacheKey.SINGLE)) {
                Integer lowerValue = Integer.parseInt(lowerEnd.replaceAll(CacheKey.SINGLE + "_", ""));
                Integer upperValue = TableCapacityUtils.getUpperValue(lowerValue);
                tables = getRoutTable(availableTargetNames, rangeShardingValue.getLogicTableName(), lowerValue, upperValue, CacheKey.SINGLE);
            }
        }
        return tables;
    }

    public Collection<String> getRoutTable(Collection<String> availableTargetNames, String logicTable, Integer lowerEnd, Integer upperEnd, String type) {
        Set<String> routTables = new HashSet<>();
        if (lowerEnd != null && upperEnd != null) {
            List<String> rangeNameList = getRangeNameList(lowerEnd, upperEnd);
            for (String string : rangeNameList) {
                String table = logicTable + "_" + type + "_" + string;
                if (availableTargetNames.contains(table)) {
                    routTables.add(table);
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


}
