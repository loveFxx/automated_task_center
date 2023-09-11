package com.sailvan.dispatchcenter.shard.algorithm;

import com.sailvan.dispatchcenter.shard.config.MysqlShardMarkerConfiguration;
import com.sailvan.dispatchcenter.shard.utils.DataFormat;
import com.sailvan.dispatchcenter.shard.utils.ShardingToolUtils;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * 分表精确算法 根据时间
 * @date 2021-10
 * @author menghui
 */
@ConditionalOnBean(MysqlShardMarkerConfiguration.MysqlShardMarker.class)
public class TblPreShardAlgo implements PreciseShardingAlgorithm<String> {

    @Autowired
    ShardingToolUtils shardingToolUtils;

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingColumn) {
        // 不分表
        String value = shardingColumn.getValue();
        String currentDayFormat = DataFormat.getCurrentDayFormat(value);
        String tableName = shardingColumn.getLogicTableName()+"_"+currentDayFormat;
        String resultTableName = shardingToolUtils.shardingTablesCheckAndCreatAndReturn(shardingColumn.getLogicTableName(), tableName);
        if(!StringUtils.isEmpty(resultTableName)){
            return resultTableName;
        }
        throw new IllegalArgumentException();
    }

    
}
