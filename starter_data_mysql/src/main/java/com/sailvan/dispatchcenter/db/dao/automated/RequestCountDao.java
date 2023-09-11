package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.RequestCount;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 请求数
 * @author mh
 **/
@Mapper
public interface RequestCountDao {

    /**
     * 搜索所有
     * @return
     */
    List<RequestCount> getRequestCountAll();

    List<RequestCount> getRequestCountByPeriod(String period);


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

