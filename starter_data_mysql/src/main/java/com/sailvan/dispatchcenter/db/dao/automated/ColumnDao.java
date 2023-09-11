package com.sailvan.dispatchcenter.db.dao.automated;
import com.sailvan.dispatchcenter.common.domain.Column;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ColumnDao {


    int batchInsertColumn(@Param("taskId")int taskId,@Param("columnList") List<Column> columnList);

    int deleteColumnByTaskId(int TaskId);

    int getColumnId(String columnName);

    List<Column> listColumnsByTaskId(int columnId);

    List<Column> listColumnsByTaskIdAndIsRequired(int taskId, int isRequired);
}
