package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskDao {

    List<Task> listTask();

    /**
     *  搜索
     * @param task
     * @return
     */
    List<Task> getTaskByTask(Task task,String taskName,String systems);

    /**
     *  更新
     * @param task
     * @return
     */
    int updateTask(Task task);

    /**
     *  插入
     * @param task
     * @return
     */
    int insertTask(Task task);

    /**
     *  删除
     * @param id
     * @return
     */
    int deleteTaskById(Integer id);

    Task getTaskByTaskName(String taskName);

    Task getTaskByUniqueId(@Param("id") int uniqueId);

    List<String > getColumnList();



    Task findTaskById(int id);

    List<Task> listTasksByTypeAndStatus(int type, int status);

    int updateTaskStatusById(int status, int id);

    List<String> getAllTaskName();

    public Task findTaskByName(String taskName);

    List<Task> listTasksByRunMode(List<Integer> runModeList);

    List<Task> getTasksByRunMode(int runMode);

    List<String> getTasksByExecutePlatform(String platform);

    List<String> getTasksExcludeExecutePlatform(String platform);
}
