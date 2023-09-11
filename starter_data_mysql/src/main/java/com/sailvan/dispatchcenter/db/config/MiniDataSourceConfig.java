package com.sailvan.dispatchcenter.db.config;


import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * mini数据库配置
 * @date 2021-06
 * @author menghui
 */
@Configuration
@MapperScan(basePackages = MiniDataSourceConfig.PACKAGE, sqlSessionFactoryRef = "miniSqlSessionFactory")
public class MiniDataSourceConfig {

    /**
     *  精确到 mini 目录，以便跟其他数据源隔离
     */
    static final String PACKAGE = "com.sailvan.dispatchcenter.db.dao.mini";
    static final String MAPPER_LOCATION = "classpath:Mapper/mini/*.xml";

    @Value("${spring.datasource.mini.url}")
    private String url;

    @Value("${spring.datasource.mini.username}")
    private String user;

    @Value("${spring.datasource.mini.password}")
    private String password;

    @Value("${spring.datasource.mini.driverClassName}")
    private String driverClass;

    @Bean(name = "miniDataSource")
    public DruidDataSource miniDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean(name = "miniTransactionManager")
    public DataSourceTransactionManager miniTransactionManager() {
        return new DataSourceTransactionManager(miniDataSource());
    }

    @Bean(name = "miniSqlSessionFactory")
    public SqlSessionFactory miniSqlSessionFactory(@Qualifier("miniDataSource") DruidDataSource miniDataSource)
            throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(miniDataSource);
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources(MiniDataSourceConfig.MAPPER_LOCATION));
        return sessionFactory.getObject();
    }
}
