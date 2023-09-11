package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.config.CoreServiceAddressConfig;
import com.sailvan.dispatchcenter.common.constant.CacheKey;
import com.sailvan.dispatchcenter.common.domain.TaskResult;
import com.sailvan.dispatchcenter.common.domain.TaskResultIndexRange;
import com.sailvan.dispatchcenter.common.domain.TaskSourceList;
import com.sailvan.dispatchcenter.common.pipe.TaskSourceListService;
import com.sailvan.dispatchcenter.common.util.CommonUtils;
import com.sailvan.dispatchcenter.common.util.TaskUtil;
import com.sailvan.dispatchcenter.db.dao.automated.TaskResultDao;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.response.PageDataResultCommon;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.*;

public class TaskResultService implements com.sailvan.dispatchcenter.common.pipe.TaskResultService {

    private static Logger logger = LoggerFactory.getLogger(TaskResultService.class);

    @Autowired
    private TaskResultDao taskResultDao;

    @Autowired
    TaskSourceListService taskSourceListService;

    @Autowired
    TaskUtil taskUtil;

    @Autowired
    TaskResultIndexRangeService taskResultIndexRangeService;

    @Autowired
    CoreServiceAddressConfig coreServiceAddressConfig;

    /**
     * 获取任务结果
     * @param pageNum 页数
     * @param pageSize 每页大小
     * @param taskSourceId 任务库id
     * @return 任务结果分页列表
     */
    @Override
    public Object listTaskResultByTaskSourceId(Integer pageNum, Integer pageSize, String taskSourceId, Object pageDataResult,String startDate,String endDate) {

        boolean flag = false;
        if (StringUtil.isNotEmpty(startDate) && StringUtil.isNotEmpty(endDate)){
            flag = true;
        }

        String[] s = taskSourceId.split("_");
        int isSingle;
        if (s[0].equals(CacheKey.CIRCLE)){
            isSingle = 0;
        }else {
            isSingle = 1;
        }
        TaskSourceList taskSource = taskSourceListService.findTaskSourceById(Integer.parseInt(s[1]),isSingle);

        int smallestId = taskUtil.getTaskResultSearchSmallestId();
        List<TaskResult> taskResultList = new ArrayList<>();
        if (flag){
            TaskResultIndexRange smallestRangeIndex = taskResultIndexRangeService.getSmallestRangeIndex(startDate);
            TaskResultIndexRange biggestRangeIndex = taskResultIndexRangeService.getBiggestRangeIndex(endDate);

            //为单次性任务
            if (isSingle == 1){
                //这句话的分页效果只对其后第一条查询生效
                PageHelper.startPage(pageNum, pageSize);
                taskResultList = taskResultDao.listTaskResultByTaskSourceIdRange(taskSourceId,smallestRangeIndex.getIndex(),biggestRangeIndex.getIndex());
            }

            //为周期性任务
            if (isSingle == 0){
                //这句话的分页效果只对其后第一条查询生效
                PageHelper.startPage(pageNum, pageSize);
                taskResultList = taskResultDao.listTaskResultByUniqueIdRange(taskSource.getUniqueId(),smallestRangeIndex.getIndex(),biggestRangeIndex.getIndex());
            }
        }else {
            //为单次性任务
            if (isSingle == 1){
                //这句话的分页效果只对其后第一条查询生效
                PageHelper.startPage(pageNum, pageSize);
                taskResultList = taskResultDao.listTaskResultByTaskSourceId(taskSourceId,smallestId);
            }

            //为周期性任务
            if (isSingle == 0){
                //这句话的分页效果只对其后第一条查询生效
                PageHelper.startPage(pageNum, pageSize);
                taskResultList = taskResultDao.listTaskResultByUniqueIdOrderByCreatedTime(taskSource.getUniqueId(),smallestId);
            }
        }


        if(taskResultList.size() != 0){
            PageInfo<TaskResult> pageInfo = new PageInfo<>(taskResultList);
            if(pageDataResult instanceof PageDataResult){
                ((PageDataResult)pageDataResult).setList(taskResultList);
                ((PageDataResult)pageDataResult).setTotals((int) pageInfo.getTotal());
                ((PageDataResult)pageDataResult).setPageNum(pageNum);
            }else if(pageDataResult instanceof PageDataResultCommon){
                ((PageDataResultCommon)pageDataResult).setLists(taskResultList);
                ((PageDataResultCommon)pageDataResult).setTotals((int) pageInfo.getTotal());
            }
        }

        return pageDataResult;
    }


    /**
     * 返回任务结果判重的
     * @param uniqueId
     * @param centerParams 中心端生成参数
     * @return 任务结果判重hash key
     */
    @Override
    public int parseResultHashKey(int uniqueId, LinkedHashMap centerParams){
        StringBuilder string = new StringBuilder();
        string.append(uniqueId);
        if (centerParams != null && !centerParams.isEmpty()){
            Iterator<Map.Entry> entries = centerParams.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                if (entry.getKey().equals("filename")){
                    continue;
                }
                string.append(entry.getValue());
            }
        }

