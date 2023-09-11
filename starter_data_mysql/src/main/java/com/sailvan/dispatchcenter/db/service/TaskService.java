package com.sailvan.dispatchcenter.db.service;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.constant.TaskType;
import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.pipe.AwsTaskMapService;
import com.sailvan.dispatchcenter.common.pipe.LambdaUserMapService;
import com.sailvan.dispatchcenter.common.remote.RemotePushTaskUpdate;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.db.dao.automated.ColumnDao;
import com.sailvan.dispatchcenter.db.dao.automated.TaskDao;
import com.sailvan.dispatchcenter.common.cache.InitPlatformCache;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author menghui
 * @date 21-04
 */
@Service
public class TaskService implements com.sailvan.dispatchcenter.common.pipe.TaskService {

    private static Logger logger = LoggerFactory.getLogger(TaskService.class);

    @Resource
    private TaskDao taskDao;

    @Resource
    private ColumnDao columnDao;


    @Autowired
    InitPlatformCache initPlatformCache;

    @Autowired
    ColumnService columnService;

    @Autowired
    RemotePushTaskUpdate remotePushTaskUpdate;

    @Autowired
    LambdaUserMapService lambdaUserMapService;

    @Autowired
    AwsTaskMapService awsTaskMapService;

    @Autowired
    RedisUtils redisUtils;

    /**
     *  前端传来的columnName转成id
     * @param ColumnsKey
     * @return
     */
    @Override
    public String columnNameToId(String ColumnsKey){

        List<String> columnList = Arrays.asList(ColumnsKey.split(","));
        List<Integer> columnIdList=new ArrayList<>();
        for(String column:columnList){
            int columnId=columnDao.getColumnId(column);
            if(!columnIdList.contains(columnId))
            {
                columnIdList.add(columnId);
            }

        }
        //去掉list转string后的中括号和空格
        return columnIdList.toString().replaceAll("(?:\\[|null|\\]| +)", "");
    }

    /**
     *  数据库查到的columnId转成columnName
     * @param taskId
     * @return
     */
    @Override
    public String getColumnList(int taskId){
        List<Column> columnNames=columnDao.listColumnsByTaskId( taskId );
        return columnNames.toString();
    }


    @Transactional(value="automatedTransactionManager")
    @Override
    public int insert(Task task){
        if (!Constant.taskTypeInPoolNum.containsKey(task.getTaskName())){
            Constant.taskTypeInPoolNum.put(task.getTaskName(),new AtomicInteger(0));
        }
        //先插入 取得任务id 此时已经存到task.id里了
        int result = taskDao.insertTask(task);

        if(task.getColumnList().size()!=0) {
            columnDao.batchInsertColumn(task.getId(),task.getColumnList());
        }

        return result;
    }


    @Transactional(value="automatedTransactionManager")
    @Override
    public int update(Task task){
        //空是来更新task的status的
        if(task.getColumnList()!=null) {
            columnDao.deleteColumnByTaskId(task.getId());
            if (task.getColumnList().size() != 0) {
                columnDao.batchInsertColumn(task.getId(), task.getColumnList());
            }
        }
        Task oldTask = taskDao.findTaskById(task.getId());
        int result = taskDao.updateTask(task);
        if (oldTask.getIntervalTimes() != 0 && task.getIntervalTimes() == 0){
            Constant.FOREVER_TASKS.add(task.getTaskName());
        }

        if (task.getIntervalTimes() != 0){
            Constant.FOREVER_TASKS.remove(task.getTaskName());
        }

        //为周期性任务需要更新
        boolean flag = true;
        if (result > 0 && task.getType() == Constant.CIRCLE_TASK
                && (oldTask.getProduceInterval() != task.getProduceInterval() ||
                oldTask.getProduceConcurrency()!=task.getProduceConcurrency() ||
                oldTask.getProduceCapacity() != task.getProduceCapacity())){
            remotePushTaskUpdate.remoteUpdateConcurrency(task);
            flag = false;
        }
        if (flag && result > 0 && task.getType() == Constant.CIRCLE_TASK && !oldTask.getCronExpression().equals(task.getCronExpression())){
            remotePushTaskUpdate.remoteUpdate(task);
        }

        return result;
    }

