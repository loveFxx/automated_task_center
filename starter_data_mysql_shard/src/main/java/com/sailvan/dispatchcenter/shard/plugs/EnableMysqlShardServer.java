package com.sailvan.dispatchcenter.shard.plugs;

import com.sailvan.dispatchcenter.shard.config.MysqlShardMarkerConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MysqlShardMarkerConfiguration.class)
public @interface EnableMysqlShardServer {
}
