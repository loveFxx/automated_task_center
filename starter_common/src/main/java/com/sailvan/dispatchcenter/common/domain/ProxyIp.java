package com.sailvan.dispatchcenter.common.domain;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.sailvan.dispatchcenter.common.constant.Constant;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @program: automated_task_center
 * @description:
 * @author: Wu Xingjian
 * @create: 2021-05-10 15:12
 **/

@Data
public class ProxyIp implements Serializable {
    private int id;

    private String ip;

    private int port;

    /**
     * 代理IP状态字段
     */
    private  int validStatus;

    private String shopName;

    private String crawlPlatform;

    private String updateTime;

    /**
     * 服务提供商
     */
    private String isp;

    /**
     * 上次续费时间
     */
    private String lastRenewalTime;

    /**
     * 过期时间
     */
    private String expireTime;

    /**
     * 代理IP单位时间限制次数配置
     */
    private String limitConfig = "";

    /**
     * 单位时间
     */
    private int unitTime = 1;

    /**
     * 被禁率
     */
    private int maxBannedRate = 80;

    /**
     * 单位时间达到被禁率延迟时间
     */
    private int delayTime = 3;

    /**
     * 代理IP校验次数
     */
    private int validateTimes = 0;

    /**
     * 软删除标志
     */
    private int isDeleted = 0;

    private String ids;

    private List<ProxyIpShop> proxyIpShops;

    private List<AccountProxy> accountProxies;

    private List<ProxyIpPlatform> proxyIpPlatforms;

    /**
     *  搜索出来的 crawlPlatform是平台id，所以需要存储一下名字 用来展示
     */
    private String crawlPlatformName;

    /**
     * 用来初始化select搜索框
     */
    private JSONArray accountSelect;

    /**
     * 用来初始化可爬取搜索框(已经去除存在的账号平台)
     */
    private JSONArray crawlPlatformSelect;

    /**
     * 平台状态的状态 对应搜索框
     */
    private int status = 0;

    private int largeTaskType = Constant.LARGE_TASK_TYPE_CRAWL_PLATFORM;

    /**
     * 账号平台 对应搜索框
     */
    private String platformShop;

    private String account;
}
