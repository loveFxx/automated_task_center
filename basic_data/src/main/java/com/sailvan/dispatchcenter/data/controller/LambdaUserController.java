package com.sailvan.dispatchcenter.data.controller;

import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.*;
import com.sailvan.dispatchcenter.common.pipe.*;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.data.init.InitDataLambdaRedisCache;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * lambda用户管理
 * @date 2022-02
 * @author menghui
 */
@RestController
public class LambdaUserController {

	private static Logger logger = LoggerFactory.getLogger(LambdaUserController.class);

	@Autowired
	LambdaUserService lambdaUserService;

	@Autowired
	LambdaUserMapService lambdaUserMapService;

	@Autowired
	AwsLambdaFunctionService lambdaFunctionService;

	@Autowired
	RegionService regionService;

	@Autowired
	InitDataLambdaRedisCache initDataLambdaRedisCache;

	@RequestMapping(value = "/getLambdaUserList", method = RequestMethod.POST)
	@ResponseBody
	public PageDataResult getLambdaUserList(@RequestParam("pageNum") Integer pageNum,
									  @RequestParam("pageSize") Integer pageSize, LambdaUser lambdaUser) {
		PageDataResult pdr = new PageDataResult();
		try {
			pdr = lambdaUserService.getLambdaUserList(lambdaUser, pageNum ,pageSize);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pdr;
	}



	@RequestMapping(value = "/updateLambdaUser", method = RequestMethod.POST)
	@ResponseBody
	public ApiResponseDomain updateLambdaUser(LambdaUserMap lambdaUserMap) {
		ApiResponse apiResponse = new ApiResponse();
		boolean b = false;
		int i = 0;
		LambdaUser lambdaUser = lambdaUserService.getLambdaUserById(lambdaUserMap.getLambdaAccountId());
		//判断是否需要修改user表
		if (lambdaUser != null){
			//userMap表修改 修改成功返回true 修改失败返回false
			b = lambdaUserMapService.updateLambdaUserMap(lambdaUserMap);
			String accessKey = lambdaUser.getAccessKey();
			String accessSecret = lambdaUser.getAccessSecret();
			if (accessKey.equals(lambdaUserMap.getAccessKey())
					&&accessSecret.equals(lambdaUserMap.getAccessSecret())){

			}else {
				//user表修改 修改成功返回1
				SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
				String format = sdf.format(new Date());
				lambdaUser.setAccessKey(lambdaUserMap.getAccessKey());
				lambdaUser.setAccessSecret(lambdaUserMap.getAccessSecret());
				lambdaUser.setUpdatedAt(format);
				i = lambdaUserService.updateLambdaUser(lambdaUser);
			}
		}else {
			return apiResponse.error(ResponseCode.ERROR_CODE,"用户不存在",null);
		}
		//当user表和user Map表同时修改失败则返回修改失败  否则返回修改成功
		if (!b && i ==0){
			return apiResponse.error(ResponseCode.ERROR_CODE,"修改失败",null);
		} else if (b && i ==0){
			return apiResponse.success("仅“用户关联”修改成功",1);
		}else if (!b && i !=0) {
			return apiResponse.success("仅“用户信息”修改成功",1);
		}else {
			return apiResponse.success("修改成功",1);
		}


	}

	@RequestMapping(value = "/addLambdaUser", method = RequestMethod.POST)
	@ResponseBody
	public ApiResponseDomain addLambdaUser(LambdaUser lambdaUser) {
		int result;
		ApiResponse apiResponse = new ApiResponse();
		LambdaUser lambdaUserSelect = new LambdaUser();
		lambdaUserSelect.setAccessKey(lambdaUser.getAccessKey());
		List<LambdaUser> select = lambdaUserService.getLambdaUserByLambdaUser(lambdaUserSelect);
		//判断是否已经存在该用户
		if (select==null || select.isEmpty()) {
			lambdaUser.setCreatedAt(DateUtils.getCurrentDate());
			lambdaUser.setUpdatedAt(DateUtils.getCurrentDate());
			result = lambdaUserService.insertLambdaUser(lambdaUser);
			lambdaUserMapService.insertLambdaUserMap(lambdaUser);
			if (result > 0){
				return apiResponse.success("添加成功",result);
			}else{
				return apiResponse.error(ResponseCode.ERROR_CODE,"添加失败",null);
			}
		}else {
			return apiResponse.error(ResponseCode.ERROR_CODE,"已经存在",null);
		}


	}


	@RequestMapping(value = "/addFunction", method = RequestMethod.POST)
	@ResponseBody
	public ApiResponseDomain addFunction(AwsLambdaFunction lambdaFunction) {
		ApiResponse apiResponse = new ApiResponse();
		if ("".equals(lambdaFunction.getFunctionName())||"".equals(lambdaFunction.getProcessNum())||
		 null == lambdaFunction.getFunctionName()||null==lambdaFunction.getProcessNum()){
			return apiResponse.error(ResponseCode.ERROR_CODE,"请填写完整",null);
		}else {
			AwsLambdaFunction alFunction
					= lambdaFunctionService.getFunctionByFunctionName(lambdaFunction.getFunctionName());
			if (alFunction!= null){
				return apiResponse.error(ResponseCode.ERROR_CODE,"函数已存在",null);
			}else {
				SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
				Date date = new Date();
				String format = sdf.format(date);
				lambdaFunction.setCreatedAt(format);
				lambdaFunction.setUpdatedAt(format);
				lambdaFunctionService.addFunction(lambdaFunction);
				return apiResponse.success("添加成功",1);
			}

		}


	}

	@RequestMapping(value = "/addLambdaUserRelation", method = RequestMethod.POST)
	@ResponseBody
	public ApiResponseDomain addLambdaUserRelation(LambdaUser lambdaUser) {
		ApiResponse apiResponse = new ApiResponse();
		LambdaUserMap lambdaUserMap = new LambdaUserMap();
		LambdaUser lambdaUser1 = lambdaUserService.getLambdaUserByAccountName(lambdaUser.getAccountName());
		AwsRegion region = regionService.getRegionByRegionName(lambdaUser.getRegion());
		AwsLambdaFunction functionByName
				= lambdaFunctionService.getFunctionByFunctionName(lambdaUser.getLambdaFunction());
		lambdaUserMap.setLambdaAccountId(lambdaUser1.getId());
		lambdaUserMap.setRegionId(region.getId());
		lambdaUserMap.setRegion(lambdaUser.getRegion());
		lambdaUserMap.setFunctionId(functionByName.getId());
		lambdaUserMap.setFunctionName(lambdaUser.getLambdaFunction());
		lambdaUserMap.setCreatedAt(DateUtils.getCurrentDate());
		lambdaUserMap.setUpdatedAt(DateUtils.getCurrentDate());
		lambdaUserMap.setAccountName(lambdaUser1.getAccountName());
		LambdaUserMap luMap = lambdaUserMapService.getMapByLambdaUserMap(lambdaUserMap);
		if (luMap!=null){
			return apiResponse.error(ResponseCode.ERROR_CODE,"关联关系已存在",null);
		}else {
			lambdaUserMapService.addLambdaUserMap(lambdaUserMap);
			return apiResponse.success("添加成功",1);
		}
	}

	@RequestMapping(value = "/initCache", method = RequestMethod.POST)
	@ResponseBody
	public void addLambdaUserRelation() {
		initDataLambdaRedisCache.init();
	}

}
