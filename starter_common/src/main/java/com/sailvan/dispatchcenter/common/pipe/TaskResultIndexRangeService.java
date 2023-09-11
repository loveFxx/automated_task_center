package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.TaskResultIndexRange;

public interface TaskResultIndexRangeService {

    int insertTaskResultIndexRange(TaskResultIndexRange taskResultIndexRange);

    TaskResultIndexRange getSmallestRangeIndex(String date);

    TaskResultIndexRange getBiggestRangeIndex(String date);
}
