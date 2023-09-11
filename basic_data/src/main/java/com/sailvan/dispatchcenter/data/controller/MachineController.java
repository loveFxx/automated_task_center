package com.sailvan.dispatchcenter.data.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.Machine;
import com.sailvan.dispatchcenter.common.domain.MachineWorkType;
import com.sailvan.dispatchcenter.common.domain.Task;
import com.sailvan.dispatchcenter.common.pipe.*;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.CommonUtils;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;
import com.sailvan.dispatchcenter.common.cache.InitMachineCache;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.MachineXmlUtil;
import com.sailvan.dispatchcenter.data.scheduler.MachineExeTaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;


/**
 * 爬虫服务器管理
 * @date 2021-04
 * @author menghui
 */
@RestController
public class MachineController {

	private static Logger logger = LoggerFactory.getLogger(MachineController.class);

	@Autowired
	MachineService machineService;

	@Autowired
	StoreAccountService storeAccountService;


	@Autowired
	MachineWorkTypeService machineWorkTypeService;


	@Autowired
	MachineHeartbeatLogsService machineHeartbeatLogsService;

	@Autowired
    InitMachineCache initMachineCache;

	@Autowired
	MachineExeTaskScheduler machineExeTaskScheduler;

	@RequestMapping(value = "/refreshMiNiMachine", method = RequestMethod.POST)
	@ResponseBody
	public void refreshMiNiMachine() {
		// 获取用户列表
		logger.info("refreshMiNiMachine");
		storeAccountService.refreshMiNiMachine();
	}

	@RequestMapping(value = "/refreshMachineExeTask")
	@ResponseBody
	public void refreshMachineExeTask(@RequestParam("data") int data) {
		// 获取用户列表
		logger.info("refreshMachineExeTask");
		machineExeTaskScheduler.refreshMachineExeTask(data);
		logger.info("refreshMachineExeTask over");
	}



	@RequestMapping(value = "/refreshMachineCache")
	@ResponseBody
	public String refreshMachineCache() {
		logger.info("refreshMachineCache...");
		initMachineCache.init();
		logger.info("refreshMachineCache... over");
		return "success";
	}


	@RequestMapping(value = "/refreshMachineWorkTypeTask")
	@ResponseBody
	public String refreshMachineWorkTypeTask() throws Exception {
		machineService.refreshMachineWorkTypeTask();
		initMachineCache.init();
		logger.info("refreshMachineWorkTypeTask... over");
		return "success";
	}

