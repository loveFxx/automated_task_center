package com.sailvan.dispatchcenter.core.service;

import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.core.domain.TaskBuffer;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.sailvan.dispatchcenter.common.constant.Constant.priorityBlockingQueue;

/**
 * 任务缓冲区-mongoDB事务逻辑
 */
public class TaskBufferService {

    @Resource
    MongoTemplate mongoTemplate;

    public TaskBuffer findById(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        TaskBuffer taskBuffer = mongoTemplate.findOne(query, TaskBuffer.class);
        return taskBuffer;
    }

    public TaskBuffer findByTaskSourceId(String taskSourceId){
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Query query = new Query(Criteria.where("task_source_id").is(taskSourceId));
        TaskBuffer taskBuffer = mongoTemplate.findOne(query.with(sort).limit(1), TaskBuffer.class);
        return taskBuffer;
    }

    public TaskBuffer findByTaskSourceIdAndCreatedTime(String taskSourceId, String createdTime){
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pattern pattern =Pattern.compile("^.*" + createdTime + ".*$", Pattern.CASE_INSENSITIVE);
        Query query = new Query(Criteria.where("task_source_id").is(taskSourceId).
                andOperator(Criteria.where("created_at").is(pattern)));
        TaskBuffer taskBuffer = mongoTemplate.findOne(query.with(sort).limit(1), TaskBuffer.class);
        return taskBuffer;
    }

    public void insertTaskBuffer(TaskBuffer taskBuffer){
        mongoTemplate.save(taskBuffer);
    }

    public void deleteById(String id){
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.remove(query,TaskBuffer.class);
    }

    public void updateRetryTimesById(int retryTimes, String id){
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update();
        update.set("retry_times", retryTimes);
        update.set("updated_at", DateUtils.getCurrentDate());

        mongoTemplate.updateFirst(query, update, TaskBuffer.class);
    }

    public void updateRetryTimesAndIsInPoolById(int retryTimes,int isInPool, String id){
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update();
        update.set("retry_times", retryTimes);
        update.set("is_in_pool", isInPool);
        update.set("in_buffer_time", DateUtils.getCurrentDate());
        update.set("updated_at", DateUtils.getCurrentDate());

        mongoTemplate.updateFirst(query, update, TaskBuffer.class);
    }

    public void updateIsInPoolById(int isInPool, String id){
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update();
        update.set("is_in_pool", isInPool);
        update.set("updated_at", DateUtils.getCurrentDate());

        mongoTemplate.updateFirst(query, update, TaskBuffer.class);
    }

    public void updateIsInPoolAndInBufferTimeById(int isInPool, String id){
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update();
        update.set("is_in_pool", isInPool);
        update.set("pool_type", 0);
        update.set("in_buffer_time", DateUtils.getCurrentDate());
        update.set("updated_at", DateUtils.getCurrentDate());

        mongoTemplate.updateFirst(query, update, TaskBuffer.class);
    }

    public void updateIsInPoolAndInPoolTimeById(int isInPool, int inPoolTimes, String id, int poolType){
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update();
        update.set("is_in_pool", isInPool);
        update.set("in_pool_times", inPoolTimes);
        update.set("in_pool_time", DateUtils.getCurrentDate());
        update.set("pool_type", poolType);
        update.set("updated_at", DateUtils.getCurrentDate());

        mongoTemplate.updateFirst(query, update, TaskBuffer.class);
    }

    public TaskBuffer findByTaskSourceIdAndRefreshTime(String taskSourceId, String refreshTime){
        Query query = new Query(Criteria.where("task_source_id").is(taskSourceId).and("refresh_time").is(refreshTime));
        TaskBuffer taskBuffer = mongoTemplate.findOne(query.limit(1), TaskBuffer.class);
        return taskBuffer;
    }

    public List<TaskBuffer> listTasksInPool(int isInPool){
        Query query = new Query(Criteria.where("is_in_pool").is(isInPool));
        List<TaskBuffer> taskBuffers = mongoTemplate.find(query, TaskBuffer.class);
        return taskBuffers;
    }

    public List<TaskBuffer> listExpiredTasks(String createdAt, int isInPool){
        Query query = new Query(Criteria.where("is_in_pool").is(isInPool).and("created_at").lte(createdAt));
        List<TaskBuffer> taskBuffers = mongoTemplate.find(query.limit(1000), TaskBuffer.class);
        return taskBuffers;
    }

    public List<TaskBuffer> listExpiredTasksByType(String createdAt, int isInPool,List<String> type){
        Query query = new Query(Criteria.where("type").in(type).and("is_in_pool").is(isInPool).and("created_at").lte(createdAt));
        List<TaskBuffer> taskBuffers = mongoTemplate.find(query.limit(1000), TaskBuffer.class);
        return taskBuffers;
    }

    public List<TaskBuffer> listExpiredPoolTasks(String inPoolTime, int isInPool){
        Sort sort = new Sort(Sort.Direction.ASC, "priority");
        Query query = new Query(Criteria.where("is_in_pool").is(isInPool).and("in_pool_time").lte(inPoolTime));
        List<TaskBuffer> taskBuffers = mongoTemplate.find(query.with(sort).limit(1000), TaskBuffer.class);
        return taskBuffers;
    }

