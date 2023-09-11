package com.sailvan.dispatchcenter.shard.utils;

import com.sailvan.dispatchcenter.shard.config.MysqlShardMarkerConfiguration;
import com.sailvan.dispatchcenter.shard.schdule.TableCreate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Lazy;

import java.util.HashSet;

/**
 * 自动创建表
 * @author mh
 * @date 21-10
 *
 */
@ConditionalOnBean(MysqlShardMarkerConfiguration.MysqlShardMarker.class)
public class ShardingToolUtils {

    @Autowired
    @Lazy
    TableCreate tableCreate;

    private static final HashSet<String> tableNameCache = new HashSet<>();


    /**
     * 判断 分表获取的表名是否存在 不存在则自动建表
     *
     * @param logicTableName  逻辑表名(表头)
     * @param resultTableName 真实表名
     * @return 确认存在于数据库中的真实表名
     */
    public String shardingTablesCheckAndCreatAndReturn(String logicTableName, String resultTableName) {

        synchronized (logicTableName.intern()) {
            // 缓存中有此表 返回
            if (shardingTablesExistsCheck(resultTableName)) {
                return resultTableName;
            }
            tableCreate.createNeedTime(logicTableName, "automated",resultTableName);
            // 缓存中无此表 建表 并添加缓存
            tableNameCache.add(resultTableName);
        }

        return resultTableName;
    }

    /**
     * 判断表是否存在于缓存中
     *
     * @param resultTableName 表名
     * @return 是否存在于缓存中
     */
    public boolean shardingTablesExistsCheck(String resultTableName) {
        return tableNameCache.contains(resultTableName);
    }


}
