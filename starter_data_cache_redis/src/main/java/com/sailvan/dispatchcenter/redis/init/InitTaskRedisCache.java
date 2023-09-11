package com.sailvan.dispatchcenter.redis.init;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.Task;
import com.sailvan.dispatchcenter.common.pipe.TaskService;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.common.cache.impl.InitBaseTaskCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 初始化账号绑定的代理IP
 *
 * @author menghui
 * @date 2021-06
 */

public class InitTaskRedisCache extends InitBaseTaskCache {


    @Autowired
    public TaskService taskService;

    @Autowired
    ApplicationContext context;

    @Autowired
    public RedisUtils redisUtils;

    public static String TASK_NAME_MAP_PREFIX = "taskCache:taskNameMap:";
    public static String TASK_ID_MAP_PREFIX = "taskCache:taskIdMap:";
    public static String WORK_TYPES_MAP_PREFIX = "taskCache:workTypesMap:";


    @PostConstruct
    @Override
    public void init() {

    }

    @Override
    public JSONArray getTaskNameMapCache(){
        Object o = redisUtils.get(TASK_NAME_MAP_PREFIX);
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        return JSONArray.parseArray((String.valueOf(o)));
    }

    @Override
    public List<Task> getWorkTypesMapCache(String platform){
        Object o = redisUtils.get(WORK_TYPES_MAP_PREFIX+platform+":");
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        List<Task> tasks = JSONArray.parseArray(String.valueOf(o), Task.class);
        return  tasks;
    }

    @Override
    public JSONArray getTaskIdMapCache(){
        Object o = redisUtils.get(TASK_ID_MAP_PREFIX);
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        return JSONArray.parseArray(String.valueOf(o));
    }


    @Override
    public synchronized void updateTaskCache(){
        init();
    }


    /**
     * 初始化大类型与小类型映射关系
     */
    public void initWorkTypesMap(Map<String, List<Task>> workTypesMapTmp){
        List<Task> tasks = taskService.listTask();
        for (Task task:tasks) {
            buildWorkTypesMap(workTypesMapTmp, task);
        }
    }

    /**
     * 构建映射map
     * @param task
     */
    public void buildWorkTypesMap(Map<String, List<Task>> workTypesMapTmp, Task task){
        //1.按可爬取平台;2.按账号站点
        if (task.getLargeTaskType() == Constant.LARGE_TASK_TYPE_CRAWL_PLATFORM){
            String executePlatforms = task.getExecutePlatforms();
            String[] split = executePlatforms.split(",");
            for (String i:split) {
                generateMap(workTypesMapTmp, task,Constant.EXECUTE_PLATFORMS.get(i));
            }
        }
        if (task.getLargeTaskType() == Constant.LARGE_TASK_TYPE_ACCOUNT_PLATFORM){
            generateMap(workTypesMapTmp, task,"Account");
        }
    }

    /**
     * 构建映射map
     * @param task 任务类型
     * @param key 大类型名称
     */
    private void generateMap(Map<String, List<Task>> workTypesMapTmp, Task task, String key){
        if (workTypesMapTmp.containsKey(key)){
            List<Task> types = workTypesMapTmp.get(key);
            if (!types.contains(task)){
                types.add(task);
            }
        }else {
            List<Task> types = new ArrayList<>();
            types.add(task);
            workTypesMapTmp.put(key,types);
        }
    }
}
