package com.sailvan.dispatchcenter.data;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.sailvan.dispatchcenter.data.plugs.EnableInitCache;
import com.sailvan.dispatchcenter.es.plugs.EnableEsServer;
import com.sailvan.dispatchcenter.shard.plugs.EnableMysqlShardServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author mh
 * @date 21-03
 */
@EnableTransactionManagement
@EnableScheduling
@EnableCaching
@SpringBootApplication(scanBasePackages = {
		"com.sailvan.dispatchcenter.data.*",
		"com.sailvan.dispatchcenter.common"
},exclude = {
		DataSourceAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class,
		DruidDataSourceAutoConfigure.class ,
		HibernateJpaAutoConfiguration.class})
@EnableInitCache
@EnableEsServer
@EnableMysqlShardServer
public class DataApp {

	public static void main(String[] args) {
		SpringApplication.run(DataApp.class, args);
	}


}
