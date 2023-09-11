package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.TaskResultIndexRange;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskResultIndexRangeDao {

    int insertTaskResultIndexRange(TaskResultIndexRange taskResultIndexRange);

    TaskResultIndexRange getSmallestRangeIndex(String date);

    TaskResultIndexRange getBiggestRangeIndex(String date);
}
