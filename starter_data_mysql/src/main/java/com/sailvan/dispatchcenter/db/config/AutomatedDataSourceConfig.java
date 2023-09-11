package com.sailvan.dispatchcenter.db.config;


import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;


/**
 * automated 数据库配置
 * @date 2021-06
 * @author menghui
 */
@Configuration
@MapperScan(basePackages = AutomatedDataSourceConfig.PACKAGE, sqlSessionFactoryRef = "automatedSqlSessionFactory")
public class AutomatedDataSourceConfig {

    /**
     * 精确到 mini 目录，以便跟其他数据源隔离
     */
    static final String PACKAGE = "com.sailvan.dispatchcenter.db.dao.automated";
    static final String MAPPER_LOCATION = "classpath:Mapper/automated/*.xml";

    @Value("${spring.datasource.automated.url}")
    private String url;

    @Value("${spring.datasource.automated.username}")
    private String user;

    @Value("${spring.datasource.automated.password}")
    private String password;

    @Value("${spring.datasource.automated.driverClassName}")
    private String driverClass;

    @Bean(name = "automatedDataSource")
    @Primary
    public DruidDataSource automatedDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);

        dataSource.setInitialSize(1);
        dataSource.setMinIdle(1);
        dataSource.setBreakAfterAcquireFailure(true);
//                ds.setMaxActive(datasourceConfig.getMaxActive());
        dataSource.setMaxActive(2000);
        dataSource.setMaxWait(90000); //60000
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(10);
//                ds.setRemoveAbandoned(true);
//                ds.setRemoveAbandonedTimeout(100);
        dataSource.setTestOnBorrow(true);
//                ds.setUseUnfairLock(true);
        dataSource.setMinIdle(1);
        dataSource.setPoolPreparedStatements(true);
        dataSource.setInitialSize(1);
        dataSource.setTimeBetweenEvictionRunsMillis(30000);//60000
        dataSource.setMaxWaitThreadCount(3000);
        dataSource.setMinEvictableIdleTimeMillis(90000);//300000
        dataSource.setValidationQuery("SELECT 1 FROM DUAL");

        return dataSource;
    }

    @Bean(name = "automatedTransactionManager")
    @Primary
    public DataSourceTransactionManager automatedTransactionManager() {
        return new DataSourceTransactionManager(automatedDataSource());
    }

    @Bean(name = "automatedSqlSessionFactory")
    @Primary
    public SqlSessionFactory miniSqlSessionFactory(@Qualifier("automatedDataSource") DruidDataSource automatedDataSource)
            throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(automatedDataSource);
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources(AutomatedDataSourceConfig.MAPPER_LOCATION));
        return sessionFactory.getObject();
    }

}
