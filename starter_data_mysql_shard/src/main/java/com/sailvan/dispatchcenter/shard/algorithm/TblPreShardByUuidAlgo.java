package com.sailvan.dispatchcenter.shard.algorithm;

import com.sailvan.dispatchcenter.common.constant.CacheKey;
import com.sailvan.dispatchcenter.shard.config.MysqlShardMarkerConfiguration;
import com.sailvan.dispatchcenter.shard.utils.TableCapacityUtils;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import java.util.Collection;

/**
 * 分表精确算法 根据字符串ID
 * @deprecated 暂时不用
 * @date 2021-10
 * @author menghui
 */
@ConditionalOnBean(MysqlShardMarkerConfiguration.MysqlShardMarker.class)
public class TblPreShardByUuidAlgo implements PreciseShardingAlgorithm<String> {
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingColumn) {
        String value = shardingColumn.getValue();
        if (value.startsWith(CacheKey.CIRCLE)) {
            return getAvailableTable(availableTargetNames, shardingColumn, value, CacheKey.CIRCLE);
        }else if (value.startsWith(CacheKey.SINGLE)) {
            return getAvailableTable(availableTargetNames, shardingColumn, value, CacheKey.SINGLE);
        }else {
            throw new RuntimeException("任务库存在不支持的id前缀,只能是:"+CacheKey.CIRCLE+" 和 "+CacheKey.SINGLE);
        }
    }


    private String getAvailableTable(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingColumn, String value, String type){
        int id = Integer.parseInt(value.replaceAll(type+"_",""));
        int tab = TableCapacityUtils.getTableSuffix(id);
        String tableName = shardingColumn.getLogicTableName()+"_"+type+"_"+tab;
        if (availableTargetNames.contains(tableName)) {
            return tableName ;
        }
        return null;
    }

    
}
