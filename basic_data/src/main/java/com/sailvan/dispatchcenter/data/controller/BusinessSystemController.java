package com.sailvan.dispatchcenter.data.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sailvan.dispatchcenter.common.cache.InitSystemCache;
import com.sailvan.dispatchcenter.common.cache.InitTaskCache;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.BusinessSystem;
import com.sailvan.dispatchcenter.common.domain.BusinessSystemTask;
import com.sailvan.dispatchcenter.common.domain.Task;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.CommonUtils;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.db.service.BusinessSystemService;
import com.sailvan.dispatchcenter.db.service.BusinessSystemTaskService;
import com.sailvan.dispatchcenter.db.service.TaskService;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;


/**
 * 业务系统管理
 * @date 2021-04
 * @author menghui
 */
@RestController
public class BusinessSystemController {

	private static Logger logger = LoggerFactory.getLogger(BusinessSystemController.class);

	@Autowired
    BusinessSystemService businessSystemService;

	@Autowired
	InitTaskCache initTaskCache;

	@Autowired
	InitSystemCache initSystemCache;

	@Autowired
	BusinessSystemTaskService businessSystemTaskService;

	@Autowired
	TaskService taskService;

	@RequestMapping(value = "/refreshSystemCache")
	@ResponseBody
	public String refreshSystemCache() {
		initSystemCache.init();
		return "success";
	}

