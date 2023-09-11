package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.Column;

import java.util.List;

public interface ColumnService {


    /**
     * 获取拆分字段
     * @param taskId
     * @param isRequired
     * @return
     */
    public List<Column> listColumnsByTaskIdAndIsRequired(int taskId, int isRequired);

    public List<Column> listColumnsByTaskId(int taskId);

    /**
     * 验证传递参数类型
     * @param type 参数类型
     * @param value 参数值
     * @return 参数类型是否匹配
     */
    public boolean validateColumns(String type, Object value);
}
