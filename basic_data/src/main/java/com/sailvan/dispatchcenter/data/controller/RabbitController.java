package com.sailvan.dispatchcenter.data.controller;

import com.alibaba.fastjson.JSONObject;
import com.sailvan.dispatchcenter.common.cache.InitAccountCache;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.StoreAccount;
import com.sailvan.dispatchcenter.common.util.CommonUtils;
import com.sailvan.dispatchcenter.db.service.StoreAccountService;
import com.sailvan.dispatchcenter.db.service.TokenService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;

/**
 * rabbit
 * @author mh
 * @date 2021-05
 */
@Component
public class RabbitController {

    private static final Logger logger = LoggerFactory.getLogger(RabbitController.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Resource
    TokenService tokenService;

    @Autowired
    StoreAccountService storeAccountService;

    @Autowired
    InitAccountCache initAccountCache;

    @RequestMapping(value="push")
    public String sendPi(@RequestBody String message){
        try {
            rabbitTemplate.convertAndSend("pi-workbench-notice", "pi_to_workbench", message);
        }catch (Exception e){
            return "fail"+ e;
        }
        return "success";
    }

    @RabbitListener(queues = Constant.AUTO_TASK_CENTER, containerFactory = "autoTaskCenterCustomContainerFactory")
    public void processPiWork(Message message, Channel channel) throws Exception {

        try{
            String dataBody = new String(message.getBody());
            logger.info("dataBody: {}", dataBody);
            JSONObject jsonData = JSONObject.parseObject(dataBody);
            String tokenInfo = tokenService.getAccountDetail(String.valueOf(jsonData.get("account_name")), String.valueOf(jsonData.get("platform_name")), String.valueOf(jsonData.get("area")), "");
            logger.info("tokenInfo: {}", tokenInfo);
            if (!StringUtils.isEmpty(tokenInfo)) {
                String platform = String.valueOf(jsonData.get("platform_name"));
                switch (platform){
                    case "Amazon":
                    case "AmazonVC":
                        parseData(jsonData,platform,tokenInfo);
                        break;
                    default:
                        logger.error("Platform[{}] not support",platform);
                        break;
                }
            }
        }finally {
            //手动Ack
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    private void parseData(JSONObject jsonData, String platform, String tokenInfo){
        StoreAccount storeAccount = new StoreAccount();
        storeAccount.setAccount(String.valueOf(jsonData.get("account_name")));
        storeAccount.setPlatform(platform);
        storeAccount.setArea(String.valueOf(jsonData.get("area")));
        if (platform.equals("AmazonVC")){
            String area = CommonUtils.getVCSiteContinent(String.valueOf(jsonData.get("area")));
            storeAccount.setArea(area);
        }
        // 这里只根据account、platform、area查询
        List<StoreAccount> storeAccountByAccountPlatformContinents = storeAccountService.getStoreAccountByParams(storeAccount);
        JSONObject data = storeAccountService.getTokenData( tokenInfo, storeAccount);
        if(data != null){
            if (!storeAccountByAccountPlatformContinents.isEmpty()) {
                storeAccount.setId(storeAccountByAccountPlatformContinents.get(0).getId());
                storeAccount.setAccount(storeAccountByAccountPlatformContinents.get(0).getAccount());
                storeAccount.setContinents(storeAccountByAccountPlatformContinents.get(0).getContinents());
                // 主要更新店铺名或代理IP
                storeAccountService.update(storeAccount);
                storeAccountService.updateStoreAndProxyIp(storeAccountByAccountPlatformContinents.get(0), storeAccount);
                storeAccountService.refreshAccountSites(storeAccount,data);
                StoreAccount storeAccountById = storeAccountService.getStoreAccountById(Integer.parseInt(storeAccount.getId()));
                initAccountCache.updateStoreAccountCache(storeAccountById);
                logger.info("updateStoreAccountCache account:{} success",storeAccount.getAccount());
            }else {
                if(StringUtils.isEmpty(String.valueOf(jsonData.get("account_name"))) && StringUtils.isEmpty(String.valueOf(jsonData.get("platform_name")))
                        && StringUtils.isEmpty(String.valueOf(jsonData.get("area")))){
                    // 如果三者都不是空，说明 有新账号添加 需要重新刷新
                    storeAccountService.refreshMiNiAccountProxyIpAccountSite();
                }
                logger.error("Rabbit insert storeAccount {}",storeAccount);
            }
        }
    }
}
