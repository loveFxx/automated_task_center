package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.MachineWorkType;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

import java.util.List;

/**
 * @author mh
 * @date 2021-06
 */
public interface MachineWorkTypeService {

    public List<MachineWorkType> getMachineWorkTypeByMachineId(int machineId);

    public List<MachineWorkType> getMachineWorkTypeByMachineIdStatus(int machineId, int status);


    public PageDataResult getMachineWorkTypeList(MachineWorkType machine, Integer pageNum, Integer pageSize) ;



    public List<MachineWorkType> getMachineWorkTypeByMachineWorkType(MachineWorkType machinePlatform) ;


    public int update(MachineWorkType machine);
    public int updateMachineWorkTypeStatusByIp(String ip, int status, int isUpdate);

    public int updatePlatformTypeIsUpdateStatusById(Integer id, int status, int isUpdate);

    public int updateMachineWorkTypeTaskTypeName(Integer id, String taskTypeName);

    public MachineWorkType getMachineWorkTypeById(Integer id);

    public List<MachineWorkType> getMachineWorkTypeByIp(String ip);

    public List<MachineWorkType> getMachineWorkTypeByPlatFormIdAndPlatformType(Integer platformId, Integer platformType);

    public List<MachineWorkType> getMachineWorkTypeByPlatformType(int platformType);

    public int updateStatus(MachineWorkType machine);

    public int updateMachineWorkTypeIsBrowser(MachineWorkType machine);

    public int updatePlatformTypeStatus(MachineWorkType machine);

    public int updatePlatformTypeStatusById(MachineWorkType machine);


    public int insert(MachineWorkType machine);



}
