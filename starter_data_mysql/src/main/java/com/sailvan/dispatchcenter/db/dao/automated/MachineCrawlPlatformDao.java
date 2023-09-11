package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.MachineCrawlPlatform;
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
public interface MachineCrawlPlatformDao {


    /**
     *  根据指定个别参数搜索
     * 如果status=-10 就是查询非移除数据
     * @param machineId
     * @param status
     * @return
     */
    List<MachineCrawlPlatform> getMachineCrawlPlatformByMachineIdStatus(@Param("machineId") int machineId, @Param("status") Integer status);

    /**
     *  机器ID
     * @param machineId
     * @return
     */
    List<MachineCrawlPlatform> getMachineCrawlPlatformByMachineId(@Param("machineId") int machineId);

    /**
     *  id查询
     * @param id
     * @return
     */
    MachineCrawlPlatform getMachineCrawlPlatformById(@Param("id") Integer id);



    /**
     *  更新机器任务类型状态
     * @param machine
     * @return
     */
    int updateMachineCrawlPlatformStatus(MachineCrawlPlatform machine);

    /**
     *  通过id更新状态
     * @param machine
     * @return
     */
    int updateMachineCrawlPlatformStatusById(MachineCrawlPlatform machine);



    /**
     *  插入
     * @param machine
     * @return
     */
    int insertMachineCrawlPlatform(MachineCrawlPlatform machine);

    /**
     * 搜索条件里根据平台搜索
     * @param platFormIdList
     * @return
     */
    List<Integer> getMachineByCrawlPlatform(@Param(value="platFormIdList") String platFormIdList);
}
