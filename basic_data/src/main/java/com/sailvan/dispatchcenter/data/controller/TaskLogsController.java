package com.sailvan.dispatchcenter.data.controller;

import com.sailvan.dispatchcenter.common.domain.TaskLogs;
import com.sailvan.dispatchcenter.common.cache.InitTaskCache;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.db.service.TaskLogsService;
import com.sailvan.dispatchcenter.common.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 任务管理类
 * @date 2021-04
 * @author menghui
 */
@RestController
public class TaskLogsController {

	private static Logger logger = LoggerFactory.getLogger(TaskLogsController.class);

	@Autowired
	TaskLogsService taskLogsService;

	@Autowired
    InitTaskCache initTaskCache;


	/**
	 * 任务流水
	 */
	@RequestMapping(value = "/taskLogs", method = RequestMethod.POST)
	@ResponseBody
	public PageDataResult getTaskLogsList(@RequestParam("pageNum") Integer pageNum,
										  @RequestParam("pageSize") Integer pageSize,
										  @RequestParam(required = false) String startTime,
										  @RequestParam(required = false) String endTime,
										  TaskLogs taskLogs) {
		System.out.println("taskLogs = " + taskLogs.toString());
		PageDataResult pdr = new PageDataResult();
		try {
			int pageNumTmp = CommonUtils.getPageNum("taskLogs", taskLogs.toString());

			if(null == pageNum || pageNumTmp == 1) {
				pageNum = 1;
			}
			if(null == pageSize) {
				pageSize = 10;
			}
			// 获取用户列表
			pdr = taskLogsService.getTaskLogsList(taskLogs, pageNum ,pageSize,startTime,endTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pdr;
	}

}
