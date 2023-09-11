package com.sailvan.dispatchcenter.data.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.cache.InitAccountCache;
import com.sailvan.dispatchcenter.common.constant.ResponseCode;
import com.sailvan.dispatchcenter.common.domain.StoreAccount;
import com.sailvan.dispatchcenter.common.domain.StoreAccountSites;
import com.sailvan.dispatchcenter.common.pipe.AccountExeTaskService;
import com.sailvan.dispatchcenter.common.response.ApiResponse;
import com.sailvan.dispatchcenter.common.response.PageDataResult;
import com.sailvan.dispatchcenter.common.util.GoogleAuthenticators;
import com.sailvan.dispatchcenter.db.service.StoreAccountService;
import com.sailvan.dispatchcenter.db.service.StoreAccountSitesService;
import com.sailvan.dispatchcenter.common.util.CommonUtils;
import com.sailvan.dispatchcenter.common.domain.ApiResponseDomain;
import com.sailvan.dispatchcenter.db.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;


/**
 * 店铺帐号管理
 *
 * @author menghui
 * @date 2021-04
 */
@RestController
public class StoreAccountController  {

    private static Logger logger = LoggerFactory.getLogger(StoreAccountController.class);

    @Autowired
    StoreAccountService storeAccountService;

    @Autowired
    StoreAccountSitesService storeAccountSitesService;

    @Autowired
    StoreAccountSitesService sitesService;

    @Autowired
    InitAccountCache initAccountCache;

    @Autowired
    TokenService tokenService;

    @Autowired
    AccountExeTaskService accountExeTaskService;


    @RequestMapping(value = "/refreshMiNi", method = RequestMethod.POST)
    @ResponseBody
    public void refreshMiNi() {
        logger.info("refreshMiNi");
        storeAccountService.refreshMiNiAccountProxyIpAccountSite();
    }

    @RequestMapping(value = "/getAccountDetail")
    @ResponseBody
    public String getAccountDetail(@RequestParam("account") String account, @RequestParam("platform") String platform, @RequestParam("area") String area) {
        logger.info("getTokenInfo");
        return tokenService.getAccountDetail(account, platform, area, "");
    }

    @RequestMapping(value = "/refreshAccountCache")
    @ResponseBody
    public String refreshMachineCache() {
        logger.info("refreshAccountCache...");
        initAccountCache.init();
        return "success";
    }


    @RequestMapping(value = "/getValidAccountContinents")
    public JSONArray getValidAccountContinents() {
        logger.info("getValidAccountContinents...");
        return storeAccountService.getValidAccountContinents();
    }

    @RequestMapping(value = "/getValidAccountSites")
    public JSONArray getValidAccountSites() {
        logger.info("getValidAccountSites...");
        return storeAccountService.getValidAccountSites();
    }



