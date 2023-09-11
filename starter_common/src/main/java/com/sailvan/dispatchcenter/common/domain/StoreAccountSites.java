package com.sailvan.dispatchcenter.common.domain;


import com.sailvan.dispatchcenter.common.constant.Constant;
import lombok.Data;

import java.io.Serializable;

/**
 * @author meng
 * @date 21-06
 *  店铺帐号支持站点实体类
 */
@Data
public class StoreAccountSites implements Serializable {

    private String id;
    private String accountId;

    private String account;
    private String continents;

    /**
     * 站点
     */
    private String site;

    /**
     * 机器验证，默认未验证 -2
     */
    private int statusMachine = Constant.STATUS_UNVERIFIED;

    private String statusMachineMean;

    /**
     * 人工验证 默认未验证 -2
     */
    private int statusPerson = Constant.STATUS_UNVERIFIED;

    private int status = Constant.STATUS_DISABLE;

    /**
     *  是否解绑信用卡（收款账号），1已绑定 0已解绑
     */
    private int payment ;

    private String clientMsg;
    private String clientError;

}
