package com.sailvan.dispatchcenter.shard.algorithm;

import com.google.common.collect.Range;
import com.sailvan.dispatchcenter.shard.config.MysqlShardMarkerConfiguration;
import com.sailvan.dispatchcenter.shard.utils.ShardingToolUtils;
import com.sailvan.dispatchcenter.shard.utils.TableCapacityUtils;
import lombok.SneakyThrows;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 分表范围算法 根据整形ID
 * @date 2021-10
 * @author menghui
 */
@ConditionalOnBean(MysqlShardMarkerConfiguration.MysqlShardMarker.class)
public class TblRangeShardByIdAlgo implements RangeShardingAlgorithm<Integer> {


    @Autowired
    ShardingToolUtils shardingToolUtils;

    @SneakyThrows
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Integer> rangeShardingValue) {
        System.out.println("范围-*-*-*-*-*-*-*-*-*-*-*---------------"+availableTargetNames);
        System.out.println("范围-*-*-*-*-*-*-*-*-*-*-*---------------"+rangeShardingValue);
        Range<Integer> valueRange = rangeShardingValue.getValueRange();
        Integer lowerEnd = valueRange.lowerEndpoint();
        Integer upperEndpoint ;
        if (valueRange.hasUpperBound()) {
            upperEndpoint = valueRange.upperEndpoint();
        }else {
            upperEndpoint = TableCapacityUtils.getUpperValue(lowerEnd);
        }
        Collection<String> tables = getRoutTable(availableTargetNames, rangeShardingValue.getLogicTableName(), lowerEnd, upperEndpoint);
        return tables;
    }

    public Collection<String> getRoutTable(Collection<String> availableTargetNames, String logicTable, Integer lowerEnd, Integer upperEnd) {
        Set<String> routTables = new HashSet<>();
        if (lowerEnd != null && upperEnd != null) {
            List<String> rangeNameList = getRangeNameList(lowerEnd, upperEnd);
            for (String string : rangeNameList) {
                String table = logicTable + "_" +string;
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



}
