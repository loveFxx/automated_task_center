package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.config.CoreServiceAddressConfig;
import com.sailvan.dispatchcenter.common.constant.CacheKey;
import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.pipe.TaskSourceListService;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.CommonUtils;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.TaskUtil;
import com.sailvan.dispatchcenter.db.dao.automated.TaskDao;
import com.sailvan.dispatchcenter.db.dao.automated.TaskSourceListDao;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TaskSourceListBaseService implements TaskSourceListService {

    @Autowired
    TaskSourceListDao taskSourceListDao;

    @Autowired
    TaskDao taskDao;

    @Autowired
    BusinessSystemService businessSystemService;

    @Autowired
    TaskUtil taskUtil;

    @Autowired
    CoreServiceAddressConfig coreServiceAddressConfig;

    @Autowired
    EveryDayMaxSingleIdService everyDayMaxSingleIdService;

    @Override
    public TaskSourceList getTaskSourceListByUniqueIdAndIsSingle(int id, int uniqueId, int isSingle){
        return taskSourceListDao.getTaskSourceListByUniqueIdAndIsSingle(id, uniqueId,isSingle);
    }

    @Override
    public PageDataResult getTaskSourceList(TaskSourceList taskSourceList, Integer pageNum, Integer pageSize
            , String startTime, String endTime, String systemIds, String taskIds,String expectedTime) {
        int currentMaxSingleId = taskUtil.getIdCache(CacheKey.TASK_RESULT_SINGLE_ID,coreServiceAddressConfig.getPath());
//        int sid = Integer.parseInt(smallestSingleId.toString());
        currentMaxSingleId = currentMaxSingleId + 10000;
        if (expectedTime != null && !"".equals(expectedTime) ){
            expectedTime = expectedTime+'%';
        }

        Date nowdate = new Date();
        nowdate = DateUtils.minusDay(1,nowdate);
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd");
        String yesterdayDate = sdf.format(nowdate);
        int maxSingleIdByYesterday = everyDayMaxSingleIdService.getMaxNumByDate(yesterdayDate);

        //判断taskSourceList对象中属性是否为空
        boolean b = fieldIsNULL(taskSourceList);
        List<TaskSourceList> taskSrcList = new ArrayList<>();
        String params = taskSourceList.getParams();
        String p = "";
        if (params != null &&  !"".equals(params)){
            p = "'%"+params+"%'";
        }
        //参数若都为空 则为默认查询 查询最新表
        if (b == false && (startTime == null || startTime == "")
                && (endTime == null || endTime == "")
                && (systemIds == null || endTime == "")
                && (taskIds == null || taskIds == "")){
            int s = currentMaxSingleId / CacheKey.SINGLE_TABLE_CAPACITY;
            int startSingleId = s*CacheKey.SINGLE_TABLE_CAPACITY+1;
            int isSingle = 1;


            PageHelper.startPage(pageNum, pageSize);
            List<TaskSourceList> taskSrcList1 = taskSourceListDao.getDefaultTaskById(maxSingleIdByYesterday,isSingle,currentMaxSingleId,expectedTime);
            setTaskSourceList(taskSrcList1);

            PageDataResult pageDataResult = new PageDataResult();
            if(taskSrcList1.size() != 0){
                PageInfo<TaskSourceList> pageInfo = new PageInfo<>(taskSrcList1);
                pageDataResult.setList(taskSrcList1);
                pageDataResult.setTotals((int) pageInfo.getTotal());
                pageDataResult.setPageNum(pageNum);
            }
            return pageDataResult;
        }else {
            int isSingle = 1;
            int endId = 0;
            int startId = 0;
            String createdTime = "";
            //单次 和 周期性单次
            if (taskSourceList.getType() == 2 ||taskSourceList.getType() == 3){
                //单次 周期性单次查询是否选择了创建时间
                if (taskSourceList.getCreatedAt() == null || "".equals(taskSourceList.getCreatedAt())){
                    endId = currentMaxSingleId;
                    startId = maxSingleIdByYesterday;
                }else {
                    String today  = sdf.format(new Date());
                    //选择的创建时间是否是今天
                    if (today.equals(taskSourceList.getCreatedAt())){
                        endId = currentMaxSingleId;
                        startId = maxSingleIdByYesterday;
                    }else {
                        try {
                            endId = everyDayMaxSingleIdService.getMaxNumByDate(taskSourceList.getCreatedAt());
                            String endDate = taskSourceList.getCreatedAt();
                            Date date =  sdf.parse(endDate);
                            Date startDate = DateUtils.minusDay(1,date);
                            String startDateStr = sdf.format(startDate);
                            startId = everyDayMaxSingleIdService.getMaxNumByDate(startDateStr);
                        }catch (Exception e){
                            System.out.println(e+"处理日期减一天时出问题");
                        }
                    }
                }
            }else {//查询周期性任务
                isSingle = 0;
                int maxCircleId = taskUtil.getIdCache(CacheKey.TASK_RESULT_CIRCLE_ID,coreServiceAddressConfig.getPath());

                endId = maxCircleId;
                if (taskSourceList.getCreatedAt() != null && !"".equals(taskSourceList.getCreatedAt())){
                    createdTime = taskSourceList.getCreatedAt();
                    createdTime = createdTime+'%';
                }
            }

            PageHelper.startPage(pageNum, pageSize);
            taskSrcList = taskSourceListDao.getTaskSrcByTaskSrc(taskSourceList,startTime,endTime,systemIds,taskIds,isSingle,p,endId,expectedTime,startId,createdTime);
            setTaskSourceList(taskSrcList);
            PageDataResult pageDataResult = new PageDataResult();
            if(taskSrcList.size() != 0){
                PageInfo<TaskSourceList> pageInfo = new PageInfo<>(taskSrcList);
                pageDataResult.setList(taskSrcList);
                pageDataResult.setTotals((int) pageInfo.getTotal());
                pageDataResult.setPageNum(pageNum);
            }
            return pageDataResult;
        }


    }


    @Override
    public int update(TaskSourceList taskSourceList){
        return taskSourceListDao.update(taskSourceList);
    }

    @Override
    public int insert(TaskSourceList taskSourceList){
        taskSourceListDao.insertTaskSourceList(taskSourceList);
        return taskSourceList.getId();
    }

    @Override
    public int delete(String id,int isSingle){
        return taskSourceListDao.delete(Integer.parseInt(id), isSingle);
    }

    @Override
    public TaskSourceList getTaskSourceListByUniqueIdAndIsSingleAndRefreshTime(int id, int uniqueId, int isSingle,String refreshTime){
        return taskSourceListDao.getTaskSourceListByUniqueIdAndIsSingleAndRefreshTime(id, uniqueId, isSingle, refreshTime);
    }

    @Override
    public TaskSourceList findTaskSourceById(int id, int isSingle){
        return taskSourceListDao.findTaskSourceById(id, isSingle);
    }

    @Override
    public int updateLastResultTimeById(int id, String lastResultTime, int isSingle,String taskState){
        return taskSourceListDao.updateLastResultTimeById(id, lastResultTime, isSingle,taskState);
    }

    @Override
    public int updateJobNameById(String jobName, String expectedTime, int id, int isSingle ){
        return taskSourceListDao.updateJobNameById(jobName,expectedTime,id,isSingle);
    }

    @Override
    public List<TaskSourceList> listTaskSourcesByJobName(String jobName, int isSingle, int id){
        return taskSourceListDao.listTaskSourcesByJobName(jobName,isSingle,id);
    }

    @Override
    public int bulkUpdateTimeByIds(List<Integer> lists, String expectedTime, String lastCreateTime, int isSingle){
        return taskSourceListDao.bulkUpdateTimeByIds(lists,expectedTime,lastCreateTime,isSingle);
    }

    @Override
    public int updateExpectedTimeByJobName(String expectedTime, String jobName,int id, int isSingle){
        return taskSourceListDao.updateExpectedTimeByJobName(expectedTime,jobName, id, isSingle);
    }


    public  boolean fieldIsNULL(TaskSourceList o){
        boolean flag = false;
        if (o.getId() != 0 || o.getType() != 0 ){
            flag = true;
        }
        if (o.getSystemId() != null ){
            flag = true;
        }
        return flag;
    }

    public List<TaskSourceList> setTaskSourceList(List<TaskSourceList> taskSrcList){
        for (TaskSourceList sourceList : taskSrcList) {
            String taskName=taskDao.findTaskById( sourceList.getTaskId()).getTaskName();
            sourceList.setTaskName(taskName);
            List<String> list = Arrays.asList(String.valueOf(sourceList.getSystemId()).split(","));
            List<String> systemNameList = new ArrayList<>();
            for (String s : list) {
                BusinessSystem businessSystem = businessSystemService.findSystemById(Integer.parseInt(s));
                if(StringUtils.isEmpty(businessSystem)){
                    continue;
                }
                systemNameList.add(businessSystem.getSystemName());
            }
            sourceList.setSystemName(String.join(",", systemNameList));
        }
        return taskSrcList;
    }


    @Override
    public List<TaskSourceList> queryTaskSource(List workTypes, List taskIds, int id,int isSingle){
        return taskSourceListDao.queryTaskSource(workTypes,taskIds,id,isSingle);
    }

    @Override
    public int batchInsertTaskSource(List<TaskSourceList> taskSourceLists){
        return taskSourceListDao.batchInsertTaskSource(taskSourceLists);
    }

    @Override
    public int batchUpdateJobNameById(String jobName, String expectedTime, List<Integer> ids, int isSingle){
        return taskSourceListDao.batchUpdateJobNameById(jobName, expectedTime, ids, isSingle);
    }

    @Override
    public int countTaskSourceByTaskId(int taskId){
        return taskSourceListDao.countTaskSourceByTaskId(taskId);
    }

    @Override
    public List<Integer> listTaskSourceByTaskId(int taskId,int id,int limit){
        return taskSourceListDao.listTaskSourceByTaskId(taskId, id, limit);
    }

    @Override
    public List<TaskSourceList> groupByTaskSources(List<Integer> ids){
        return taskSourceListDao.groupByTaskSources(ids);
    }

    @Override
    public List<Integer> listIds(int taskId){
        return taskSourceListDao.listIds(taskId);
    }

    @Override
    public List<TaskSourceList> listTaskSourceByParams(int taskId,String params){
        return taskSourceListDao.listTaskSourceByParams(taskId,params);
    }

    @Override
    public int batchDeleteById(int taskId, List<Integer> ids){
        return taskSourceListDao.batchDeleteById(taskId,ids);
    }

    /**
     * 获取重置间隔时间
     * @param intervalType 1：自然日；2：自然小时
     * @param intervalTimes 间隔时间
     * @param time 时间
     * @return 重置间隔时间
     * @throws ParseException
     */
    @Override
    public String generateRefreshTimeByIntervalTime(int intervalType, int intervalTimes, String time) throws ParseException {
        String refreshTime;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date parse = dateFormat.parse(time);
        if (intervalTimes == 0){
            refreshTime = dateFormat.format(parse);
        }else{
            if (intervalType == 1){
                DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                Date  expectedTime  =  new Date(dateFormat1.parse(time).getTime()+ 24L *3600*1000*(intervalTimes-1));
                refreshTime = dateFormat1.format(expectedTime) + " 23:59:59";
            }else{
                DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                DateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");
                String timeShort = dateFormat2.format(parse);
                StringTokenizer st = new StringTokenizer(timeShort, ":");
                List<String> inTime = new ArrayList<String>();
                while (st.hasMoreElements()) {
                    inTime.add(st.nextToken());
                }
                String hour = inTime.get(0);
                int days = (Integer.parseInt(hour) + intervalTimes) / 24;
                int hours = (Integer.parseInt(hour) + intervalTimes) % 24;
                Date expectedTime = new Date(dateFormat1.parse(time).getTime()+ 24L *3600*1000*days);
                refreshTime = dateFormat1.format(expectedTime) + " "+hours + ":00:00";
            }
        }

        return refreshTime;
    }

    /**
     * 任务类型ID与其他参数的hash值
     * @param taskName 任务类型
     * @param workType 任务大类型
     * @param param 拆分字段参数json格式
     * @return hash值
     */
    @Override
    public int parseUniqueId(String taskName,String workType, String param){
        if (param == null){
            return CommonUtils.hashCode("ABHCED" , taskName + "_" + workType);
        }
        return CommonUtils.hashCode("ABHCED" , taskName+"_" + param + "_" + workType);
    }
}
