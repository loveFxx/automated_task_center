package com.sailvan.dispatchcenter.common.domain;


import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author wxj
 * @date 21-05-08
 *  店铺帐号实体类
 */
@Data
public class StoreAccount implements Serializable {

    private String id;
    private String platform = "";

    /**
     * 店铺名字
     */
    private String shopName;

    /**
     * 店铺账号
     */
    private String account;

    /**
     * 店铺登录账号
     */
    private String username;

    /**
     * 店铺登录密码
     */
    private String password;

    /**
     * 小类型 0xxx 1xxx 2xxx
     */
    private int smallType = -1;


    /**
     * 大洲
     */
    private String continents;
    private String area;

    private int loginPlatform;

    private String qrContent;

    /**
     * 状态
     */
    private int status;
    private int verificationStatus;

    /**
     * 当前账号绑定的代理IP
     */
    private String proxyIp;
    private int proxyIpPort;
    private int haveMachine;
    private String createdAt;

    /**
     * 用来初始化select搜索框
     */
    private JSONArray accountSelect;
    /**
     * 用来对应店铺所在机器
     */
//    private List<String> machineIp;
//    private List<Integer> machineStatus;
    private List<MachineWorkType> machineWorkTypeList;

}
