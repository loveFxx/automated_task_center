package com.sailvan.dispatchcenter.shard.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.sailvan.dispatchcenter.common.constant.CacheKey;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.github.pagehelper.PageHelper;
import com.sailvan.dispatchcenter.shard.algorithm.*;
import com.sailvan.dispatchcenter.shard.utils.DataFormat;
import lombok.SneakyThrows;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * 分表自定义源配置
 *
 * @author menghui
 * @date 2021-10
 */
@RefreshScope
@MapperScan(basePackages = "com.sailvan.dispatchcenter.shard.dao", sqlSessionFactoryRef = "shardingSqlSessionFactory")
@ConditionalOnBean(MysqlShardMarkerConfiguration.MysqlShardMarker.class)
public class ShardingDataSourceConfig {


    @Value("${spring.shardingsphere.datasource.automated.url}")
    private String url;

    @Value("${spring.shardingsphere.datasource.automated.username}")
    private String user;

    @Value("${spring.shardingsphere.datasource.automated.password}")
    private String password;

    @Value("${spring.shardingsphere.datasource.automated.driverClassName}")
    private String driverClass;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    TblPreComplexByUuidAlgo tblPreComplexByUuidAlgo;

    @Autowired
    TblPreShardAlgo tblPreShardAlgo;

    @Autowired
    TblPreShardByIdAlgo tblPreShardByIdAlgo;

    @Autowired
    TblRangeShardAlgo tblRangeShardAlgo;

    @Autowired
    TblRangeShardByIdAlgo tblRangeShardByIdAlgo;

    /**
     * 第一个库
     *
     * @return
     */
    @Bean(name = "automated")
    @ConfigurationProperties(prefix = "spring.shardingsphere.datasource.automated")
    public DataSource sharding() {
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


    @Bean("shardingDataSource")
    public ShardingDataSource shardingDataSource(@Qualifier("automated") DataSource automated) throws SQLException {
        // 配置真实数据源
        Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();
        dataSourceMap.put("automated", automated);
        Properties p = new Properties();
//        p.setProperty("sql.show", Boolean.TRUE.toString());
        // 获取数据源对象
        ShardingDataSource dataSource = (ShardingDataSource) ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfiguration(), p);
        return dataSource;
    }

