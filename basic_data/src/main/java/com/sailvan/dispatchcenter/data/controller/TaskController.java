package com.sailvan.dispatchcenter.data.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.domain.AwsTaskMap;
import com.sailvan.dispatchcenter.common.domain.LambdaUserMap;
import com.sailvan.dispatchcenter.common.domain.Task;
import com.sailvan.dispatchcenter.common.pipe.AwsTaskMapService;
import com.sailvan.dispatchcenter.common.pipe.LambdaUserMapService;
import com.sailvan.dispatchcenter.data.async.AsyncUpdateCache;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;
import com.sailvan.dispatchcenter.common.cache.InitMachineCache;
import com.sailvan.dispatchcenter.common.cache.InitTaskCache;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.db.service.TaskService;
import com.sailvan.dispatchcenter.common.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 任务管理类
 * @date 2021-04
 * @author menghui
 */
@RestController
public class TaskController {

	private static Logger logger = LoggerFactory.getLogger(TaskController.class);

	@Autowired
	TaskService taskService;

	@Autowired
    InitTaskCache initTaskCache;

	@Autowired
    InitMachineCache initMachineCache;

	@Autowired
    AsyncUpdateCache asyncUpdateCache;

	@Autowired
	AwsTaskMapService taskMapService;

	@Autowired
	LambdaUserMapService lambdaUserMapService;

	@RequestMapping(value = "/refreshTaskCache")
	@ResponseBody
	public String refreshTaskCache() {
		initTaskCache.init();
		return "success";
	}

	/**
	 * 任务
	 */
	@RequestMapping(value = "/taskManage", method = RequestMethod.POST)
	@ResponseBody
	public PageDataResult getTaskList(@RequestParam("pageNum") Integer pageNum,
									  @RequestParam("pageSize") Integer pageSize, Task task) {
		PageDataResult pdr = new PageDataResult();
		try {
			int pageNumTmp = CommonUtils.getPageNum("task", task.toString());

			if(null == pageNum || pageNumTmp == 1) {
				pageNum = 1;
			}
			if(null == pageSize) {
				pageSize = 10;
			}
			// 获取用户列表
			pdr = taskService.getTaskList(task, pageNum ,pageSize);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pdr;
	}


	@RequestMapping(value = "/deleteTask", method = RequestMethod.POST)
	@ResponseBody
	public ApiResponseDomain deleteCrawlerServer(Integer id) {
		int result = taskService.delete(id);
		int res = taskMapService.deleteByTaskId(id);
		ApiResponse apiResponse = new ApiResponse();
		if (result > 0){
			asyncUpdateCache.updateTaskCache();
			return apiResponse.success("删除成功",result);
		}else{
			return apiResponse.error(ResponseCode.ERROR_CODE, "删除id:"+id+"失败",null);
		}
	}

	@RequestMapping(value = "/updateTask", method = RequestMethod.POST)
	@ResponseBody
	public ApiResponseDomain updateTask(@RequestBody Task task, HttpServletRequest httpServletRequest) {
		HttpSession session = httpServletRequest.getSession();
		String user = (String)session.getValue("user");
		task.setUpdatedUser(user);

		int result;
		ApiResponse apiResponse = new ApiResponse();
		if(task.getId()!=0){
			//更新
			result = taskService.update(task);
			if ( result > 0){
				asyncUpdateCache.updateTaskCache();
				asyncUpdateCache.updateMachineCacheMap(task);
				String awsUserRegionFunctions = task.getAwsUserRegionFunctions();
				String[] split = awsUserRegionFunctions.split("/|_");
				LambdaUserMap mapByLambdaUserMap = new LambdaUserMap();
				if (split != null && split.length == 3){
					LambdaUserMap lambdaUserMap = new LambdaUserMap();
					lambdaUserMap.setAccountName(split[0]);
					lambdaUserMap.setRegion(split[1]);
					lambdaUserMap.setFunctionName(split[2]);
					mapByLambdaUserMap = lambdaUserMapService.getMapByLambdaUserMap(lambdaUserMap);
					AwsTaskMap taskMapByTaskId = taskMapService.getTaskMapByTaskId(task.getId());
					if (null == taskMapByTaskId){
						taskMapService.addTaskMap(task);
					}else {
						int awsLambdaMapId = taskMapByTaskId.getAwsLambdaMapId();
						if (awsLambdaMapId != mapByLambdaUserMap.getId()){
							AwsTaskMap awsTaskMap = new AwsTaskMap();
							awsTaskMap.setTaskName(taskMapByTaskId.getTaskName());
							awsTaskMap.setAwsLambdaMapId(mapByLambdaUserMap.getId());
							awsTaskMap.setTaskId(task.getId());
							awsTaskMap.setId(taskMapByTaskId.getId());
							taskMapService.updateTaskMap(awsTaskMap);
						}
					}



				}

				return apiResponse.success("更新成功",result);
			}else{
				return apiResponse.error(ResponseCode.ERROR_CODE, "更新失败",result);
			}
		}else {
			Task taskByTaskName = taskService.getTaskByTaskName(task.getTaskName());
			if(taskByTaskName != null){
				return apiResponse.error(ResponseCode.ERROR_CODE,"添加失败 任务名已经存在",null);
			}
			//插入
			result = taskService.insert(task);

			if ( result > 0){
				asyncUpdateCache.updateTaskCache();
				asyncUpdateCache.updateMachineCacheMap(task);
				taskMapService.addTaskMap(task);


				return apiResponse.success("添加成功",result);
			}else{
				return apiResponse.error(ResponseCode.ERROR_CODE,"添加失败",result);
			}
		}
	}


	/**
	 *  批量删除任务
	 * @param obj
	 * @return
	 */
	@RequestMapping(value = "/batchDeleteTask", method = RequestMethod.POST)
	@ResponseBody
	public ApiResponseDomain batchDeleteTask(@RequestBody JSONObject obj) {
		ApiResponse apiResponse = new ApiResponse();
		String ids = obj.getString("ids");
		JSONArray idsArray= JSONArray.parseArray(ids);
		System.out.println("idsArray id="+idsArray.toJSONString());
		for(int i=0;i<idsArray.size();i++){
			Integer id = Integer.valueOf(idsArray.get(i).toString());
			int result = taskService.delete(id);
			if (result<=0){
				return apiResponse.error(ResponseCode.ERROR_CODE, "系统出现异常，删除id:"+id+"失败",null);
			}
		}
		asyncUpdateCache.updateTaskCache();
		return apiResponse.success("批量删除成功",null);
	}


	@RequestMapping(value = "/updateTaskStatus", method = RequestMethod.POST)
	@ResponseBody
	public ApiResponseDomain updateTaskStatus(@RequestBody Task task) {
		ApiResponse apiResponse = new ApiResponse();
		int result = taskService.updateTaskStatus(task);
		if (result > 0) {
			asyncUpdateCache.updateTaskCache();
			initMachineCache.updateMachineCacheMap(task);
			return apiResponse.success("更新成功", result);
		} else {
			return apiResponse.error(ResponseCode.ERROR_CODE, "更新失败", result);
		}
	}

}
