package com.sailvan.dispatchcenter.data.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.sailvan.dispatchcenter.common.cache.InitValidVersionCache;
import com.sailvan.dispatchcenter.common.config.FtpConfigInner;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;
import com.sailvan.dispatchcenter.common.domain.Version;
import com.sailvan.dispatchcenter.common.pipe.VersionService;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.FTPClientUtils;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * 客户端版本管理
 *
 * @author menghui
 * @date 2021-07
 */
@RestController
public class VersionController {

    private static Logger logger = LoggerFactory.getLogger(VersionController.class);

    @Autowired
    VersionService versionService;

    @Autowired
    InitValidVersionCache initValidVersionCache;

    @Autowired
    FtpConfigInner ftpConfigInner;

    @Autowired
    RedisUtils redisUtils;

    @RequestMapping(value = "/refreshValidVersionCache")
    @ResponseBody
    public String refreshValidVersionCache() {
        initValidVersionCache.init();
        return "success";
    }

    @RequestMapping(value = "/getVersionList")
    @ResponseBody
    public PageDataResult getVersionList(@RequestParam("pageNum") Integer pageNum,
                                         @RequestParam("pageSize") Integer pageSize, Version version) {

        PageDataResult pdr = new PageDataResult();
        try {
            pdr = versionService.getVersionList(version, pageNum, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pdr;
    }


    /**
     * 删除版本
     */
    @RequestMapping(value = "/deleteVersionById", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain deleteVersionById(Version version) throws IOException {
        ApiResponse apiResponse = new ApiResponse();

        int result=0;
        try {
             result = versionService.deleteVersionById(version);
        }catch (Exception e){
            return apiResponse.error(ResponseCode.ERROR_CODE, "未知失败", "");
        }

        if (result == 1) {
            initValidVersionCache.init();
            return apiResponse.success("删除成功", "");

        }else if(result==0){
            return apiResponse.error(ResponseCode.ERROR_CODE, "ftp连接失败", "");
        } else {
            return apiResponse.error(ResponseCode.ERROR_CODE,"删除失败", "");
        }
    }


    /**
     * 更新版本的status和updateLimit
     */
    @RequestMapping(value = "/updateVersion", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain updateVersion(Version version) {
        int result;
        ApiResponse apiResponse = new ApiResponse();
        result = versionService.update(version);
        String resetAll = version.getResetAll();
        if ("0".equals(resetAll)){
            //非全量更新
            if (result > 0) {
                logger.debug("updateVersion update Version {} ", version);
                initValidVersionCache.init();
                return apiResponse.success("更新成功", result);
            } else {
                logger.error("updateVersion update Version {} ", version);
                return apiResponse.error(ResponseCode.ERROR_CODE, "更新失败", null);
            }
        }else {
            //全量更新 遍历文件夹中所有文件
            Map allFile = FTPClientUtils.getAllFile(ftpConfigInner, "/web/version/app");
            //存入缓存
            initValidVersionCache.putVersionAll(allFile,version);
        }
        return apiResponse.success("更新成功",1);

    }


    /**
     * 增加父版本或者子版本 版本号可重复
     */
    @RequestMapping(value = "/addVersion", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain addVersion( @RequestBody Map<String, Object> map) {
        Version  fatherVersion = JSON.parseObject(JSON.toJSONString((LinkedHashMap)map.get("fatherVersion")), new TypeReference<Version>() {});
        Version  sonVersion = JSON.parseObject(JSON.toJSONString((LinkedHashMap)map.get("sonVersion")), new TypeReference<Version>() {});


        int result;
        ApiResponse apiResponse = new ApiResponse();
        result = versionService.insertFatherAndSon(fatherVersion,sonVersion);
        if (result > 0) {
            initValidVersionCache.init();
            return apiResponse.success("addVersion成功", result);
        } else {
            return apiResponse.error(ResponseCode.ERROR_CODE, "添加失败", null);
        }

    }


    /**
     * 上传文件
     */
    @RequestMapping(value = "/uploadVersion", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain uploadVersionFile(@RequestParam(name = "file") MultipartFile file, Version version) {
        ApiResponse apiResponse = new ApiResponse();
        int res=0;
        Thread t = Thread.currentThread();
        String name = t.getName();
        System.out.println("name=" + name + "进入了controller uploadVersionFile");
        if (file.isEmpty()) {
            return apiResponse.error(ResponseCode.ERROR_CODE, "文件是空", "");
        }
        try {
            res=versionService.uploadVersionFile(file, version);
        } catch (Exception e) {
            e.printStackTrace();
            return apiResponse.error(ResponseCode.ERROR_CODE, "未知失败", "");
        }
        if(res==1){
            initValidVersionCache.init();
            return apiResponse.success(file.getOriginalFilename()+"上传成功", "");

        }else if(res==0){
            return apiResponse.error(ResponseCode.ERROR_CODE, "ftp建立连接失败", "");
        }else{
            return apiResponse.error(ResponseCode.ERROR_CODE, file.getOriginalFilename()+"上传失败", "");
        }
    }

    @RequestMapping(value = "/getProcess")
    public HashMap<String, LinkedHashMap<String, Integer>> getProcess() {
        return FTPClientUtils.progressMap;
    }


    /**
     * 点开版本后异步获取文件映射
     * 虽然返回的是PageDataResult 但是并没有用到pageInfo分页类
     */
    @RequestMapping(value = "/getVersionFileMap")
    @ResponseBody
    public PageDataResult getVersionFileMap(Integer versionId) {
        return versionService.getVersionFileMap(versionId);
    }


    /**
     * 根据id获取版本 用于修改版本内容时异步获取每个版本的updateLimit
     */

    @RequestMapping(value = "/getVersionById")
    @ResponseBody
    public ApiResponseDomain getVersionById(Integer versionId) {

        ApiResponse apiResponse = new ApiResponse();
        Version version = versionService.getVersionById(versionId);
        if (version.getId() != 0) {
            return apiResponse.success("查询成功", version);
        } else {
            return apiResponse.error(ResponseCode.ERROR_CODE, "查询失败", null);
        }
    }


    /**
     * 修改某版本文件和客户端上文件位置配置
     */
    @RequestMapping(value = "/updateVersionFileMap")
    @ResponseBody
    public ApiResponseDomain updateVersionFileMap(@RequestParam Integer id, String clientFilePath,String fileName) {

        int result;
        ApiResponse apiResponse = new ApiResponse();
        result = versionService.updateVersionFileMap(id, clientFilePath,fileName);
        if (result > 0) {
            initValidVersionCache.init();
            return apiResponse.success("更新成功", result);
        } else {
            return apiResponse.error(ResponseCode.ERROR_CODE, "更新失败", null);
        }
    }


    /**
     * 删除已上传的mysql+文件
     *
     * @params fileId用来看文件id version用来看版本
     */
    @RequestMapping(value = "/deleteFile")
    @ResponseBody
    public ApiResponseDomain deleteFile(Integer fileId, String fileName, Version version) throws IOException {
        int result;
        ApiResponse apiResponse = new ApiResponse();
        try {
            result = versionService.deleteFile(fileId, fileName, version);
        } catch (Exception e) {
            e.printStackTrace();
            return apiResponse.error(ResponseCode.ERROR_CODE, "未知错误", "");
        }

        if (result == 1) {
            initValidVersionCache.init();
            return apiResponse.success("删除成功", "");
        }else if(result==0) {
            return apiResponse.error(ResponseCode.ERROR_CODE, "ftp建立连接失败", "");
        }
        else {
            return apiResponse.error(ResponseCode.ERROR_CODE, "删除失败", "");
        }

    }



    /**
     * 仅从数据库里删除一条文件配置 不删除文件
     *
     * @params
     */
    @RequestMapping(value = "/deleteFileMap")
    @ResponseBody
    public ApiResponseDomain deleteFileMap(Integer fileId) throws IOException {

        int result;
        ApiResponse apiResponse = new ApiResponse();

        result = versionService.deleteFileMap(fileId);

        if (result > 0) {
            initValidVersionCache.init();
            return apiResponse.success("删除成功", result);
        } else {
            return apiResponse.error(ResponseCode.ERROR_CODE, "删除失败", null);
        }


    }





    /**
     * 每次上传文件前清空之前的进度list 然后批量添加
     */
    @RequestMapping(value = "/initFilePercentMapList", method = RequestMethod.POST)
    //@ResponseBody
    public void initFilePercentMapList(@RequestBody JSONObject obj) {
        //TODO 先清空
        if (!FTPClientUtils.progressMap.isEmpty()) {
            FTPClientUtils.progressMap.clear();
        }
    }




    /**
     * 增加某版本文件和客户端上文件位置配置
     */
    @RequestMapping(value = "/addVersionFileMap")
    @ResponseBody
    public ApiResponseDomain addVersionFileMap(@RequestParam Integer versionId, @RequestParam String fileName,@RequestParam String clientFilePath) {

        int result;
        ApiResponse apiResponse = new ApiResponse();
        result = versionService.addVersionFileMap(versionId, fileName,clientFilePath);
        if (result > 0) {
            initValidVersionCache.init();
            return apiResponse.success("更新成功", result);
        } else {
            return apiResponse.error(ResponseCode.ERROR_CODE, "更新失败", null);
        }
    }

}