    public List<TaskBuffer> listExpiredBufferTasks(String inBufferTime, int isInPool){
        Query query = new Query(Criteria.where("is_in_pool").is(isInPool).and("in_buffer_time").lte(inBufferTime));
        List<TaskBuffer> taskBuffers = mongoTemplate.find(query, TaskBuffer.class);
        return taskBuffers;
    }

    public List<TaskBuffer> listExpiredRefreshTimeTasks(String refreshTime){
        Query query = new Query(Criteria.where("refresh_time").lte(refreshTime));
        List<TaskBuffer> taskBuffers = mongoTemplate.find(query, TaskBuffer.class);
        return taskBuffers;
    }

    public List<TaskBuffer> listNearlyOutPoolTasks(String type, int isInPool){
        Sort sort = new Sort(Sort.Direction.DESC, "priority");
        Query query = new Query(Criteria.where("type").is(type).and("is_in_pool").is(isInPool));
        List<TaskBuffer> taskBuffers = mongoTemplate.find(query.with(sort).limit(50), TaskBuffer.class);
        return taskBuffers;
    }

    public List<TaskBuffer> listTaskBufferBeforeDays(int day,int limit){
        String before = DateUtils.getFormatTime(DateUtils.minusDay(day,DateUtils.getCurrentDateToDate()),"yyyy-MM-dd HH:mm:ss");
        Query query = new Query(Criteria.where("created_at").lte(before));
        return mongoTemplate.find(query.limit(limit), TaskBuffer.class);
    }

    public long batchDelete(Object[] ids){
        Query query = new Query(Criteria.where("id").in(ids));
        DeleteResult deleteResult = mongoTemplate.remove(query, TaskBuffer.class);
        return deleteResult.getDeletedCount();
    }

    public void updateBuffer(int isInPool, int inPoolTimes, String id,int isEnforced){
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update();
        update.set("is_in_pool", isInPool);
        update.set("is_enforced", isEnforced);
        update.set("in_pool_times", inPoolTimes);
        update.set("in_pool_time", DateUtils.getCurrentDate());
        update.set("updated_at", DateUtils.getCurrentDate());

        mongoTemplate.updateFirst(query, update, TaskBuffer.class);
    }

    public int countTasks(int isInPool){
        Query query = new Query(Criteria.where("is_in_pool").is(isInPool));
        long count =  mongoTemplate.count(query, TaskBuffer.class);
        return Integer.parseInt(String.valueOf(count));
    }

    public List<TaskBuffer> chunkTasks(int isInPool, int poolType, int skips,int limit){
        Query query = new Query(Criteria.where("is_in_pool").is(isInPool).and("pool_type").is(poolType));
        List<TaskBuffer> taskBuffers = mongoTemplate.find(query.limit(limit).skip(skips), TaskBuffer.class);
        return taskBuffers;
    }

    public List<TaskBuffer> listLowestPriorityPoolTasks(int isInPool){
        Sort sort = new Sort(Sort.Direction.ASC, "priority");
        Query query = new Query(Criteria.where("is_in_pool").is(isInPool));
        List<TaskBuffer> taskBuffers = mongoTemplate.find(query.with(sort).limit(2000), TaskBuffer.class);
        return taskBuffers;
    }

    public int countDifferentTasks(int isInPool,String workType, String type){
        Query query = new Query(Criteria.where("is_in_pool").is(isInPool).and("work_type").is(workType).and("type").is(type));
        long count =  mongoTemplate.count(query, TaskBuffer.class);
        return Integer.parseInt(String.valueOf(count));
    }

    public int countInPoolTasks(String type,String startDate, String endDate){
        Query query = new Query(Criteria.where("is_in_pool").is(1).and("type").is(type)
                .andOperator(Criteria.where("in_pool_time").lt(endDate),Criteria.where("in_pool_time").gte(startDate)));
        long count =  mongoTemplate.count(query, TaskBuffer.class);
        return Integer.parseInt(String.valueOf(count));
    }

    public int countInPoolTasksBefore(String type,String date){
        Query query = new Query(Criteria.where("is_in_pool").is(1).and("type").is(type)
                .andOperator(Criteria.where("in_pool_time").gte(date)));
        long count =  mongoTemplate.count(query, TaskBuffer.class);
        return Integer.parseInt(String.valueOf(count));
    }

    public int countInPoolTasksAfter(String type,String date){
        Query query = new Query(Criteria.where("is_in_pool").is(1).and("type").is(type)
                .andOperator(Criteria.where("in_pool_time").lt(date)));
        long count =  mongoTemplate.count(query, TaskBuffer.class);
        return Integer.parseInt(String.valueOf(count));
    }

    public int countInBufferTask(String type){
        Query query = new Query(Criteria.where("is_in_pool").is(0).and("type").is(type));
        long count =  mongoTemplate.count(query, TaskBuffer.class);
        return Integer.parseInt(String.valueOf(count));
    }

