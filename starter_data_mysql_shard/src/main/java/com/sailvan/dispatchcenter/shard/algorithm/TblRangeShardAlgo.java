package com.sailvan.dispatchcenter.shard.algorithm;

import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.google.common.collect.Range;
import com.sailvan.dispatchcenter.shard.config.MysqlShardMarkerConfiguration;
import com.sailvan.dispatchcenter.shard.utils.ShardingToolUtils;
import com.sailvan.dispatchcenter.shard.utils.DataFormat;
import lombok.SneakyThrows;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 分表范围算法 根据时间
 * @date 2021-10
 * @author menghui
 */
@ConditionalOnBean(MysqlShardMarkerConfiguration.MysqlShardMarker.class)
public class TblRangeShardAlgo implements RangeShardingAlgorithm<String> {

    @Autowired
    ShardingToolUtils shardingToolUtils;

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
        Collection<String> tables = getRoutTable(availableTargetNames, rangeShardingValue.getLogicTableName(), DateUtils.convertDate(lowerEnd), DateUtils.convertDate(upperEndpoint));
        return tables;
    }

    public Collection<String> getRoutTable(Collection<String> availableTargetNames, String logicTable, Date lowerEnd, Date upperEnd) {
        Set<String> routTables = new HashSet<>();
        if (lowerEnd != null && upperEnd != null) {
            List<String> rangeNameList = DataFormat.getRangeNameList(lowerEnd, upperEnd);
            for (String string : rangeNameList) {
                String currentDayFormat = DataFormat.getCurrentDayFormat(string);
                String table = logicTable + "_" +currentDayFormat;
                String resultTableName = shardingToolUtils.shardingTablesCheckAndCreatAndReturn(logicTable, table);
                if(!StringUtils.isEmpty(resultTableName)){
                    routTables.add(resultTableName);
                }
            }
        }
        return routTables;
    }



}
