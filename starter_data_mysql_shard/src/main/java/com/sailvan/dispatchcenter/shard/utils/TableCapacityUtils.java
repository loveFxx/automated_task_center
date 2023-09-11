package com.sailvan.dispatchcenter.shard.utils;

import com.sailvan.dispatchcenter.common.constant.CacheKey;
import com.sailvan.dispatchcenter.shard.config.MysqlShardMarkerConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

/**
 *  表名后缀
 * @author mh
 * @date 2021-11
 */
public class TableCapacityUtils {

    public static int getTableSuffix(Integer id){
        return  (id-1)/ CacheKey.SINGLE_TABLE_CAPACITY+1;
    }

    public static int getUpperValue(Integer lowerValue){
        return lowerValue + CacheKey.SINGLE_TABLE_CAPACITY * 2;
    }

    public static int getLowerValue(Integer upperValue){
        int diff = upperValue - CacheKey.SINGLE_TABLE_CAPACITY * 2;
        if(diff<0){
            return 0;
        }
        return diff;
    }
}
