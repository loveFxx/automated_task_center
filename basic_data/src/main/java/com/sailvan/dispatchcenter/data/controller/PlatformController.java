package com.sailvan.dispatchcenter.data.controller;


import com.sailvan.dispatchcenter.common.cache.InitMachineCache;
import com.sailvan.dispatchcenter.common.cache.InitPlatformCache;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;
import com.sailvan.dispatchcenter.common.domain.Platform;
import com.sailvan.dispatchcenter.common.pipe.PlatformService;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.data.async.AsyncUpdateCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 平台管理
 * @date 2021-09
 * @author  yyj
 */
@RestController
public class PlatformController {

    @Autowired
    PlatformService platformService;

    @Autowired
    InitPlatformCache initPlatformCache;

    @Autowired
    InitMachineCache initMachineCache;

    @RequestMapping(value = "/getPlatformList", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult getPlatformList(@RequestParam("pageNum") Integer pageNum,
                                          @RequestParam("pageSize") Integer pageSize, Platform platform){

        PageDataResult pdResult = new PageDataResult();
        List<Platform> platformAll = platformService.getPlatform(platform);
        pdResult.setList(platformAll);
        return pdResult;
    }

    /**
     * 修改平台
     * @param platform
     * @return
     */
    @RequestMapping(value = "/updatePlatform", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain updatePlatform(Platform platform) {
        int result;
        ApiResponse apiResponse = new ApiResponse();

        if(!StringUtils.isEmpty(platform.getId())&& platform.getId() != 0){
            //修改
            Platform platformByIdOld = platformService.getPlatformById(platform.getId());
            result = platformService.update(platform);
            if (result > 0){
                initPlatformCache.updateCache();
                if(platformByIdOld.getIsBrowser() != platform.getIsBrowser()){
                    initMachineCache.updateMachineCacheMap(platform);
                }
                return apiResponse.success("修改成功",result);
            }else{
                return apiResponse.error(ResponseCode.ERROR_CODE,"修改失败",null);
            }
        }else {
            return apiResponse.error(ResponseCode.ERROR_CODE,"平台修改不能为空",null);
        }
    }

    /**
     * 添加平台
     * @param platform
     * @return
     */
    @RequestMapping(value = "/addPlatform", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain addPlatform(Platform platform) {
        int result;
        ApiResponse apiResponse = new ApiResponse();
        if(!StringUtils.isEmpty(platform.getPlatformName()) && !StringUtils.isEmpty(platform.getPlatformNameZh())){
            //添加
            List<Platform> platformByPlatform = platformService.getPlatformByPlatform(platform);
            if (!platformByPlatform.isEmpty()) {
                return apiResponse.error(ResponseCode.ERROR_CODE,"该平台已存在","-1");
            }
            platform.setCreatedAt(DateUtils.getCurrentDate());
            result = platformService.insert(platform);
            if (result > 0){
                initPlatformCache.updateCache();
                return apiResponse.success("添加成功",result);
            }else{
                return apiResponse.error(ResponseCode.ERROR_CODE,"添加失败",null);
            }
        }
        return apiResponse.error(ResponseCode.ERROR_CODE,"添加平台不能为空",null);
    }

    /**
     * 删除平台
     * @param platform
     * @return
     */
    @RequestMapping(value = "/deletePlatformById", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain deletePlatformById(Platform platform) {
        int result = platformService.delete(platform.getId());
        ApiResponse apiResponse = new ApiResponse();
        if (result > 0){
            initPlatformCache.updateCache();
            return apiResponse.success("删除成功",result);
        }else{
            return apiResponse.error(ResponseCode.ERROR_CODE, "删除id:"+platform.getId()+"失败",null);
        }
    }
}
