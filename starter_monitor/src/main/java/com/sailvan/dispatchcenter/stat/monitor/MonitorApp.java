package com.sailvan.dispatchcenter.stat.monitor;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.sailvan.dispatchcenter.es.plugs.EnableEsServer;
import com.sailvan.dispatchcenter.shard.plugs.EnableMysqlShardServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @program: automated_task_center3
 * @description:
 * @author: Wu Xingjian
 * @create: 2021-10-22 14:48
 **/
@EnableScheduling
@SpringBootApplication(scanBasePackages = {
        "com.sailvan.dispatchcenter.stat.*",
        "com.sailvan.dispatchcenter.common"
}, exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        DruidDataSourceAutoConfigure.class,
        HibernateJpaAutoConfiguration.class})
@EnableEsServer
@EnableMysqlShardServer
public class MonitorApp {

    public static void main(String[] args) {
        SpringApplication.run(MonitorApp.class, args);
    }
}
