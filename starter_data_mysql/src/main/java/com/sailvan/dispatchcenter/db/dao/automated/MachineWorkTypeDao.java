package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.MachineWorkType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author mh
 * @date 21-06
 *
 *  客户端机器可执行任务类型
 */
@Mapper
public interface MachineWorkTypeDao {


    /**
     *  根据指定个别参数搜索
     * 如果status=-10 就是查询非移除数据
     * @param machineId
     * @param status
     * @return
     */
    List<MachineWorkType> getMachineWorkTypeByMachineIdStatus(@Param("machineId") int machineId, @Param("status") Integer status);

    /**
     *  机器ID
     * @param machineId
     * @return
     */
    List<MachineWorkType> getMachineWorkTypeByMachineId(@Param("machineId") int machineId);

    /**
     *  id查询
     * @param id
     * @return
     */
    MachineWorkType getMachineWorkTypeById(@Param("id") Integer id);

    /**
     *  查询
     * @param ip
     * @return
     */
    List<MachineWorkType> getMachineWorkTypeByIp(@Param("machineIp") String machineIp);
    List<MachineWorkType> getMachineWorkTypeByPlatFormIdAndPlatformType(@Param("platformId") Integer platformId,@Param("platformType") Integer platformType);
    List<MachineWorkType> getMachineWorkTypeByPlatformType(@Param("platformType") int platformType);

    /**
     *  任务类型查询
     * @param machinePlatform
     * @return
     */
    List<MachineWorkType> getMachineWorkTypeByMachineWorkType(MachineWorkType machinePlatform);

    /**
     *  任务名称查询
     * @param machinePlatform
     * @return
     */
    List<MachineWorkType> getMachineWorkTypeByTaskNameOrAccount(MachineWorkType machinePlatform);

    /**
     *  根据指定个别参数更新
     * @param machine
     * @return
     */
    int updateMachineWorkType(MachineWorkType machine);

    /**
     *  更新
     * @param ip
     * @param status
     * @return
     */
    int updateMachineWorkTypeStatusByIp(@Param("machineIp") String machineIp, @Param("status") int status, @Param("isUpdate") int isUpdate);

    /**
     *  gengxin1
     * @param id
     * @param status
     * @param isUpdate
     * @return
     */
    int updatePlatformTypeIsUpdateStatusById(@Param("id") int id, @Param("status") int status, @Param("isUpdate") int isUpdate);

    /**
     *  更新机器任务类型状态
     * @param machine
     * @return
     */
    int updateMachineWorkTypeStatus(MachineWorkType machine);
    int updateMachineWorkTypeIsBrowser(MachineWorkType machine);

    /**
     *  更新任务类型
     * @param id
     * @param taskTypeName
     * @return
     */
    int updateMachineWorkTypeTaskTypeName(@Param("id") Integer id, @Param("taskTypeName") String taskTypeName);


    /**
     *  账号机失效
     * @param machineId
     * @param platformType
     * @param status
     * @return
     */
    int invalidAccountMachine(@Param("machineId") Integer machineId, @Param("platformType") Integer platformType, @Param("status") Integer status, @Param("isUpdate") Integer isUpdate);

    /**
     *  更新机器平台状态
     * @param machine
     * @return
     */
    int updateMachineWorkTypePlatformTypeStatus(MachineWorkType machine);

    /**
     *  根据id 更新状态
     * @param machine
     * @return
     */
    int updatePlatformTypeStatusById(MachineWorkType machine);

    /**
     *  插入
     * @param machine
     * @return
     */
    int insertMachineWorkType(MachineWorkType machine);



    List<Integer> getMachineTypeByAccount( @Param(value="accountList") String accountList);


    /**
     * 得到有效的continent account组合
     * @return
     */
    List<String> getContinentsAccountByStatusAndType();


    List<MachineWorkType> getMachineByAccount(String account,String continents);

}
