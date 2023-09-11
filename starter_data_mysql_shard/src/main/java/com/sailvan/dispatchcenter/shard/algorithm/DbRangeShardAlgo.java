package com.sailvan.dispatchcenter.shard.algorithm;

import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.google.common.collect.Range;
import com.sailvan.dispatchcenter.shard.config.MysqlShardMarkerConfiguration;
import com.sailvan.dispatchcenter.shard.utils.DataFormat;
import lombok.SneakyThrows;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import java.util.*;

/**
 * 分库范围算法
 * @deprecated 暂时不用
 * @date 2021-10
 * @author menghui
 */
@ConditionalOnBean(MysqlShardMarkerConfiguration.MysqlShardMarker.class)
public class DbRangeShardAlgo implements RangeShardingAlgorithm<String> {
    @SneakyThrows
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<String> rangeShardingValue) {
        System.out.println("范围-*-*-*-*-*-*-*-*-*-*-*---------------"+availableTargetNames);
        System.out.println("范围-*-*-*-*-*-*-*-*-*-*-*---------------"+rangeShardingValue);
        Range<String> valueRange = rangeShardingValue.getValueRange();
        String lowerEnd = valueRange.lowerEndpoint();
        String upperEndpoint ;
        if (valueRange.hasUpperBound()) {
            upperEndpoint = valueRange.upperEndpoint();
        }else {
            upperEndpoint = DateUtils.getAfterStart(lowerEnd,1);
        }
        Collection<String> dbs = getRoutTable(availableTargetNames, rangeShardingValue.getLogicTableName(), DateUtils.convertDate(lowerEnd), DateUtils.convertDate(upperEndpoint));
        return dbs;
    }

    public Collection<String> getRoutTable(Collection<String> availableTargetNames, String logicTable, Date lowerEnd, Date upperEnd) {
        Set<String> routTables = new HashSet<>();
        if (lowerEnd != null && upperEnd != null) {
            List<String> rangeNameList = DataFormat.getRangeNameList(lowerEnd, upperEnd);
            for (String availableTargetName : availableTargetNames) {
                for (String string : rangeNameList) {
                    String currentYearFormat = DataFormat.getCurrentMonthYear(string);
                    String db = availableTargetName + "_" +currentYearFormat;
                    if(!routTables.contains(db)){
                        routTables.add(db);
                    }
                }
            }

        }
        return routTables;
    }



}
