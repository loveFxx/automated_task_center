package com.sailvan.dispatchcenter.data.init;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.Task;
import com.sailvan.dispatchcenter.data.plugs.InitCacheMarkerConfiguration;
import com.sailvan.dispatchcenter.redis.init.InitTaskRedisCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Primary
@ConditionalOnBean(name = "initCacheMarker")
public class InitDataTaskRedisCache extends InitTaskRedisCache {
    @Override
    @PostConstruct
    public void init() {
        /**
         * {taskName:taskName}
         */
        JSONArray taskNameMap = new JSONArray();

        /**
         * {taskName:taskId}
         */
        JSONArray taskIdMap = new JSONArray();


        /**
         * 大类型与小类型映射
         */
        Map<String, List<Task>> workTypesMap = new HashMap<>();
        List<Task> list = taskService.listTask();
        for (Task task : list) {
            String taskName = task.getTaskName();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name",taskName);
            jsonObject.put("value",taskName);
            if (!StringUtils.isEmpty(taskName) && !taskNameMap.contains(jsonObject)) {
                taskNameMap.add(jsonObject);
            }

            JSONObject jsonObjectTaskId = new JSONObject();
            jsonObjectTaskId.put("name",taskName);
            jsonObjectTaskId.put("value",task.getId());
            if (!StringUtils.isEmpty(taskName) && !taskIdMap.contains(jsonObjectTaskId)) {
                taskIdMap.add(jsonObjectTaskId);
            }
            buildWorkTypesMap(workTypesMap, task);
        }

        for (Map.Entry<String, List<Task>> stringListEntry : workTypesMap.entrySet()) {
            String key = stringListEntry.getKey();
            List<Task> value = stringListEntry.getValue();
            String json = JSON.toJSONString(value);
            redisUtils.put(WORK_TYPES_MAP_PREFIX+key+":",json, Constant.EFFECTIVE);
        }
        redisUtils.put(TASK_NAME_MAP_PREFIX,taskNameMap.toJSONString(),Constant.EFFECTIVE);
        redisUtils.put(TASK_ID_MAP_PREFIX,taskIdMap.toJSONString(),Constant.EFFECTIVE);
        System.out.println("task init");
    }
}