    @Transactional(value="automatedTransactionManager")
    @Override
    public int updateTaskStatus(Task task){
        int result = taskDao.updateTaskStatusById(task.getStatus(),task.getId());
        if (result > 0){
            Task newTask = findTaskById(task.getId());
            //周期性任务停止任务
            if (newTask.getStatus() == Constant.STATUS_INVALID && newTask.getType() == Constant.CIRCLE_TASK){
                remotePushTaskUpdate.remotePause(task);
            }

            //周期性启用任务
            if (newTask.getStatus() == Constant.STATUS_VALID && newTask.getType() == Constant.CIRCLE_TASK){
                remotePushTaskUpdate.remoteResume(task);
            }
        }
        return result;
    }


    @Override
    public PageDataResult getTaskList(Task task, Integer pageNum, Integer pageSize) {
        PageDataResult pageDataResult = new PageDataResult();
        String systems = task.getSystems();

        String taskName = "";
        if (!StringUtil.isEmpty(task.getTaskName())){
            taskName = "'%"+task.getTaskName()+"%'";
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Task> taskList = taskDao.getTaskByTask(task,taskName,systems);
        for (Task curTask:taskList){
            curTask.setColumnList(columnDao.listColumnsByTaskId( curTask.getId() ));
            curTask.setCrawlPlatformSelect(initPlatformCache.getCrawlPlatformSelectCache());
            AwsTaskMap awsTaskMap = awsTaskMapService.getTaskMapByTaskId(curTask.getId());
            if (awsTaskMap != null){
                LambdaUserMap lambdaUserMap = lambdaUserMapService.getMapById(awsTaskMap.getAwsLambdaMapId());
                curTask.setAwsUserRegionFunctions(lambdaUserMap.getAccountName()+"/"+lambdaUserMap.getRegion()+"_"+lambdaUserMap.getFunctionName());
            }
        }
        if(taskList.size() != 0){
            PageInfo<Task> pageInfo = new PageInfo<>(taskList);
            pageDataResult.setList(taskList);
            pageDataResult.setTotals((int) pageInfo.getTotal());
            pageDataResult.setPageNum(pageNum);
        }

        return pageDataResult;
    }

    @Transactional(value="automatedTransactionManager")
    @Override
    public int delete(Integer id){
        columnDao.deleteColumnByTaskId(id);
        return taskDao.deleteTaskById(id);
    }

    @Override
    public Task getTaskByTaskName(String taskName){
        return taskDao.getTaskByTaskName(taskName);
    }

    //根据unique id查找任务库列表唯一值
    @Override
    public Task getTaskByUniqueId(int uniqueId){
        return taskDao.getTaskByUniqueId(uniqueId);
    }

    @Override
    public List<Task> listTask(){
        return taskDao.listTask();
    }

    @Override
    public Task findTaskById(int id){
        return taskDao.findTaskById(id);
    }

    @Override
    public List<Task> listTasksByTypeAndStatus(int type, int status){
        return taskDao.listTasksByTypeAndStatus(type,status);
    }

    @Override
    public String buildFilename(LinkedHashMap map, LinkedHashMap taskMap,String taskAbbreviation){
        StringBuilder filename = new StringBuilder();
        if (taskMap.containsKey("account")){
            filename.append(taskMap.get("account")).append("_");
        }
        if (taskMap.containsKey("site")){
            filename.append(taskMap.get("site")).append("_");
        }
        if (StringUtil.isNotEmpty(taskAbbreviation)){
            filename.append(taskAbbreviation).append("_");
        }
        if (map.isEmpty()){
            map.put("start_date",DateUtils.getDate());
            map.put("end_date",DateUtils.getDate());
        }

        if (map.containsKey("start_date")){
            filename.append(String.valueOf(map.get("start_date")).replace("-","")).append("_");
        }

        if (map.containsKey("end_date")){
            filename.append(String.valueOf(map.get("end_date")).replace("-","")).append("_");
        }
        synchronized ("time"){
            filename.append(System.currentTimeMillis()).append("_");
        }

        return filename.delete(filename.length()-1,filename.length()).toString().toUpperCase();
    }


    /**
     * 生成报告日期
     * @param task 任务类型
     * @return 中心端生成参数
     */
    @Override
    public LinkedHashMap generateCenterParams(TaskSourceList taskSource, Task task) throws ParseException {
        Date date = new Date();
        LinkedHashMap map = new LinkedHashMap<>();
        LinkedHashMap taskMap = new LinkedHashMap<>();
        if (task.getType()== Constant.CIRCLE_TASK){
            JSONObject parseObject = JSONObject.parseObject(taskSource.getParams());
            List<Column> columns = columnService.listColumnsByTaskId(task.getId());
            for (Column column : columns) {
                if (column.getIsCombined() == 1 && column.getIsRequired() == 0){
                    if (parseObject.containsKey(column.getColumnsName())){
                        map.put(column.getColumnsName(),parseObject.get(column.getColumnsName()));
                    }
                }
                if (column.getIsRequired() == 1){
                    if (parseObject.containsKey(column.getColumnsName())){
                        taskMap.put(column.getColumnsName(),parseObject.get(column.getColumnsName()));
                    }
                }
            }
        }

        String filename = "";
        switch (task.getTaskName()){
            case TaskType.MT_DAILY:
                if (map.isEmpty()) {
                    map = generateCutTwoPeriod(date);
                }
                filename = buildFilename(map, taskMap, task.getTaskAbbreviation());
                if (!map.containsKey("filename")) {
                    map.put("filename", filename + ".csv");
                }
                break;
            case TaskType.CPC_SP_DAILY:
            case TaskType.CPC_SD_DAILY:
            case TaskType.CPC_SBV_DAILY:
            case TaskType.CPC_SB_DAILY:
            case TaskType.VC_CPC_SB_DAILY:
            case TaskType.VC_CPC_SP_DAILY:
            case TaskType.VC_CPC_SD_DAILY:
            case TaskType.VC_CPC_SBV_DAILY:
            case TaskType.VC_PAYMENT_REMITTANCE_DAILY:
            case TaskType.VC_CHARGEBACK_DAILY:
                if (map.isEmpty()) {
                    map = generateCutTwoPeriod(date);
                }
                filename = buildFilename(map, taskMap, task.getTaskAbbreviation());
                if (!map.containsKey("filename")) {
                    map.put("filename", filename + ".xlsx");
                }
                break;
            case TaskType.RETURNS_DAILY:
                if (map.isEmpty()){
                    map = generateCutTwoPeriod(date);
                }
                filename = buildFilename(map,taskMap, task.getTaskAbbreviation());
                if (!map.containsKey("filename")){
                    map.put("filename",filename + ".txt");
                }
                break;
            case TaskType.VAT_DOWNLOAD_DAILY:
                if (map.isEmpty()){
                    map = generateCutFourPeriod(date);
                }
                filename = buildFilename(map,taskMap, task.getTaskAbbreviation());
                if (!map.containsKey("filename")){
                    map.put("filename",filename + ".zip");
                }
                break;
            case TaskType.MT_SUMMARY_MONTHLY:
                if (map.isEmpty()){
                    map = generateLastMonthPeriod();
                }
                filename = buildFilename(map,taskMap, task.getTaskAbbreviation());
                if (!map.containsKey("filename")){
                    map.put("filename",filename + ".pdf");
                }
                break;
            case TaskType.MT_MONTHLY:
            case TaskType.MT_B2B_MONTHLY:
            case TaskType.VAT_MONTHLY:
            case TaskType.VC_RETAIL_MONTHLY:
            case TaskType.VC_CHARGEBACK_MONTHLY:
                if (map.isEmpty()){
                    map = generateLastMonthPeriod();
                }
                filename = buildFilename(map,taskMap, task.getTaskAbbreviation());
                if (!map.containsKey("filename")){
                    map.put("filename",filename + ".csv");
                }
                break;
            case TaskType.CPC_SP_MONTHLY:
            case TaskType.CPC_SD_MONTHLY:
            case TaskType.CPC_SBV_MONTHLY:
            case TaskType.CPC_SB_MONTHLY:
            case TaskType.VC_CPC_SP_MONTHLY:
            case TaskType.VC_CPC_SD_MONTHLY:
            case TaskType.VC_CPC_SBV_MONTHLY:
            case TaskType.VC_CPC_SB_MONTHLY:
            case TaskType.VC_PAYMENT_REMITTANCE_MONTHLY:
                if (map.isEmpty()){
                    map = generateLastMonthPeriod();
                }
                filename = buildFilename(map,taskMap, task.getTaskAbbreviation());
                if (!map.containsKey("filename")){
                    map.put("filename",filename + ".xlsx");
                }
                break;
            case TaskType.STORAGE_FREE_MONTHLY:
            case TaskType.STORAGE_MON_MONTHLY:
            case TaskType.RETURNS_MONTHLY:
                if (map.isEmpty()){
                    map = generateLastMonthPeriod();
                }
                filename = buildFilename(map,taskMap, task.getTaskAbbreviation());
                if (!map.containsKey("filename")){
                    map.put("filename",filename + ".txt");
                }
                break;
            case TaskType.BUSINESS_DAILY:
                if (map.isEmpty()){
                    Date date1 = DateUtils.minusDay(3,date);
                    String formatTime1 = DateUtils.getFormatTime(date1, "yyyy-MM-dd");
                    map.put("start_date",formatTime1);
                    map.put("end_date",formatTime1);
                }
                filename = buildFilename(map,taskMap, task.getTaskAbbreviation());
                if (!map.containsKey("filename")){
                    map.put("filename",filename + ".csv");
                }
                break;
            case TaskType.BUSINESS_BEFORE_FOUR_DAILY:
                if (map.isEmpty()){
                    Date date1 = DateUtils.minusDay(4,date);
                    String formatTime1 = DateUtils.getFormatTime(date1, "yyyy-MM-dd");
                    map.put("start_date",formatTime1);
                    map.put("end_date",formatTime1);
                }
                filename = buildFilename(map,taskMap, task.getTaskAbbreviation());
                if (!map.containsKey("filename")){
                    map.put("filename",filename + ".csv");
                }
                break;
            case TaskType.DEAL:
            case TaskType.DEAL_RESULT:
            case TaskType.INVENTORY_AGE:
                filename = buildFilename(map,taskMap, task.getTaskAbbreviation());
                if (!map.containsKey("filename")){
                    map.put("filename",filename + ".txt");
                }
                break;
            case TaskType.BOX_DOWNLOAD:
                filename = buildFilename(map,taskMap, task.getTaskAbbreviation());
                if (!map.containsKey("filename")){
                    map.put("filename",filename + ".xlsx");
                }
                break;
            case TaskType.VC_COOP_LIST_DAILY:
            case TaskType.VC_RETURN_LIST_DAILY:
            case TaskType.VC_INVENTORY_LIST_DAILY:
                if (map.isEmpty()){
                    map = generateRecentThirtyPeriod(date);
                }
                break;
            default:break;
        }
        return map;
    }

    @Override
    public List<String> getAllTaskName() {
        List<String> allTaskName = taskDao.getAllTaskName();
        return allTaskName;
    }

    private LinkedHashMap generateCutTwoPeriod(Date datetime){
        LinkedHashMap map = new LinkedHashMap<>();
        String dayOfMonth = DateUtils.getDayOfMonth(1);
        Date date = DateUtils.minusDay(2,datetime);
        String formatTime = DateUtils.getFormatTime(date, "yyyy-MM-dd");
        map.put("start_date",dayOfMonth);
        map.put("end_date",formatTime);
        return map;
    }

    private LinkedHashMap generateLastMonthPeriod(){
        LinkedHashMap map = new LinkedHashMap<>();
        String lastMonthDayOne = DateUtils.getLastMonthDayOne();
        String lastMonthLastDay = DateUtils.getLastMonthLastDay();
        map.put("start_date",lastMonthDayOne);
        map.put("end_date",lastMonthLastDay);
        return map;
    }

    private LinkedHashMap generateCutFourPeriod(Date datetime){
        LinkedHashMap map = new LinkedHashMap<>();
        Date date = DateUtils.minusDay(4,datetime);
        String formatTime = DateUtils.getFormatTime(date, "yyyy-MM-dd");
        map.put("start_date",formatTime);
        map.put("end_date",formatTime);
        return map;
    }

    private LinkedHashMap generateRecentThirtyPeriod(Date datetime){
        LinkedHashMap map = new LinkedHashMap<>();
        Date date1 = DateUtils.minusDay(31,datetime);
        String formatTime1 = DateUtils.getFormatTime(date1, "yyyy-MM-dd");
        Date date2 = DateUtils.minusDay(1,datetime);
        String formatTime2 = DateUtils.getFormatTime(date2, "yyyy-MM-dd");
        map.put("start_date",formatTime1);
        map.put("end_date",formatTime2);
        return map;
    }

    @Override
    public Task findTaskByName(String taskName){
        return taskDao.findTaskByName(taskName);
    }

    @Override
    public List<Task> listTasksByRunMode(List<Integer> runModeList){
        return taskDao.listTasksByRunMode(runModeList);
    }

    @Override
    public List<Task> getTasksByRunMode(int runMode){
        return taskDao.getTasksByRunMode(runMode);
    }

    @Override
    public List<String> getTasksByExecutePlatform(String platform){
        return taskDao.getTasksByExecutePlatform(platform);
    }

    @Override
    public List<String> getTasksExcludeExecutePlatform(String platform){
        return taskDao.getTasksExcludeExecutePlatform(platform);
    }
}
