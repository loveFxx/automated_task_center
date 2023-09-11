package com.sailvan.dispatchcenter.common.constant;

/**
 *  店铺与机器的关联
 *  @author mh
 *  @date 2021-12
 */
public class AccountAssociateMachine {

    /**
     *  机器开启，有可用对应的大类型
     */
    public final static int INIT = 0;

    /**
     *  机器开启，有可用对应的大类型
     */
    public final static int ENABLE_AVAILABLE = 1;

    /**
     *  机器开启，无有可用对应的大类型
     */
    public final static int ENABLE_UNAVAILABLE = 2;

    /**
     *  机器关闭，有可用对应的大类型
     */
    public final static int NOTENABLE_AVAILABLE = 3;

    /**
     *  机器关闭，无可用对应的大类型
     */
    public final static int NOTENABLE_UNAVAILABLE = 4;
}