    public ShardingRuleConfiguration shardingRuleConfiguration() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(getMachineHeartbeatLogsTableRuleConfiguration());
        shardingRuleConfig.getTableRuleConfigs().add(getTaskLogsTableRuleConfiguration());
        shardingRuleConfig.getTableRuleConfigs().add(getTaskResultTableRuleConfiguration());
        shardingRuleConfig.getTableRuleConfigs().add(getTaskSourceListTableRuleConfiguration());
        return shardingRuleConfig;
    }

    /**
     * 创建SessionFactory
     *
     * @param shardingDataSource
     * @return
     * @throws Exception
     */
    @Bean(name = "shardingSqlSessionFactory")
    public SqlSessionFactory shardingSqlSessionFactory(ShardingDataSource shardingDataSource) throws Exception {
        // @Qualifier("shardingDataSource")
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(shardingDataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:Mapper/shard/*.xml"));
        return bean.getObject();
    }

    /**
     * 创建事务管理器
     *
     * @param shardingDataSource
     * @return
     */
    @Bean("shardingTransactionManger")
    public DataSourceTransactionManager shardingTransactionManger(ShardingDataSource shardingDataSource) {
        return new DataSourceTransactionManager(shardingDataSource);
    }

    /**
     * 创建SqlSessionTemplate
     *
     * @param sqlSessionFactory
     * @return
     */
    @Bean(name = "shardingSqlSessionTemplate")
    public SqlSessionTemplate shardingSqlSessionTemplate(@Qualifier("shardingSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }


    /**
     * 机器心跳表规则
     *
     * @return
     */
    private TableRuleConfiguration getMachineHeartbeatLogsTableRuleConfiguration() {
        return getTableRuleConfiguration("atc_machine_heartbeat_logs", "created_time");
    }

    /**
     * 任务库跳表规则
     *
     * @return
     */
    private TableRuleConfiguration getTaskSourceListTableRuleConfiguration() {
        return getTableRuleConfigurationByUuid("atc_task_source_list", "id,is_single");
    }

    /**
     * 任务结果表规则
     *
     * @return
     */
    private TableRuleConfiguration getTaskResultTableRuleConfiguration() {
        return getTableRuleConfigurationById("atc_task_result", "id");
    }

    /**
     * 任务日志表规则
     *
     * @return
     */
    private TableRuleConfiguration getTaskLogsTableRuleConfiguration() {
        return getTableRuleConfiguration("atc_task_logs", "created_time");
    }

    /**
     * 表规则 ID
     *
     * @return
     */
    private TableRuleConfiguration getTableRuleConfigurationById(String tableName, String shardingColumn) {
        // 配置Order表规则 ComplexShardingStrategyConfiguration
        // ,automated."+tableName+"_$->{202201..202212},automated."+tableName+"_$->{202301..202312}
//        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration(tableName, "automated."+tableName+"_$->{202110..202112}");
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration(tableName, "automated." + tableName + "_$->{1..10}");

        // 分表策略，使用 Standard 自定义实现，这里没有分表，表名固定为user_info
        StandardShardingStrategyConfiguration tableInlineStrategy =
                new StandardShardingStrategyConfiguration(shardingColumn, tblPreShardByIdAlgo, tblRangeShardByIdAlgo);
        orderTableRuleConfig.setTableShardingStrategyConfig(tableInlineStrategy);

        // 添加表配置
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
        return orderTableRuleConfig;
    }

    private TableRuleConfiguration getTableRuleConfigurationByUuid(String tableName, String shardingColumn) {
        // 配置Order表规则
        // ,automated."+tableName+"_$->{202201..202212},automated."+tableName+"_$->{202301..202312}
//        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration(tableName, "automated."+tableName+"_$->{202110..202112}");
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration(tableName, "automated." + tableName + "_" + CacheKey.SINGLE + "_$->{1..10}" + ",automated." + tableName + "_"  + CacheKey.CIRCLE + "_$->{1..10}");

        // 分表策略，使用 Standard 自定义实现，这里没有分表，表名固定为user_info
        ComplexShardingStrategyConfiguration tableInlineStrategy =
                new ComplexShardingStrategyConfiguration(shardingColumn, tblPreComplexByUuidAlgo);
        orderTableRuleConfig.setTableShardingStrategyConfig(tableInlineStrategy);

        // 添加表配置
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
        return orderTableRuleConfig;
    }


    /**
     * 表规则
     *
     * @return
     */
    private TableRuleConfiguration getTableRuleConfiguration(String tableName, String shardingColumn) {
        // 配置Order表规则
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration(tableName, getActualDataNodes("automated", "created_time", tableName));

        // 分表策略，使用 Standard 自定义实现，这里没有分表，表名固定为user_info
        StandardShardingStrategyConfiguration tableInlineStrategy =
                new StandardShardingStrategyConfiguration("created_time", tblPreShardAlgo, tblRangeShardAlgo);
        orderTableRuleConfig.setTableShardingStrategyConfig(tableInlineStrategy);

        // 分库策略，使用 Standard 自定义实现
//        StandardShardingStrategyConfiguration dataBaseInlineStrategy =new StandardShardingStrategyConfiguration("created_time", new DBShardAlgo(), new DbRangeShardAlgo());
//        orderTableRuleConfig.setDatabaseShardingStrategyConfig(dataBaseInlineStrategy);

        // 添加表配置
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
        return orderTableRuleConfig;
    }

    @SneakyThrows
    private String getActualDataNodes(String db, String field, String tableName) {
        if ("created_time".equals(field)) {
            String lowEndpoint = DateUtils.getAfterDays(-7);
            String upperEndpoint = DateUtils.getAfterDays(7);
            List<String> rangeNameList = DataFormat.getRangeNameList(DateUtils.convertDate(lowEndpoint), DateUtils.convertDate(upperEndpoint));
            String result = "";
            for (String s : rangeNameList) {
                s = DataFormat.getCurrentDayFormat(s);
                if (!StringUtils.isEmpty(result)) {
                    result = result + ",";
                }
                result = result + db + "." + tableName + "_" + s;
            }
            return result;
        }
        return "";
    }

    @SneakyThrows
    private String getActualDataNodesDbTable(String db, String field, String tableName) {
        Set<String> strings = new HashSet<>();
        if ("created_time".equals(field)) {
            String lowEndpoint = DateUtils.getAfterDays(-7);
            String upperEndpoint = DateUtils.getAfterDays(7);
            List<String> rangeNameList = DataFormat.getRangeNameList(DateUtils.convertDate(lowEndpoint), DateUtils.convertDate(upperEndpoint));
            String result = "";
            for (String string : rangeNameList) {
                String currentYearFormat = DataFormat.getCurrentMonthYear(string);
                String routingDb = db + "_" + currentYearFormat;
                if (!strings.contains(routingDb)) {
                    strings.add(routingDb);
                }
                String table = DataFormat.getCurrentDayFormat(string);
                if (!StringUtils.isEmpty(result)) {
                    result = result + ",";
                }
                result = result + routingDb + "." + tableName + "_" + table;
            }
            return result;
        }
        return "";

    }


    /**
     * 分页
     *
     * @return
     */
    @Bean(name = "pageHelper")
    public PageHelper getPageHelper() {
        PageHelper pageHelper = new PageHelper();
        Properties properties = new Properties();
        properties.setProperty("reasonable", "true");
        properties.setProperty("supportMethodsArguments", "true");
        properties.setProperty("returnPageInfo", "true");
        properties.setProperty("params", "count=countSql");
        pageHelper.setProperties(properties);
        return pageHelper;
    }
}