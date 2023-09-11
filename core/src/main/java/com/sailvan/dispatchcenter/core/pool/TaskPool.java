package com.sailvan.dispatchcenter.core.pool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.StoreAccount;
import com.sailvan.dispatchcenter.common.domain.TaskMetadata;
import com.sailvan.dispatchcenter.common.pipe.ProxyIpService;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.core.collection.LambdaCollection;
import com.sailvan.dispatchcenter.core.config.TaskPoolConfig;
import com.sailvan.dispatchcenter.core.domain.TaskBuffer;
import com.sailvan.dispatchcenter.core.service.TaskBufferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TaskPool {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    TaskPoolConfig taskPoolConfig;

    @Resource
    TaskBufferService taskBufferService;

    @Resource
    ProxyIpService proxyIpService;

    public volatile AtomicInteger totalNum = new AtomicInteger(0);


    public static ConcurrentHashMap<String, LinkedListNode> hashLinkedList = new ConcurrentHashMap<>();

    public AtomicInteger getTotalNum() {
        return totalNum;
    }

    public ConcurrentHashMap<String, LinkedListNode> getHashLinkedList() {
        return hashLinkedList;
    }

    static public class LinkedListNode{

        //优先级
        volatile int priority;
        //任务类型
        volatile String type;
        //下一结点
        volatile LinkedListNode next;
        //头结点，永远指向最高优先级
        volatile LinkedListNode head;

        volatile Queue queue = new ConcurrentLinkedQueue();

        public LinkedListNode(){
            super();
        }

        LinkedListNode(int priority, String type) {
            this.priority = priority;
            this.type = type;
        }

        /**
         * 添加新结点
         * @param linkedListNode 当前list
         * @param node 新节点
         */
        private void add(LinkedListNode linkedListNode, LinkedListNode node){
            LinkedListNode current = linkedListNode.head;
            if (node != null){
                //新节点的优先级比当前头结点高，将头结点指向当前新节点
                if (current != null && current.priority <= node.priority){
                    LinkedListNode nextNode = current;
                    current = node;
                    current.next = nextNode;
                    linkedListNode.head = current;
                }else{
                    LinkedListNode pre = null;
                    while (current != null && current.priority > node.priority){
                        pre = current;
                        current = current.next;
                    }
                    if (pre != null){
                        LinkedListNode nextNode = current;
                        current = node;
                        pre.next = current;
                        current.next = nextNode;
                    }else {
                        if (current == null){
                            linkedListNode.head = node;
                        }
                    }
                }
            }
        }

        /**
         * 根据优先级查找当前结点
         * @param priority 优先级
         * @param linkedListNode 当前list
         * @return 当前结点
         */
        private LinkedListNode findByPriority(int priority, String type, LinkedListNode linkedListNode){
            LinkedListNode current = linkedListNode.head;
            if (current == null){
                return null;
            }
            while (current != null && !(current.priority == priority && current.type.equals(type))){
                current = current.next;
            }
            return current;
        }
    }

    /**
     * 新添任务
     * @param largeTaskType 任务大类型
     * @param taskType 任务小类型
     * @param priority 优先级
     * @param data 任务
     */
    public void push(String largeTaskType, String taskType, int priority, Object data){

        synchronized ((largeTaskType+taskType+priority).intern()){
            //有此类型
            if (hashLinkedList.containsKey(largeTaskType)){
                LinkedListNode linkedListNode = hashLinkedList.get(largeTaskType);
                LinkedListNode current = linkedListNode.findByPriority(priority, taskType, linkedListNode);

                //当前节点为空，需要添加节点
                if (current == null){
                    logger.info("新添节点addNode，任务缓存区ID--{}，任务池数量:{}",data,getTotalNum());
                    LinkedListNode node = new LinkedListNode(priority,taskType);
                    node.queue.offer(data);

                    totalNum.incrementAndGet();
                    linkedListNode.add(linkedListNode, node);
                }else {
                    logger.info("当前节点不为空，任务缓存区ID--{}，任务池数量:{}",data,getTotalNum());
                    //当前节点不为空直接往队列添加数据
                    current.queue.offer(data);
                    totalNum.incrementAndGet();
                }
            }else {
                logger.info("检查无此节点类型，任务缓存区ID--{}，任务池数量:{}",data,getTotalNum());
                //检查无此类型的新添
                LinkedListNode linkedListNode = new LinkedListNode();
                linkedListNode.head = new LinkedListNode(priority,taskType);
                hashLinkedList.put(largeTaskType,linkedListNode);
                linkedListNode.head.queue.offer(data);
                totalNum.incrementAndGet();
            }

            if (Constant.taskTypeInPoolNum.containsKey(taskType)){
                Constant.taskTypeInPoolNum.get(taskType).incrementAndGet();
            }else {
                Constant.taskTypeInPoolNum.put(taskType,new AtomicInteger(0));
            }
        }
    }

    /**
     * 取任务
     * @param largeTaskType 任务大类型
     * @param types 任务小类型
     * @return 任务
     */
    public Object pop(String largeTaskType, List<String> types) {
        //还在初始化，不让取
        if (redisUtils.exists("init_task_pool")){
            return null;
        }
        Object data = null;

        if (hashLinkedList.containsKey(largeTaskType)) {
            LinkedListNode linkedListNode = hashLinkedList.get(largeTaskType);
            LinkedListNode current = linkedListNode.head;
            LinkedListNode pre = null;
            while (current != null && !types.contains(current.type)) {
                pre = current;
                current = current.next;
            }

            if (current != null) {
//              Object data = linkedListNode.head.queue.peek(); //取不删除
                data = current.queue.poll();
                int priority = current.priority;
                String taskType = current.type;

                //如果任务为0，需要对链表进行更新
                if (current.queue.size() == 0) {
                    synchronized ((largeTaskType + taskType + priority).intern()) {
                        if (current.queue.size() == 0){
                            //当前在头结点
                            if (pre == null) {
                                //下一结点不为空，将头结点移至下一结点,否则删除整个类型
                                if (current.next != null) {
                                    linkedListNode.head = current.next;
                                } else {
                                    hashLinkedList.remove(largeTaskType);
                                }
                            } else {
                                pre.next = current.next;
                            }
                        }
                    }
                }
                if (data != null){
                    totalNum.decrementAndGet();

                    if (Constant.taskTypeInPoolNum.containsKey(taskType)){
                        Constant.taskTypeInPoolNum.get(taskType).decrementAndGet();
                    }
                }
            }
        }

        //取任务触发
        if (data != null) {
            return data;
        }
        return null;
    }

    /**
     * 删除对应优先级下的所有任务
     * @param largeTaskType 大类型
     * @param taskType 小类型
     * @param priority 优先级
     */
//    public void deleteData(String largeTaskType, String taskType, int priority){
//        String type = generateType(largeTaskType,taskType);
//        synchronized (type.intern()) {
//            if (hashLinkedList.containsKey(type)) {
//                LinkedListNode linkedListNode = hashLinkedList.get(type);
//                LinkedListNode current = linkedListNode.head;
//
//                LinkedListNode pre = null;
//                while (current != null && current.priority != priority){
//                    pre = current;
//                    current = current.next;
//                }
//
//                //当前优先级在头部节点
//                if (current != null && pre == null){
//                    if (current.next != null){
//                        linkedListNode.head = current.next;
//                        totalNum.getAndAdd(-current.num);
//                        transferBufferToPool();
//                    }else {
//                        hashLinkedList.remove(type);
//                    }
//                }
//
//                if (current != null && pre != null){
//                    pre.next = current.next;
//                    totalNum.getAndAdd(-current.num);
//                    transferBufferToPool();
//                }
//            }
//        }
//    }

    /**
     * 删除对应优先级下对应任务
     * @param largeTaskType 大类型
     * @param taskType 小类型
     * @param priority 优先级
     * @param data 任务数据
     */
    public void deleteData(String largeTaskType, String taskType, int priority, Object data){

        if (hashLinkedList.containsKey(largeTaskType)) {
            LinkedListNode linkedListNode = hashLinkedList.get(largeTaskType);
            LinkedListNode current = linkedListNode.head;

            LinkedListNode pre = null;
            while (current != null && !(current.priority == priority && current.type.equals(taskType))) {
                pre = current;
                current = current.next;
            }

            if (current != null) {
                boolean flag = current.queue.remove(data);

                if (flag){
                    totalNum.decrementAndGet();
                    logger.info("delete_task--删除任务池内任务，任务缓冲区ID:{},任务池数量:{},是否移除:{}", data, getTotalNum(), true);
                    //当前队列无任务，需删除该结点
                    if (current.queue.size() == 0) {
                        synchronized ((largeTaskType + taskType + priority).intern()) {
                            if (current.queue.size() == 0) {
                                //当前在头结点
                                if (pre == null) {
                                    //下一结点不为空，将头结点移至下一结点,否则删除整个类型
                                    if (current.next != null) {
                                        linkedListNode.head = current.next;
                                    } else {
                                        hashLinkedList.remove(largeTaskType);
                                    }
                                } else {
                                    pre.next = current.next;
                                }
                            }
                        }
                    }
                    if (Constant.taskTypeInPoolNum.containsKey(taskType)){
                        Constant.taskTypeInPoolNum.get(taskType).decrementAndGet();
                    }
                }
            }
        }
    }

    /**
     * 初始化任务池及任务缓冲区
     */
    public void init() {
        logger.info("正在初始化任务池");

        //初始化完毕前不让取,取了容易将状态改变导致查询出的一些任务丢失
        redisUtils.put("init_task_pool","0",24*3600L);
        List<Integer> list = taskBufferService.distinctPriority();
        for (Integer priority : list){
            taskBufferService.offerQueue(priority);
        }
        int count = taskBufferService.countTasks(1);
        if (count != 0){
            int limit = 1000;
            int page = count /limit;

            for (int i =0; i <= page;i++){
                int skips = i*limit;
                List<TaskBuffer> taskPoolBuffers = taskBufferService.chunkTasks(1,0,skips,limit);
                List<TaskBuffer> lambdaBuffers = taskBufferService.chunkTasks(1,1,skips,limit);
                if ((skips+limit) <= taskPoolConfig.getMaxNum()){
                    for (TaskBuffer taskPoolBuffer : taskPoolBuffers) {
                        if (!redisUtils.exists(Constant.TASK_PREFIX + taskPoolBuffer.getId())) {
                            TaskMetadata taskMetadata = buildTaskMetadata(taskPoolBuffer);
                            String resultJson = JSONObject.toJSONString(taskMetadata);
                            redisUtils.put(Constant.TASK_PREFIX + taskPoolBuffer.getId(), resultJson, 3600 * 24 * 2L);
                        }
                        push(taskPoolBuffer.getWork_type(), taskPoolBuffer.getType(), taskPoolBuffer.getPriority(), taskPoolBuffer.getId());
                    }
                    for (TaskBuffer lambdaBuffer : lambdaBuffers){
                        if (!redisUtils.exists(Constant.TASK_PREFIX + lambdaBuffer.getId())) {
                            TaskMetadata taskMetadata = buildTaskMetadata(lambdaBuffer);
                            String resultJson = JSONObject.toJSONString(taskMetadata);
                            redisUtils.put(Constant.TASK_PREFIX + lambdaBuffer.getId(), resultJson, 3600 * 24 * 2L);
                        }
                        LambdaCollection.instance().incrementQueue(lambdaBuffer);
                    }
                }else {
                    for (TaskBuffer taskBuffer : taskPoolBuffers) {
                        redisUtils.remove(Constant.TASK_PREFIX + taskBuffer.getId());
                        taskBufferService.updateIsInPoolAndInBufferTimeById(0, taskBuffer.getId());
                    }
                    for (TaskBuffer lambdaBuffer : lambdaBuffers){
                        redisUtils.remove(Constant.TASK_PREFIX + lambdaBuffer.getId());
                        taskBufferService.updateIsInPoolAndInBufferTimeById(0, lambdaBuffer.getId());
                    }
                }
            }
        }
        redisUtils.remove("init_task_pool");
        logger.info("初始化任务池完毕");
    }

    public TaskMetadata buildTaskMetadata(TaskBuffer taskBuffer){
        TaskMetadata taskMetadata = new TaskMetadata();
        taskMetadata.setType(taskBuffer.getType());
        taskMetadata.setPriority(taskBuffer.getPriority());
        taskMetadata.setIs_enforced(taskBuffer.getIs_enforced());
        LinkedHashMap returnParamsMap = new LinkedHashMap();
        returnParamsMap.put("task_buffer_id",taskBuffer.getId());
        returnParamsMap.put("task_source_id",taskBuffer.getTask_source_id());

        LinkedHashMap clientParamsMap = JSON.parseObject(taskBuffer.getClient_params(),LinkedHashMap.class, Feature.OrderedField);
        LinkedHashMap centerParamsMap = JSON.parseObject(taskBuffer.getCenter_params(),LinkedHashMap.class, Feature.OrderedField);

        taskMetadata.setWork_type(taskBuffer.getWork_type());
        taskMetadata.setCenter_params(centerParamsMap);
        taskMetadata.setClient_params(clientParamsMap);
        taskMetadata.setReturn_params(returnParamsMap);
        taskMetadata.setRetry_times(1); //生成任务默认当前执行次数为1
        if (clientParamsMap.containsKey("account") && clientParamsMap.containsKey("site")){
            String continent = Constant.SITE_CONTINENT_MAP.get(String.valueOf(clientParamsMap.get("site")));
            StoreAccount data = (StoreAccount)proxyIpService.getProxyIp(String.valueOf(clientParamsMap.get("account")), continent, null);
            taskMetadata.setUsername(data.getUsername());
            taskMetadata.setPassword(data.getPassword());
        }
        return taskMetadata;
    }
}

