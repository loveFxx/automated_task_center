package com.sailvan.dispatchcenter.shard.algorithm;

import com.sailvan.dispatchcenter.shard.config.MysqlShardMarkerConfiguration;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import java.util.Collection;

/**
 * 分库算法
 * @deprecated 暂时不用
 * @date 2021-10
 * @author menghui
 */
@ConditionalOnBean(MysqlShardMarkerConfiguration.MysqlShardMarker.class)
public class DBShardAlgo implements PreciseShardingAlgorithm<String> {
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<String> preciseShardingValue) {
        String db_name="automated_task_center_local";
//        Long num = preciseShardingValue.getValue()%2;
//        db_name = db_name + num;
//        for (String each : collection) {
//            if (each.equals(db_name)) {
//                return each;
//            }
//        }
//        throw new IllegalArgumentException();
        return db_name;
    }

}
