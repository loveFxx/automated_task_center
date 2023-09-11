package com.sailvan.dispatchcenter.common.pipe;


import com.sailvan.dispatchcenter.common.domain.Machine;
import com.sailvan.dispatchcenter.common.domain.MachineTypeCountDTO;
import com.sailvan.dispatchcenter.common.domain.MachineVersionStatDTO;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import org.omg.CORBA.portable.ValueOutputStream;

import java.util.List;
import java.util.Map;


/**
 * @author mh
 * @date 2021-04
 */
public interface MachineService {


    public List<Machine> getMachineAll();

    public void refreshMachineWorkTypeTask() throws Exception;


    public Machine getMachineById(Integer id);

    public PageDataResult getMachineList(Machine machine, Integer pageNum, Integer pageSize) throws Exception ;



    public int update(Machine machine);
    public int updateByHeatBeat(Machine machine);

    public int updateMachineStatus(int id, int machineStatus) ;

    public int updateStatus(int id, int status) ;

    public int updateLastWorkTaskByIp(Machine machine) ;

    public int updateMachineUserPwd(int id, String username, String password) ;

    public int insert(Machine machine) ;

    public Machine select(Machine machine);


    public Machine getMachineByIP(String ip);

    public int delete(Integer id);

    public List<Machine> getMachineByStandard(int interval) ;


    /**心跳与机器时间相差五分钟 timeDiff
     * atc_machine表中last_heartbeat,machine_local_time相差五分钟
     * @param timeDiff 单位分钟
     * @return
     */
    public List<Machine> getMachineWithBigTimeDiff(String timeDiff);


    public List<Machine> getMachineStatusOn();

    public List<Machine> getMachineLackingMemory(String memory);


    public List<Machine> countMachineWithoutNetWork();

    /**
     * 各机器类型的总数
     * @return
     */
    public List<MachineTypeCountDTO> getMachineTotalCountGroupByType();


    /**
     * 各机器类型有有效心跳的计数
     * @param interval
     * @return
     */
    public List<Map> getMachineWithLivingHeartbeatCountGroupByType(String interval);


    /**
     * 各类型的开启的机器的计数
     * @return
     */
    public List<MachineTypeCountDTO> getMachineStatusOnCountGroupByType();


    /**
     * (机器类型:总数) left join (机器类型:具有心跳机器数) left join (机器类型:开启机器数)
     * @param interval 心跳距离现在多久算作有效心跳
     * @return
     */
    public List<MachineTypeCountDTO> getMachineTypeCount(String interval);


    public List<MachineVersionStatDTO> getMachineFatherVersionStat();


    public List<MachineVersionStatDTO> getMachineSonVersionStat();

    public List<Machine> getMachineByType(String machineType);


    public List<Machine> getMachineByFatherVersion(String version);


    public List<Machine> getMachineBySonVersion(String version);
}