    public int countTasksByType(int isInPool, String type){
        Query query = new Query(Criteria.where("is_in_pool").is(isInPool).and("type").is(type));
        long count =  mongoTemplate.count(query, TaskBuffer.class);
        return Integer.parseInt(String.valueOf(count));
    }

    public List<TaskBuffer> chunkTasksByType(int isInPool, String type, int skips,int limit){
        Query query = new Query(Criteria.where("is_in_pool").is(isInPool).and("type").is(type));
        List<TaskBuffer> taskBuffers = mongoTemplate.find(query.limit(limit).skip(skips), TaskBuffer.class);
        return taskBuffers;
    }

    public TaskBuffer findPriorityTaskBuffer(int priority){
        Query query = new Query(Criteria.where("priority").is(priority).and("is_in_pool").is(0));
        Sort sort = new Sort(Sort.Direction.ASC, "in_buffer_time");
        TaskBuffer taskBuffer = mongoTemplate.findOne(query.with(sort).limit(1), TaskBuffer.class);
        return taskBuffer;
    }

    public List<TaskBuffer> listPriorityTaskBuffer(int priority){
        Query query = new Query(Criteria.where("priority").is(priority).and("is_in_pool").is(0));
        Sort sort = new Sort(Sort.Direction.ASC, "in_buffer_time");
        List<TaskBuffer> taskBuffer = mongoTemplate.find(query.with(sort).limit(2000), TaskBuffer.class);
        return taskBuffer;
    }


    public List<TaskBuffer> listTaskBufferByRunMode(List<Integer> runMode){
        Query query = new Query(Criteria.where("is_in_pool").is(0).and("run_mode").in(runMode));
        Sort sort1 = new Sort(Sort.Direction.DESC, "priority");
        Sort sort = new Sort(Sort.Direction.ASC, "in_buffer_time");
        List<TaskBuffer> taskBuffer = mongoTemplate.find(query.with(sort1).with(sort).limit(2000), TaskBuffer.class);
        return taskBuffer;
    }

    public List<TaskBuffer> listPriorityTaskBufferByRunMode(int priority, List<Integer> runMode){
        Query query = new Query(Criteria.where("priority").is(priority).
                and("is_in_pool").is(0).and("run_mode").in(runMode));
        Sort sort = new Sort(Sort.Direction.ASC, "in_buffer_time");
        List<TaskBuffer> taskBuffer = mongoTemplate.find(query.with(sort).limit(2000), TaskBuffer.class);
        return taskBuffer;
    }

    public Integer countPriorityTaskBuffer(int priority){
        Query query = new Query(Criteria.where("priority").is(priority).
                and("is_in_pool").is(0));
        long count =  mongoTemplate.count(query, TaskBuffer.class);
        return Integer.parseInt(String.valueOf(count));
    }

    public List<Integer> distinctPriority(){
        Query query = new Query(Criteria.where("is_in_pool").is(0));
        DistinctIterable distinct = mongoTemplate.getCollection("atc_task_buffer").distinct("priority", query.getQueryObject(),Integer.class);
        MongoCursor iterator = distinct.iterator();
        List myList = new ArrayList<>();
        while(iterator.hasNext()){
            myList.add(iterator.next());
        }

        return myList;
    }

    /**
     * 将优先级存入队列，优先级唯一
     * @param priority
     * @return
     */
    public boolean offerQueue(int priority){
        if (!priorityBlockingQueue.contains(priority)){
            return priorityBlockingQueue.offer(priority);
        }
        return false;
    }

    /**
     * 运行模式（run_mode）包含lambda
     * @return
     */
    public List<Integer> buildLambdaRunModeLists(){
        List<Integer> lambdaRunModeLists = new ArrayList<>();
        lambdaRunModeLists.add(Constant.LOCAL_MACHINE_AND_LAMBDA);
        lambdaRunModeLists.add(Constant.LAMBDA);
        return lambdaRunModeLists;
    }

    /**
     * 运行模式（run_mode）包含机器
     * @return
     */
    public List<Integer> buildMachineRunModeLists(){
        List<Integer> machineRunModeLists = new ArrayList<>();
        machineRunModeLists.add(Constant.LOCAL_MACHINE_AND_LAMBDA);
        machineRunModeLists.add(Constant.LOCAL_MACHINE);
        return machineRunModeLists;
    }

    public List<TaskBuffer> listBuffersByPoolType(int poolType,int limit){
        Query query = new Query(Criteria.where("is_in_pool").is(1).
                and("pool_type").is(poolType));
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        List<TaskBuffer> taskBuffer = mongoTemplate.find(query.with(sort).limit(limit), TaskBuffer.class);
        return taskBuffer;
    }

    public void updatePoolTypeByIds(int poolType,List<String> ids){

        Query query = new Query(Criteria.where("id").in(ids));
        Update update = new Update();
        update.set("pool_type", poolType);
        update.set("updated_at", DateUtils.getCurrentDate());

        mongoTemplate.updateMulti(query, update, TaskBuffer.class);
    }
}
