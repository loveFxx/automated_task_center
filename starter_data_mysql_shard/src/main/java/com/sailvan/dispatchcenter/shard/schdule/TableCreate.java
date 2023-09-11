package com.sailvan.dispatchcenter.shard.schdule;

import com.sailvan.dispatchcenter.shard.config.MysqlShardMarkerConfiguration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.apache.shardingsphere.underlying.common.rule.DataNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.context.support.StandardServletEnvironment;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

/**
 * @author mh
 */
@Slf4j
@Setter
@Getter
@Component
@ConditionalOnBean(MysqlShardMarkerConfiguration.MysqlShardMarker.class)
public class TableCreate {

    @Resource
    private ShardingDataSource shardingDataSource;

    @Autowired
    StandardServletEnvironment env;


//    @PostConstruct
    public void init() {
        ShardingRule rule = shardingDataSource.getRuntimeContext().getRule();
        Collection<TableRule> tableRules = rule.getTableRules();
        for (TableRule tableRule : tableRules) {
            String logicTable = tableRule.getLogicTable();
            List<DataNode> actualDataNodes = tableRule.getActualDataNodes();
            for (DataNode actualDataNode : actualDataNodes) {
                createNeedTime(logicTable, actualDataNode.getDataSourceName(), actualDataNode.getTableName());
            }
        }

    }

    public void createNeedTime(String table, String db, String create) {
        DataSource dataSource = shardingDataSource.getDataSourceMap().get(db);
        String sql = "SHOW CREATE TABLE " + table;
        String existSql = "select * from information_schema.tables where table_name ='" + table + "' AND table_schema =";
        doCreate(dataSource, sql, existSql, create, db, table);
    }


    private void doCreate(DataSource dataSource, String sql, String existSql, String create, String db, String table) {
        String msg = " create table: " + create + "  origin table: " + table + "  db: " + db;
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = dataSource.getConnection();
            stmt = conn.createStatement();
            ResultSet database = stmt.executeQuery("select database()");
            Assert.isTrue(database.next(), msg + "database 不存在");
            String dbschame = database.getString(1);
            existSql = existSql + "'" + dbschame + "'";
            ResultSet resultSet = stmt.executeQuery(existSql);
            Assert.isTrue(resultSet.next(), msg + "初始化表不存在");

            ResultSet resTable = stmt.executeQuery(sql);
            Assert.isTrue(resTable.next(), msg + "初始化表不存在");
            String existTableName = resTable.getString(1);
            String createSqlOrigin = resTable.getString(2);
            // log.info(existTableName, createSqlOrigin);

            String existSqlNew = StringUtils.replaceOnce(existSql, existTableName, create);
            ResultSet executeQuery = stmt.executeQuery(existSqlNew);


            if (executeQuery.next()) {
                log.info("table exist : " + msg);
            } else {
                String creatsql = StringUtils.replaceOnce(createSqlOrigin, existTableName, create);
                if (0 == stmt.executeUpdate(creatsql)) {
                    log.info(msg + " success ！");
                } else {
                    log.error(msg + " fail ！");
                }
            }
        } catch (Exception e) {
            log.error("create  table fail  error : {} ", e.getMessage());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error("SQLException", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error("SQLException", e);
                }
            }
        }
    }

    /**
     * 定时建表
     *
     * @return
     * @throws SQLException
     */
//    @Scheduled(cron = "1 1 0 * * ?")
    public void cfWdtRdCalculateTask() throws SQLException {
        init();
    }


}
