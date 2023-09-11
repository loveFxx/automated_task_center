package com.sailvan.dispatchcenter.data.controller;

import com.sailvan.dispatchcenter.common.config.CoreServiceAddressConfig;
import com.sailvan.dispatchcenter.common.domain.TaskResult;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.TaskUtil;
import com.sailvan.dispatchcenter.db.service.TaskResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 任务管理类
 * @date 2021-04
 * @author menghui
 */
@RestController
public class TaskResultController {

	private static Logger logger = LoggerFactory.getLogger(TaskResultController.class);

	@Autowired
	TaskResultService taskResultService;

	@Autowired
	CoreServiceAddressConfig coreServiceAddressConfig;

	@Autowired
	TaskUtil taskUtil;


	/**
	 * 根据前端传来curPageMinId和curPageMaxId判断是首页、跳下一页、跳上一页。执行对应service方法
	 */
	@RequestMapping(value = "/getTaskResultList", method = RequestMethod.POST)
	@ResponseBody
	public PageDataResult getTaskResultList( Integer curPageMinId,Integer curPageMaxId,TaskResult taskResult) {
		//拼接taskType
		try {
			if (!StringUtils.isEmpty(taskResult.getTaskType())) {
				String taskType = taskResult.getTaskType();
				taskType = taskType.replaceAll(",","' , '");
				taskResult.setTaskType("( '"+taskType+"' )");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		PageDataResult pdr = new PageDataResult();
		//传进来null,null->首页
		if(curPageMaxId==null&&curPageMinId==null){
			int maxId = taskUtil.getIdCache("taskResultId",coreServiceAddressConfig.getPath());
			pdr = taskResultService.getFirstTaskResultList(maxId,taskResult);
		}
		//传进来27143771,null->跳下一页
		else if (curPageMaxId == null) {
			pdr = taskResultService.getNextTaskResultList(curPageMinId,taskResult);
		}
		//传进来null,27143771->跳上一页时传进来
		else if (curPageMinId == null) {
			pdr = taskResultService.getLastTaskResultList(curPageMaxId,taskResult);
		}
		return pdr;
	}


	@RequestMapping(value = "/getTaskResultCount", method = RequestMethod.POST)
	@ResponseBody
	public int getTaskResultCount( TaskResult taskResult) {

		if (!StringUtils.isEmpty(taskResult.getTaskType())) {
			String taskType = taskResult.getTaskType();
			taskType = taskType.replaceAll(",","' , '");
			taskResult.setTaskType("( '"+taskType+"' )");
		}

			return taskResultService.getTaskResultCount(taskResult);


	}


	@RequestMapping(value = "/getMaxTaskResultId", method = RequestMethod.POST)
	@ResponseBody
	public int getMaxTaskResultId() {
		return taskResultService.getMaxTaskResultId();
	}










}