	@RequestMapping(value = "/businessSystem", method = RequestMethod.POST)
	@ResponseBody
	public PageDataResult getUserList(@RequestParam("pageNum") Integer pageNum,
									  @RequestParam("pageSize") Integer pageSize, BusinessSystem businessSystem) {
		PageDataResult pdr = new PageDataResult();
		try {
			int pageNumTmp = CommonUtils.getPageNum("businessSystem", businessSystem.toString());

			if(null == pageNum || pageNumTmp == 1) {
				pageNum = 1;
			}
			if(null == pageSize) {
				pageSize = 10;
			}
			pdr = businessSystemService.getBusinessSystemList(businessSystem, pageNum ,pageSize);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pdr;
	}


	@RequestMapping(value = "/deleteBusinessSystemById", method = RequestMethod.POST)
	@ResponseBody
	public ApiResponseDomain deleteBusinessSystemById(Integer id) {
		int result = businessSystemService.delete(id);
		ApiResponse apiResponse = new ApiResponse();
		if (result > 0){
			initSystemCache.updateSystemNameCache();
			logger.debug("deleteBusinessSystemById BusinessSystemId {} ",id);
			return apiResponse.success("删除成功",result);
		}else{
			logger.error("deleteBusinessSystemById BusinessSystemId {} ",id);
			return apiResponse.error(ResponseCode.ERROR_CODE, "删除id:"+id+"失败",null);
		}
	}

	@SneakyThrows
	@RequestMapping(value = "/updateBusinessSystem", method = RequestMethod.POST)
	@ResponseBody
	public ApiResponseDomain updateBusinessSystem(BusinessSystem businessSystem, HttpServletRequest httpServletRequest) {
		HttpSession session = httpServletRequest.getSession();
		String user = (String)session.getValue("user");
		businessSystem.setUpdateUser(user);
		int result;
		ApiResponse apiResponse = new ApiResponse();
        if(!StringUtils.isEmpty(businessSystem.getId()) && businessSystem.getId()>0){
        	result = businessSystemService.update(businessSystem);
			if (result > 0){
				updateBusinessTaskList(businessSystem);
				initSystemCache.updateSystemNameCache();
				logger.debug("updateBusinessSystem update businessSystem {} ",businessSystem);
				return apiResponse.success("更新成功",result);
			}else{
				logger.error("updateBusinessSystem update businessSystem {} ",businessSystem);
				return apiResponse.error(ResponseCode.ERROR_CODE, "更新失败",null);
			}
		}else {
			businessSystem.setStatus(Constant.STATUS_INVALID);
			result = businessSystemService.insert(businessSystem);
			if (result > 0){
				updateBusinessTaskList(businessSystem);
				initSystemCache.updateSystemNameCache();
				logger.debug("updateBusinessSystem insert businessSystem {} ",businessSystem);
				return apiResponse.success("添加成功",result);
			}else{
				logger.error("updateBusinessSystem insert businessSystem {} ",businessSystem);
				return apiResponse.error(ResponseCode.ERROR_CODE,"添加失败",null);
			}
		}
	}

	@SneakyThrows
	private void updateBusinessTaskList(BusinessSystem businessSystem){
		// 交集 需要检查状态的
		List<String> idListRetain = new ArrayList<>();
		//listCrawlPlatforms中移除idList有的 需要新增的
		List<String> idListAdd = new ArrayList<>();
		//idList 中移除 listCrawlPlatforms 需要删除的
		List<String> idListRemove = new ArrayList<>();

		String taskTypeName = businessSystem.getTaskTypeName();
		if(StringUtils.isEmpty(taskTypeName)){
			return;
		}
		List<String> listCrawlPlatforms = Arrays.asList(taskTypeName.split(","));
		List<BusinessSystemTask> businessSystemTaskBySystemId = businessSystemTaskService.getBusinessSystemTaskBySystemId(businessSystem.getId());
		Map<String,BusinessSystemTask> businessSystemTaskBySystemIdMap = new HashMap();
		if (businessSystemTaskBySystemId == null || businessSystemTaskBySystemId.isEmpty()) {
			idListAdd.addAll(listCrawlPlatforms);
		}else {
			List<String> idList = new ArrayList<>();
			for (BusinessSystemTask businessSystemTask : businessSystemTaskBySystemId) {
				int taskId = businessSystemTask.getTaskId();
				if (!idList.contains(taskId)) {
					idList.add(String.valueOf(taskId));
				}
				businessSystemTaskBySystemIdMap.put(String.valueOf(taskId), businessSystemTask);
			}
			idListRetain.addAll(idList);
			idListAdd.addAll(listCrawlPlatforms);
			idListRemove.addAll(idList);

			idListRetain.retainAll(listCrawlPlatforms);
			idListRemove.removeAll(listCrawlPlatforms);
			idListAdd.removeAll(idList);
		}

		for (String s : idListAdd) {
			BusinessSystemTask businessSystemTask = new BusinessSystemTask();
			businessSystemTask.setTaskId(Integer.parseInt(s));
			Task taskById = taskService.findTaskById(Integer.parseInt(s));
			businessSystemTask.setTaskName(taskById.getTaskName());
			businessSystemTask.setSystemId(businessSystem.getId());
			businessSystemTask.setSystemName(businessSystem.getSystemName());
			businessSystemTask.setStatus(Constant.STATUS_VALID);
			businessSystemTask.setCreateTime(DateUtils.getAfterDays(0));
			businessSystemTaskService.insertBusinessSystemTask(businessSystemTask);
		}

		for (String s : idListRemove) {
			BusinessSystemTask businessSystemTask = new BusinessSystemTask();
			businessSystemTask.setTaskId(Integer.parseInt(s));
			businessSystemTask.setSystemId(businessSystem.getId());
			businessSystemTask.setStatus(Constant.STATUS_INVALID);
			businessSystemTaskService.updateBusinessSystemTaskStatus(businessSystemTask);
		}

		for (String s : idListRetain) {
			BusinessSystemTask businessSystemTask = businessSystemTaskBySystemIdMap.get(s);
			if (businessSystemTask.getStatus() != Constant.STATUS_VALID) {
				businessSystemTask.setStatus(Constant.STATUS_VALID);
				businessSystemTaskService.updateBusinessSystemTaskStatus(businessSystemTask);
			}
		}
	}

	@RequestMapping(value = "/updateBusinessSystemStatus", method = RequestMethod.POST)
	@ResponseBody
	public ApiResponseDomain updateBusinessSystemStatus(BusinessSystem businessSystem) {
		int result;
		ApiResponse apiResponse = new ApiResponse();
		if(!StringUtils.isEmpty(businessSystem.getId())){
			result = businessSystemService.update(businessSystem);
			if (result > 0){
				initSystemCache.updateSystemNameCache();
				logger.debug("updateBusinessSystemStatus  businessSystem {} ",businessSystem);
				return apiResponse.success("更新成功",result);
			}else{
				logger.error("updateBusinessSystemStatus  businessSystem {} ",businessSystem);
				return apiResponse.error(ResponseCode.ERROR_CODE,"更新失败",null);
			}
		}
		logger.error("updateBusinessSystemStatus  businessSystem {} ",businessSystem);
		return  apiResponse.error(ResponseCode.ERROR_CODE,"更新失败",null);
	}

	@RequestMapping(value = "/businessSystemTask", method = RequestMethod.POST)
	@ResponseBody
	public PageDataResult businessSystemTask(@RequestParam("pageNum") Integer pageNum,
									  @RequestParam("pageSize") Integer pageSize, String systemId) {
		int id = Integer.parseInt(systemId);
		PageHelper.startPage(pageNum, pageSize);
		List<BusinessSystemTask> businessSystemTaskBySystemId = businessSystemTaskService.getbstById(id);

		//List<BusinessSystemTask> businessSystemTaskBySystemId = businessSystemTaskService.getBusinessSystemTaskBySystemId(Integer.getInteger(systemId));

		PageDataResult pageDataResult = new PageDataResult();
		if(businessSystemTaskBySystemId.size() != 0){
			PageInfo<BusinessSystemTask> pageInfoNew = new PageInfo<>(businessSystemTaskBySystemId);
			pageDataResult.setList(businessSystemTaskBySystemId);
			pageDataResult.setTotals((int) pageInfoNew.getTotal());
			pageDataResult.setPageNum(pageNum);
		}
		return pageDataResult;
	}

}
