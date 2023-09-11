package com.sailvan.dispatchcenter.data.controller;

import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.CacheKey;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;
import com.sailvan.dispatchcenter.common.domain.Task;
import com.sailvan.dispatchcenter.common.domain.TaskSourceList;
import com.sailvan.dispatchcenter.common.pipe.ImportService;
import com.sailvan.dispatchcenter.common.pipe.TaskResultService;
import com.sailvan.dispatchcenter.common.pipe.TaskService;
import com.sailvan.dispatchcenter.common.remote.RemotePushTaskUpdate;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.CommonUtils;
import com.sailvan.dispatchcenter.db.service.TaskSourceListBaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.*;


/**
 * @date 2021-04
 * @author
 */
@RestController
public class TaskSourceListController  {

    private final Logger logger = LoggerFactory.getLogger(TaskSourceListController.class);

    @Resource
    TaskSourceListBaseService taskSourceListService;

    @Autowired
    TaskResultService taskResultService;

    @Autowired
    RemotePushTaskUpdate remotePushTaskUpdate;

    @Autowired
    ImportService importService;

    @Autowired
    TaskService taskService;

    @RequestMapping(value = "/taskSourceList", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult getTaskSourceList(@RequestParam("pageNum") Integer pageNum,
                                             @RequestParam("pageSize") Integer pageSize,
                                             @RequestParam(required = false) String startTime,
                                            @RequestParam(required = false) String expectedTime,
                                            @RequestParam(required = false) String endTime,
                                            @RequestParam(required = false) String systemIds,
                                            @RequestParam(required = false) String taskIds,
                                            TaskSourceList taskSourceList) {
        PageDataResult pdr = new PageDataResult();
        try {
            int pageNumTmp = CommonUtils.getPageNum("taskSourceList", taskSourceList.toString());

            if(null == pageNum || pageNumTmp == 1) {
                pageNum = 1;
            }
            if(null == pageSize) {
                pageSize = 10;
            }
            pdr = taskSourceListService.getTaskSourceList(taskSourceList, pageNum ,pageSize,startTime,endTime,systemIds,taskIds,expectedTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pdr;
    }

    @RequestMapping(value = "/deleteTaskSourceList", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain deleteTaskSourceList(String id,String isSingle) {
        ApiResponse apiResponse = new ApiResponse();
        if (isSingle.equals("0")){
            id = CacheKey.CIRCLE+"_"+id;
        }else {
            id = CacheKey.SINGLE+"_"+id;
        }
        remotePushTaskUpdate.remoteDelete(id);
        return apiResponse.success("删除成功",1);
    }

    @RequestMapping(value = "/updateTaskSourceList", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain updateTaskSourceList(TaskSourceList taskSourceList) {
        int result;
        ApiResponse apiResponse = new ApiResponse();
        if(taskSourceList.getId()!=0){//这里和别的类不太一样 根据taskSrcList.js的openTask()
            result = taskSourceListService.update(taskSourceList);
            if (result > 0){
                return apiResponse.success("更新成功",result);
            }else{
                return apiResponse.error(ResponseCode.ERROR_CODE, "更新失败",null);
            }
        }else {
            result = taskSourceListService.insert(taskSourceList);
            if (result > 0){
                return apiResponse.success("添加成功",result);
            }else{
                return apiResponse.error(ResponseCode.ERROR_CODE, "添加失败",null);
            }
        }
    }

    /**
     *  批量删除任务
     * @param
     * @return
     */
    @RequestMapping(value = "/batchDeleteTaskSourceList", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain batchDeleteTaskSourceList(String ids, String isSingle) {
        ApiResponse apiResponse = new ApiResponse();

        String[] idsArray = ids.split(",");
        String[] isSingleArray = isSingle.split(",");

        for(int i = 0;i < idsArray.length;i++){
            String id = idsArray[i];
            if (isSingleArray[i].equals("0")){
                id = CacheKey.CIRCLE+"_"+id;
            }else {
                id = CacheKey.SINGLE+"_"+id;
            }
            remotePushTaskUpdate.remoteDelete(id);
        }

        return apiResponse.success("批量删除成功",null);
    }

    @RequestMapping(value = "/getTaskResultFromTaskSrc", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult listTaskResult(@RequestParam(value = "pageNum",required = false) Integer pageNum, @RequestParam(value = "pageSize",required = false) Integer pageSize,
                                                 @RequestParam("taskSourceId") String taskSourceId,@RequestParam(value = "startDate",required = false) String startDate,@RequestParam(value = "endDate",required = false) String endDate) {
        PageDataResult pdr = new PageDataResult();
        try {
            if(null == pageNum) {
                pageNum = 1;
            }
            if(null == pageSize) {
                pageSize = 10;
            }
            pdr = (PageDataResult) taskResultService.listTaskResultByTaskSourceId(pageNum,pageSize,taskSourceId, pdr,startDate,endDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pdr;
    }

    /**
     * 强制入池 远程调用
     * @param id
     * @param isSingle
     * @param uniqueId
     */
    @RequestMapping(value = "/forcedEnterPool",method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain forcedEnterPool(String id,String isSingle,String uniqueId){
        ApiResponse apiResponse = new ApiResponse();
        int intId = Integer.parseInt(id);
        int intIsSingle = Integer.parseInt(isSingle);
        int iuId = Integer.parseInt(uniqueId);
        TaskSourceList tsl = taskSourceListService.getTaskSourceListByUniqueIdAndIsSingle(intId, iuId,intIsSingle);
        remotePushTaskUpdate.remotePush(tsl);
        return apiResponse.success("入池成功",1);


    }

    @RequestMapping(value = "/remoteRePushCircleTasks",method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain remoteRePushCircleTasks(@RequestParam(value = "accountContinentsRePush",required = false) String accountContinentsRePush,
                                                     @RequestParam(value = "accountSitesRePush",required = false) String accountSitesRePush,
                                                     @RequestParam(value = "taskNameRePush",required = false) String taskNameRePush,
                                                     @RequestParam(value = "isEnforcedRePush",required = false) int isEnforcedRePush){
        ApiResponse apiResponse = new ApiResponse();
        if(StringUtils.isEmpty(accountContinentsRePush) && StringUtils.isEmpty(taskNameRePush)){
            return apiResponse.success("参数不能同时为空",1);
        }
        List<String> accountContinentLists = Arrays.asList(accountContinentsRePush.split(","));
        List<String> taskIdLists = Arrays.asList(taskNameRePush.split(","));
        JSONObject jsonObject = new JSONObject();
        if(accountContinentLists != null && !accountContinentLists.isEmpty()){
            jsonObject.put("work_type",accountContinentLists);
        }
        if(taskIdLists != null && !taskIdLists.isEmpty()){
            jsonObject.put("task_id",taskIdLists);
        }

        jsonObject.put("is_enforced",isEnforcedRePush);
        remotePushTaskUpdate.remoteRePushCircleTasks(jsonObject);
        return apiResponse.success("重推成功",1);
    }

    /**
     * 导PI提供的表修复任务库，删除重复的任务，只能一个一个类型去处理
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/fixTaskSource")
    @ResponseBody
    public Map<String,Object> fixTaskSource(HttpServletRequest request) throws Exception {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultiValueMap<String, MultipartFile> multiFileMap = multipartRequest.getMultiFileMap();
        MultipartFile file = null;
        String content = "content";
        if (multiFileMap.containsKey(content)) {
            file = multiFileMap.getFirst(content);
        }
        Map<String,Object> map = new HashMap<>();
        if (file == null || file.isEmpty()) {
            map.put("code",0);
            map.put("msg","空文件");
            return map;
        }
        InputStream inputStream = file.getInputStream();
        List<List<Object>> rowLists = importService.parseExcel(inputStream, file.getOriginalFilename());
        inputStream.close();

        String type = "";
        List<Integer> ids = new ArrayList<>();
        for (List<Object> row : rowLists) {
            String circleId = String.valueOf(row.get(0));
            String[] s = circleId.split("_");
            int id = Integer.parseInt(s[1]);
            type = String.valueOf(row.get(1));
            ids.add(id);
        }

        if (!StringUtils.isEmpty(type)){
            Task task = taskService.findTaskByName(type);
            int i = taskSourceListService.batchDeleteById(task.getId(), ids);
            logger.info("删除数据："+i+"条");
            map.put("code",1);
            map.put("msg","删除数据："+i+"条");
        }else {
            map.put("code",8);
            map.put("msg","上传失败");
        }

        return map;
    }

}
