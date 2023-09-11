package com.sailvan.dispatchcenter.shard.config;

import org.springframework.context.annotation.Bean;


public class MysqlShardMarkerConfiguration {
    @Bean("mysqlShardMarker")
    public MysqlShardMarker mysqlShardMarkerBean() {
        return new MysqlShardMarker();
    }
    public class MysqlShardMarker {
    }
}