	@RequestMapping(value = "/importXML")
	@ResponseBody
	public String importXML(HttpServletResponse response) {
		logger.info("importXML...");
		List<Machine> machineAll = machineService.getMachineAll();
		File file = MachineXmlUtil.createXml(machineAll);
		if(!file.exists()){
			return "下载文件不存在";
		}
		response.reset();
		response.setContentType("application/octet-stream");
		response.setCharacterEncoding("utf-8");
		response.setContentLength((int) file.length());
		response.setHeader("Content-Disposition", "attachment;filename=" + file.getName() );

		try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));) {
			byte[] buff = new byte[1024];
			OutputStream os  = response.getOutputStream();
			int i = 0;
			while ((i = bis.read(buff)) != -1) {
				os.write(buff, 0, i);
				os.flush();
			}
		} catch (IOException e) {
			return "下载失败";
		}finally {
			file.delete();
		}
		return "下载成功";
	}



	@RequestMapping(value = "/getMachineList", method = RequestMethod.POST)
	@ResponseBody
	public PageDataResult getMachineList(@RequestParam("pageNum") Integer pageNum,
									  @RequestParam("pageSize") Integer pageSize, Machine machine) {
		PageDataResult pdr = new PageDataResult();
		try {
			int pageNumTmp = CommonUtils.getPageNum("machine", machine.toString());

			if(null == pageNum || pageNumTmp == 1) {
				pageNum = 1;
			}
			if(null == pageSize) {
				pageSize = 10;
			}
			if (logger.isTraceEnabled()) {
				logger.trace("getMachineList machine {}, pageNum {}, {}pageSize", machine, pageNum, pageSize);
			}
			pdr = machineService.getMachineList(machine, pageNum ,pageSize);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pdr;
	}


	@RequestMapping(value = "/showMachineTaskType", method = RequestMethod.POST)
	@ResponseBody
	public PageDataResult showMachineTaskType(@RequestParam("pageNum") Integer pageNum,
                                              @RequestParam("pageSize") Integer pageSize,
											  MachineWorkType taskType) {
		PageDataResult pdr = new PageDataResult();
		try {
			pdr = machineWorkTypeService.getMachineWorkTypeList(taskType,0,10);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pdr;
	}

	@RequestMapping(value = "/deleteMachineById", method = RequestMethod.POST)
	@ResponseBody
	public ApiResponseDomain deleteCrawlerServer(Integer id) {
		ApiResponse apiResponse = new ApiResponse();
		int result = machineService.delete(id);
		if (result > 0){
			return apiResponse.success("删除成功",result);
		}else{
			return apiResponse.error(ResponseCode.ERROR_CODE, "删除id:"+id+"失败",null);
		}
	}

	@RequestMapping(value = "/updateMachine", method = RequestMethod.POST)
	@ResponseBody
	public ApiResponseDomain updateMachine(Machine machine) {
		int result;
		ApiResponse apiResponse = new ApiResponse();
        if(!StringUtils.isEmpty(machine.getId()) && machine.getId()>0){
			machine.setNetWork(Constant.STATUS_DISABLE);
        	result = machineService.update(machine);
			if (result > 0){
				logger.debug("updateCrawlerServer update machine {}",machine);
				initMachineCache.updateMachineCacheMap(machine);
				return apiResponse.success("更新成功",result);
			}else{
				logger.error("updateCrawlerServer update machine {}",machine);
				return apiResponse.error(ResponseCode.ERROR_CODE, "更新失败",null);
			}
		}else {
			Machine select = initMachineCache.getMachineCacheMapCacheByIp(machine.getIp());
			if (select==null) {
				machine.setStatus(Constant.STATUS_INVALID);
				machine.setCreatedTime(DateUtils.getCurrentDate());
				result = machineService.insert(machine);
				if (result > 0){
					logger.debug("updateCrawlerServer insert machine {}",machine);
					initMachineCache.updateMachineCacheMap(machine);
					return apiResponse.success("添加成功",result);
				}else{
					logger.error("updateCrawlerServer insert machine {}",machine);
					return apiResponse.error(ResponseCode.ERROR_CODE,"添加失败",null);
				}
			}else {
				return apiResponse.error(ResponseCode.ERROR_CODE,"已经存在",null);
			}

		}
	}


	@RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
	@ResponseBody
	public ApiResponseDomain updateStatus(Machine machine) {
		int result;
		ApiResponse apiResponse = new ApiResponse();
		result = machineService.updateStatus(machine.getId(),machine.getStatus());
		if (result > 0){
			logger.debug("updateCrawlerServer update machine {}",machine);
			machine.setUpdateMachineStatus(Constant.STATUS_VALID);
			initMachineCache.updateMachineCacheMap(machine);
			return apiResponse.success("更新成功",result);
		}else{
			logger.error("updateCrawlerServer update machine {}",machine);
			return apiResponse.error(ResponseCode.ERROR_CODE, "更新失败",null);
		}

	}

	@RequestMapping(value = "/updateMachineTaskTypeStatus", method = RequestMethod.POST)
	@ResponseBody
	public ApiResponseDomain updateMachineTaskTypeStatus(MachineWorkType machine) {
		int result;
		ApiResponse apiResponse = new ApiResponse();
		if(!StringUtils.isEmpty(machine.getId())){
			machine.setIsUpdate(Constant.STATUS_IS_UPDATE);
			result = machineWorkTypeService.updateStatus(machine);
			if (result > 0){
				logger.debug("updateMachineTaskTypeStatus insert MachineTaskType {}",machine);
				initMachineCache.updateMachineCacheMap(machine);
				return apiResponse.success("更新成功",result);
			}else{
				logger.error("updateMachineTaskTypeStatus insert MachineTaskType {}",machine);
				return apiResponse.error(ResponseCode.ERROR_CODE,"更新失败",null);
			}
		}
		return  apiResponse.error(ResponseCode.ERROR_CODE,"更新失败",null);
	}


	@RequestMapping(value = "/updateMachineWorkTypeIsBrowser", method = RequestMethod.POST)
	@ResponseBody
	public ApiResponseDomain updateMachineWorkTypeIsBrowser(MachineWorkType machine) {
		ApiResponse apiResponse = new ApiResponse();
		int result = machineWorkTypeService.updateMachineWorkTypeIsBrowser(machine);
		if (result > 0){
			logger.debug("updateMachineTaskTypeStatus insert MachineTaskType {}",machine);
			MachineWorkType machineWorkTypeById = machineWorkTypeService.getMachineWorkTypeById(machine.getId());
			initMachineCache.updateMachineCacheMap(machineWorkTypeById);
			return apiResponse.success("更新成功",result);
		}else{
			logger.error("updateMachineTaskTypeStatus insert MachineTaskType {}",machine);
			return apiResponse.error(ResponseCode.ERROR_CODE,"更新失败",null);
		}
	}


	/**
	 *  批量删除任务
	 * @param obj
	 * @return
	 */
	@Deprecated
	@RequestMapping(value = "/batchDeleteMachine", method = RequestMethod.POST)
	@ResponseBody
	public ApiResponseDomain batchDeleteCrawlerServer(@RequestBody JSONObject obj) {
		ApiResponse apiResponse = new ApiResponse();
		String ids = obj.getString("ids");
		JSONArray idsArray= JSONArray.parseArray(ids);
		logger.debug("batchDeleteCrawlerServer ids {}",ids);
		for(int i=0;i<idsArray.size();i++){
			Integer id = Integer.valueOf(idsArray.get(i).toString());
			int result = machineService.delete(id);
			if (result<=0){
				return apiResponse.error(ResponseCode.ERROR_CODE, "系统出现异常，删除id:"+id+"失败",null);
			}
		}
		return apiResponse.success("批量删除成功",null);
	}


	@RequestMapping(value = "/getMachineHeartbeat", method = RequestMethod.POST)
	@ResponseBody
	public PageDataResult getMachineHeartbeatLogsByMachineIdList(Integer machineId) {
		PageDataResult pdr = new PageDataResult();
		pdr= machineHeartbeatLogsService.getMachineHeartbeatLogsByMachineIdList(machineId);
		return pdr;
	}

}
