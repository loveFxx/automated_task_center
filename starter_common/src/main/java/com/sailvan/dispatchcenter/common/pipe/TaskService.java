package com.sailvan.dispatchcenter.common.pipe;

import com.sailvan.dispatchcenter.common.domain.Task;
import com.sailvan.dispatchcenter.common.domain.TaskSourceList;
import com.sailvan.dispatchcenter.common.response.PageDataResult;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author menghui
 * @date 21-04
 */
public interface TaskService {


    /**
     *  前端传来的columnName转成id
     * @param ColumnsKey
     * @return
     */
    public String columnNameToId(String ColumnsKey);

    /**
     *  数据库查到的columnId转成columnName
     * @param taskId
     * @return
     */
    public String getColumnList(int taskId);


    public int insert(Task task);


    public int update(Task task);

    public int updateTaskStatus(Task task);


    public PageDataResult getTaskList(Task task, Integer pageNum, Integer pageSize) ;



    public int delete(Integer id);

    public Task getTaskByTaskName(String taskName);

    //根据unique id查找任务库列表唯一值
    public Task getTaskByUniqueId(int uniqueId);

    public List<Task> listTask();

    public Task findTaskById(int id);

    public List<Task> listTasksByTypeAndStatus(int type, int status);


    public String buildFilename(LinkedHashMap map, LinkedHashMap taskMap,String taskAbbreviation);

    /**
     * 生成报告日期
     * @param task 任务类型
     * @return 中心端生成参数
     */
    public LinkedHashMap generateCenterParams(TaskSourceList taskSource, Task task) throws ParseException ;

    List<String> getAllTaskName();

    public Task findTaskByName(String taskName);

    List<Task> listTasksByRunMode(List<Integer> runModeList);

    List<Task> getTasksByRunMode(int runMode);

    List<String> getTasksByExecutePlatform(String platform);

    List<String> getTasksExcludeExecutePlatform(String platform);
}
