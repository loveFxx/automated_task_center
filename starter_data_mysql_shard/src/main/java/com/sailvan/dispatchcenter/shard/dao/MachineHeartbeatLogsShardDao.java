package com.sailvan.dispatchcenter.shard.dao;

import com.sailvan.dispatchcenter.common.domain.MachineHeartbeatLogs;
import com.sailvan.dispatchcenter.shard.config.MysqlShardMarkerConfiguration;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Primary;

import java.util.List;


/**
 * 机器心跳日志分表
 * @author mh
 * @date 21-10
 *
 */
//@Primary
@Mapper
//@ConditionalOnMissingBean(name = "esMarker")
@ConditionalOnBean(MysqlShardMarkerConfiguration.MysqlShardMarker.class)
public interface MachineHeartbeatLogsShardDao {

    /**
     *  根据machineId查询
     * @param machineHeartbeatLogs
     * @return
     */
    List<MachineHeartbeatLogs> getMachineHeartbeatLogsByMachineId(MachineHeartbeatLogs machineHeartbeatLogs);


    /**
     *  插入
     * @param machineHeartbeatLogs
     * @return
     */
    int insertMachineHeartbeatLogs(MachineHeartbeatLogs machineHeartbeatLogs);

    List<String> getLatestIds(String start,String end);

    List<MachineHeartbeatLogs> getLatestHeartByIds(String start,String end, String idList);

}
