package com.sailvan.dispatchcenter.common.pipe;


import com.sailvan.dispatchcenter.common.domain.RequestCount;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author menghui
 * @date 21-12
 */
public abstract interface RequestCountService {


    /**
     * 搜索所有
     * @return
     */
    List<RequestCount> getRequestCountAll();

    List<RequestCount> getRequestCountByPeriod(@Param("period") String period);


    /**
     *  根据指定个别参数更新
     * @param requestCount
     * @return
     */
    int updateRequestCount(RequestCount requestCount);


    /**
     *  插入
     * @param requestCount
     * @return
     */
    int insertRequestCount(RequestCount requestCount);


    /**
     * 查询
     * @param requestCount
     * @return
     */
    List<RequestCount> selectByPeriodAndSystemName(RequestCount requestCount);
}
