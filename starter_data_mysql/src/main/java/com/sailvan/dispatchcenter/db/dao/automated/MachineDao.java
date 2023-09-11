package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.Machine;
import com.sailvan.dispatchcenter.common.domain.MachineTypeCountDTO;
import com.sailvan.dispatchcenter.common.domain.MachineVersionStatDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mh
 * @date 21-04
 *
 *  客户端机器表
 */
@Mapper
public interface MachineDao {

    /**
     * 搜索所有
     * @return
     */
    List<Machine> getMachineAll();

    /**
     *  根据指定个别参数搜索
     * @param machine
     * @return
     */
    List<Machine> getMachineByMachine(Machine machine);

    /**
     *  根据Id查询
     * @param id
     * @return
     */
    Machine getMachineById(@Param("id") Integer id);

    /**
     *  ip
     * @param ip
     * @return
     */
    Machine getMachineByIP(@Param("ip") String ip);

    /**
     *  根据指定个别参数更新
     * @param machine
     * @return
     */
    int updateMachine(Machine machine);
    int updateByHeatBeat(Machine machine);


    /**
     *  根据心跳更新机器状态
     * @param id
     * @param machineStatus
     * @return
     */
    int updateMachineStatus(@Param("id") Integer id, @Param("machineStatus") Integer machineStatus);

    /**
     *  手动开启或禁用机器
     * @param id
     * @param machineStatus
     * @return
     */
    int updateStatus(@Param("id") Integer id, @Param("status") Integer machineStatus);

    int updateLastWorkTaskByIp(Machine machine);


    /**
     *  更新用户名密码
     * @param id
     * @param username
     * @param password
     * @return
     */
    int updateMachineUserPwd(@Param("id") Integer id, @Param("username") String username, @Param("password") String password);

    /**
     *  插入
     * @param machine
     * @return
     */
    int insertMachine(Machine machine);

    /**
     *  搜索
     * @param machine
     * @return
     */
    Machine select(Machine machine);

    /**
     *  删除
     * @param id
     * @return
     */
    int deleteMachineById(Integer id);

    /**
     *  机器表里last_heartbeat超时或者为空的机器
     *  和这个方法类似
     *  {@link com.sailvan.dispatchcenter.db.dao.automated.MachineDao#getAccountSiteWithHeartbeatTimeOut(int)}
     * @param interval 以秒为单位
     * @return
     */
    List<Machine> getMachineByStandard(@Param("interval") int interval);

    /**
     * 机器表里last_heartbeat超时或者为空的机器对应的帐号站点
     * @param interval
     * @return
     */
    List<LinkedHashMap> getAccountSiteWithHeartbeatTimeOut(@Param("interval") int interval);









    /**心跳与机器时间相差五分钟 timeDiff
     * atc_machine表中last_heartbeat,machine_local_time相差五分钟
     * @param timeDiff 单位分钟
     * @return
     */
    List<Machine> getMachineWithBigTimeDiff(@Param("timeDiff") String timeDiff);


    List<Machine> getMachineStatusOn();

    List<Machine> getMachineLackingMemory(@Param("memory")String memory);


    List<Machine> countMachineWithoutNetWork();

    /**
     * 各机器类型的总数
     * @return
     */
    List<MachineTypeCountDTO> getMachineTotalCountGroupByType();


    /**
     * 各机器类型有有效心跳的计数
     * @param interval
     * @return
     */
    List<Map> getMachineWithLivingHeartbeatCountGroupByType(@Param("interval") String interval);


    /**
     * 各类型的开启的机器的计数
     * @return
     */
    List<MachineTypeCountDTO> getMachineStatusOnCountGroupByType();


    /**
     * (机器类型:总数) left join (机器类型:具有心跳机器数) left join (机器类型:开启机器数)
     * @param interval 心跳距离现在多久算作有效心跳
     * @return
     */
    List<MachineTypeCountDTO> getMachineTypeCount(@Param("interval") String interval);


    List<MachineVersionStatDTO> getMachineFatherVersionStat();


    List<MachineVersionStatDTO> getMachineSonVersionStat();

    List<Machine> getMachineByType(String machineType);


    List<Machine> getMachineByFatherVersion(String version);


    List<Machine> getMachineBySonVersion(String version);

}
