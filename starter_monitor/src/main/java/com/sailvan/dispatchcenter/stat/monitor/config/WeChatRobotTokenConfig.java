package com.sailvan.dispatchcenter.stat.monitor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @program: automated_task_center2
 * @description:
 * @author: Wu Xingjian
 * @create: 2021-10-22 09:55
 **/
@Component
@ConfigurationProperties(prefix = "monitor.wechat-robot")
public class WeChatRobotTokenConfig {

    private String WechatRobotToken;

    private String TestProxyIpUrl;

    public String getWechatRobotToken() {
        return WechatRobotToken;
    }

    public void setWechatRobotToken(String wechatRobotToken) {
        WechatRobotToken = wechatRobotToken;
    }


    public String getTestProxyIpUrl() {
        return TestProxyIpUrl;
    }

    public void setTestProxyIpUrl(String testProxyIpUrl) {
        TestProxyIpUrl = testProxyIpUrl;
    }

}