    @RequestMapping(value = "/storeAccount", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult getStoreAccountList(@RequestParam("pageNum") Integer pageNum,
                                              @RequestParam("pageSize") Integer pageSize,
                                              Integer accountSiteStatus,
                                              StoreAccount storeAccount) {

        PageDataResult pdr = new PageDataResult();

        //TODO 这里没加 CommonUtils.getPageNum 不懂那个的效果

        // 如果用到了accountSiteStatus根据站点状态查
        if (null!=accountSiteStatus) {
            StoreAccountSites storeAccountSites = new StoreAccountSites();
            storeAccountSites.setStatus(accountSiteStatus);
            if (!StringUtils.isEmpty(storeAccount.getAccount())) {
                String account = storeAccount.getAccount();
                account = account.replaceAll(",","' , '");
                storeAccount.setAccount("( '"+account+"' )");
            }
            pdr= storeAccountService.getStoreAccountBySiteStatus(pageNum, pageSize, storeAccount,accountSiteStatus);
            return pdr;

        }
        try {
            int pageNumTmp = CommonUtils.getPageNum("storeAccount", storeAccount.toString());

            if(null == pageNum || pageNumTmp == 1) {
                pageNum = 1;
            }
            if (null == pageSize) {
                pageSize = 10;
            }
            pdr = storeAccountService.getStoreAccountList(storeAccount, pageNum, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pdr;
    }


    @RequestMapping(value = "/storeAccountSites", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult storeAccountSites(@RequestParam("pageNum") Integer pageNum,
                                            @RequestParam("pageSize") Integer pageSize, StoreAccountSites storeAccount) {
        PageDataResult pdr = new PageDataResult();
        try {
            if (null == pageNum) {
                pageNum = 1;
            }
            if (null == pageSize) {
                pageSize = 10;
            }
            pdr = sitesService.getStoreAccountSitesList(storeAccount, pageNum, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pdr;
    }

    @RequestMapping(value = "/deleteStoreAccount", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain deleteStoreAccount(Integer id) {
        int result = storeAccountService.delete(id);
        ApiResponse apiResponse = new ApiResponse();
        if (result > 0) {
            logger.info(" deleteStoreAccount id {}",id);
            return apiResponse.success("删除成功", result);
        } else {
            logger.error(" deleteStoreAccount id {}",id);
            return apiResponse.error(ResponseCode.ERROR_CODE, "删除id:" + id + "失败", null);
        }
    }

    @RequestMapping(value = "/updateStoreAccount", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain updateStoreAccount(StoreAccount storeAccount) {
        int result;
        ApiResponse apiResponse = new ApiResponse();
        if (!StringUtils.isEmpty(storeAccount.getId())) {
            result = storeAccountService.update(storeAccount);
            if (result > 0) {
                logger.info(" updateStoreAccount storeAccount {}",storeAccount);
                return apiResponse.success("更新成功", result);
            } else {
                logger.error(" updateStoreAccount storeAccount {}",storeAccount);
                return apiResponse.error(ResponseCode.ERROR_CODE, "更新失败", null);
            }
        } else {
            result = storeAccountService.insert(storeAccount);
            if (result > 0) {
                logger.info(" insert storeAccount {}",storeAccount);
                return apiResponse.success("添加成功", result);
            } else {
                logger.error(" insert storeAccount {}",storeAccount);
                return apiResponse.error(ResponseCode.ERROR_CODE, "添加失败", null);
            }
        }
    }


    /**
     * 批量删除任务
     *
     * @param obj
     * @return
     */
    @RequestMapping(value = "/batchDeleteStoreAccount", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponseDomain batchDeleteStoreAccount(@RequestBody JSONObject obj) {

        ApiResponse apiResponse = new ApiResponse();
        String ids = obj.getString("ids");
        JSONArray idsArray = JSONArray.parseArray(ids);
        logger.info("batchDeleteStoreAccount ids {}" , idsArray.toJSONString());
        for (int i = 0; i < idsArray.size(); i++) {
            Integer id = Integer.valueOf(idsArray.get(i).toString());
            int result = storeAccountService.delete(id);
            if (result <= 0) {
                return apiResponse.error(ResponseCode.ERROR_CODE,"系统出现异常，删除id:" + id + "失败", null);
            }
        }

        return apiResponse.success("批量删除成功", null);
    }


    /**
     * 前端调用GoogleAuthenticators#getToTpCode
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/getToTpCodeByQrContent", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object>  getToTpCodeByQrContent(@RequestParam("qrContent") String qrContent)   {

        ApiResponse apiResponse = new ApiResponse();

        Map<String, Object> toTpCode=null;
        try {
            toTpCode = GoogleAuthenticators.getToTpCode(qrContent);
            URI uri = new URI(qrContent);
            String path = uri.getPath();
            path=path.substring(path.indexOf(":")+1);
            toTpCode.put("path", path);

        } catch (Exception e) {
            //return apiResponse.error(ResponseCode.ERROR_CODE,"系统出现异常，删除id:" + id + "失败", null);
        }

        return toTpCode;
    }
    @RequestMapping(value = "/storeAccountAndMachine", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult getStoreAccountAndMachine(@RequestParam("pageNum") Integer pageNum,
                                              @RequestParam("pageSize") Integer pageSize,
                                              StoreAccount storeAccount) {

        PageDataResult pdr = new PageDataResult();
        //TODO 这里没加 CommonUtils.getPageNum 不懂那个的效果
        try {
            int pageNumTmp = CommonUtils.getPageNum("storeAccount", storeAccount.toString());

            if(null == pageNum || pageNumTmp == 1) {
                pageNum = 1;
            }
            if (null == pageSize) {
                pageSize = 10;
            }
            pdr = storeAccountService.getStoreAccountAndMachine(storeAccount, pageNum, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pdr;
    }


    @RequestMapping(value = "/getAccountTaskType", method = RequestMethod.POST)
    @ResponseBody
    public PageDataResult getAccountTaskType(@RequestParam("pageNum") Integer pageNum,
                                                    @RequestParam("pageSize") Integer pageSize,
                                                    StoreAccount storeAccount) {
        PageDataResult pdr = new PageDataResult();
            if(null == pageNum ) {
                pageNum = 1;
            }
            if (null == pageSize) {
                pageSize = 10;
            }
        String account = storeAccount.getAccount();
        String continent = storeAccount.getContinents();
        pdr = accountExeTaskService.getAccountExeTask(account,continent);

        return pdr;
    }

}
