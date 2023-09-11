package com.sailvan.dispatchcenter.common.util;

import com.sailvan.dispatchcenter.common.constant.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TaskUtil {

    @Autowired
    RedisUtils redisUtils;

    public int getSingleTaskSearchSmallestId(){
        return getSearchSmallestId(CacheKey.TASK_RESULT_SINGLE_ID);
    }

    public int getCircleTaskSearchSmallestId(){
        return getSearchSmallestId(CacheKey.TASK_RESULT_CIRCLE_ID);
    }

    public int getTaskResultSearchSmallestId(){
        return getSearchSmallestId(CacheKey.TASK_RESULT_ID);
    }

    public int getSearchSmallestId(String cacheKey){
        if(CacheKey.TASK_RESULT_CIRCLE_ID.equals(cacheKey)){
            return 0;
        }
        String taskResultIdCache = WriteToFileUtil.readId(cacheKey);
        int diff = 0 ;
        if(taskResultIdCache != null){
            int taskResultId = Integer.parseInt(taskResultIdCache);
            diff = taskResultId - CacheKey.SINGLE_TABLE_CAPACITY * 2;
            if(diff<=0){
                diff = 0;
            }
        }
        return diff;
    }

    public int getSingleNextTaskResultId(){
        return getNextTaskId(CacheKey.TASK_RESULT_SINGLE_ID);
    }

    public int getCircleNextTaskResultId(){
        return getNextTaskId(CacheKey.TASK_RESULT_CIRCLE_ID);
    }

    public int getNextTaskId(String cacheKey){
        synchronized (cacheKey.intern()){
            Object taskResultIdCache = WriteToFileUtil.readId(cacheKey);
            if(StringUtils.isEmpty(taskResultIdCache)){
                WriteToFileUtil.writeId(cacheKey,String.valueOf(1));
                return 1;
            }else {
                int taskResultId = Integer.parseInt(String.valueOf(taskResultIdCache));
                taskResultId ++;
                WriteToFileUtil.writeId(cacheKey,String.valueOf(taskResultId));
                return taskResultId;
            }
        }
    }

    public int getNextTaskResultId(){
        return getNextTaskId(CacheKey.TASK_RESULT_ID);
    }

    public int getIdCache(String cacheKey,String path){
        String taskResultIdCache = WriteToFileUtil.readIdByPrefix(cacheKey, path);
        int diff = 0 ;
        if(taskResultIdCache != null){
            int taskResultId = Integer.parseInt(taskResultIdCache);
            diff = taskResultId;
        }
        return diff;
    }
}