        return CommonUtils.hashCode("soPFBR",string.toString());
    }

    @Override
    public List<TaskResult> listTaskResultByResultHashKey(int resultHashKey,int id){
        return taskResultDao.listTaskResultByResultHashKey(resultHashKey,id);
    }

    @Override
    public boolean isRepeatResult(int resultHashKey,String refreshTime) throws ParseException {
        int id = taskUtil.getTaskResultSearchSmallestId();
        List<TaskResult> taskResultList = listTaskResultByResultHashKey(resultHashKey,id);
        if (taskResultList != null && !taskResultList.isEmpty()){
            for (TaskResult taskResult:taskResultList)
            {
                Date time1 = DateUtils.convertDate(refreshTime);
                Date time2 = DateUtils.convertDate(taskResult.getRefreshTime());

                if (time1.compareTo(time2) <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int insertTaskResult(TaskResult taskResult){
        return  taskResultDao.insertTaskResult(taskResult);
    }

    @Override
    public TaskResult getTaskResultByTaskSourceIdAndResultHashKeyAndRefreshTime(int id, String taskSourceId, int resultHashKey, String refreshTime){
        return taskResultDao.getTaskResultByTaskSourceIdAndResultHashKeyAndRefreshTime(id, taskSourceId, resultHashKey, refreshTime);
    }

    @Override
    public int updateTaskResult(TaskResult taskResult){
        return  taskResultDao.updateTaskResult(taskResult);
    }

    @Override
    public TaskResult findById(int id){
        return taskResultDao.findById(id);
    }

    @Override
    public List<TaskResult> listTaskResultByTaskSourceIdRange(String taskSourceId,int smallestId,int biggestId){
        return taskResultDao.listTaskResultByTaskSourceIdRange(taskSourceId, smallestId, biggestId);
    }

    @Override
    public List<TaskResult> listTaskResultByUniqueIdRange(int uniqueId,int smallestId,int biggestId){
        return taskResultDao.listTaskResultByUniqueIdRange(uniqueId, smallestId, biggestId);
    }


    /**
     *首页
     */
    public PageDataResult getFirstTaskResultList(int maxId,TaskResult taskResult){

        List<TaskResult> taskResultList=taskResultDao.getFirstTaskResultByTaskResult( maxId,taskResult);
        PageDataResult pageDataResult = new PageDataResult();
        if(taskResultList.size() != 0){
            PageInfo<TaskResult> pageInfo = new PageInfo<>(taskResultList);
            pageDataResult.setList(taskResultList);
        }
        return pageDataResult;
    }

    /**
     *跳下一页
     */
    public PageDataResult getNextTaskResultList(int curPageMinId,TaskResult taskResult){

        List<TaskResult> taskResultList=taskResultDao.getNextTaskResultByTaskResult( curPageMinId,taskResult);
        PageDataResult pageDataResult = new PageDataResult();
        if(taskResultList.size() != 0){
            PageInfo<TaskResult> pageInfo = new PageInfo<>(taskResultList);
            pageDataResult.setList(taskResultList);
        }
        return pageDataResult;
    }

    /**
     *跳上一页
     * 需要将list反转
     */
    public PageDataResult getLastTaskResultList(int curPageMaxId,TaskResult taskResult){

        List<TaskResult> taskResultList = taskResultDao.getLastTaskResultByTaskResult( curPageMaxId,taskResult);

        Collections.reverse(taskResultList);
        PageDataResult pageDataResult = new PageDataResult();
        if(taskResultList.size() != 0){
            PageInfo<TaskResult> pageInfo = new PageInfo<>(taskResultList);
            pageDataResult.setList(taskResultList);
        }
        return pageDataResult;
    }

    @Override
    public int getTaskResultCount(TaskResult taskResult){

        int maxId = taskUtil.getIdCache("taskResultId",coreServiceAddressConfig.getPath());
        int taskResultCount = taskResultDao.getTaskResultCount(maxId,taskResult);
        return taskResultCount;
    }



    public int getMaxTaskResultId(){

        return taskUtil.getIdCache("taskResultId",coreServiceAddressConfig.getPath());

    }

    @Override
    public List<TaskResult> listErrorResult(String createdTime, String taskType, int offset, int limit){
        return taskResultDao.listErrorResult(createdTime, taskType, offset, limit);
    }

    @Override
    public int countErrorResult(String createdTime, String taskType){
        return taskResultDao.countErrorResult(createdTime, taskType);
    }

    @Override
    public List<Map<String,Object>> countDistinctErrorResult(String createdTime, String taskType, int id){
        return taskResultDao.countDistinctErrorResult(createdTime, taskType,id);
    }

    @Override
    public int countTaskResultByType(String type,int minResultId,int maxResultId){
        return taskResultDao.countTaskResultByType(type,minResultId,maxResultId);
    }

    @Override
    public List<Integer> listResultIds(String type,int minResultId,int maxResultId,String oldTaskSourceId){
        return taskResultDao.listResultIds(type, minResultId, maxResultId, oldTaskSourceId);
    }

    @Override
    public int fixTaskResult(List<Integer> ids,int uniqueId,String newTaskSourceId){
        return taskResultDao.fixTaskResult(ids,uniqueId,newTaskSourceId);
    }

    @Override
    public List<TaskResult> listTaskResultByTime(String type,int minResultId,int maxResultId,String minTime, String maxTime){
        return taskResultDao.listTaskResultByTime(type, minResultId, maxResultId, minTime, maxTime);
    }
}
