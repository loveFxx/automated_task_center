package com.sailvan.dispatchcenter.core;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.sailvan.dispatchcenter.shard.plugs.EnableMysqlShardServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.request.RequestContextListener;

/**
 * @author mh
 * @date 21-03
 */
@EnableTransactionManagement
@EnableScheduling
@EnableCaching
@SpringBootApplication(scanBasePackages = {
		"com.sailvan.dispatchcenter.core",
		"com.sailvan.dispatchcenter.common"
},exclude = {
		DataSourceAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class,
		DruidDataSourceAutoConfigure.class ,
		HibernateJpaAutoConfiguration.class})
@EnableHystrix
@EnableMysqlShardServer
public class CoreApp {

	public static void main(String[] args) {
		SpringApplication.run(CoreApp.class, args);
	}


}
