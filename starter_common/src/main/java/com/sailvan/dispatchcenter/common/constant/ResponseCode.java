package com.sailvan.dispatchcenter.common.constant;

import java.util.HashMap;
import java.util.Map;

/**
 *   响应码
 * @date 2021-06
 * @author menghui
 */
public class ResponseCode {

    public static  Map<Integer, String> RESPONSE_CODE_MAP = new HashMap<>();
    public static  Map<Integer, String> ACCOUNT_CODE_MAP = new HashMap<>();
    public static  Map<Integer, String> ERROR_CODE_MAP = new HashMap<>();

    /**
     * 强制成功结束
     */
    public final static int DONE_SUCCESS_CODE = -1;



    /**
     * 强制错误结束
     */
    public final static int DONE_ERROR_CODE = -2;



    /**
     * 选择下一个执行的任务
     */
    public final static int CHOSE_NEXT = -3;
    public final static int SUCCESS_CODE = 1;

    /**
     * 商品被删除
     */
    public final static int PRODUCT_DELETED_CODE = 2;

    /**
     *  非20状态的开头的
     */
    public final static int HTTP_ERROR_CODE = 3;

    /**
     * 页面被封禁
     */
    public final static int IP_BLOCKED_CODE = 4;

    /**
     * 访问失败 页面没有响应complete
     */
    public final static int GOTO_ERROR_CODE = 5;


    /**
     * 页面成功但是没有指定元素
     */
    public final static int GET_HTTP_CHECK_ERROR_CODE = 6;


    /**
     * 客户端执行任务失败
     */
    public final static int SCRIPT_ERROR_CODE = 7;

    /**
     * 通用报错
     */
    public final static int ERROR_CODE = 8;


    /**
     * 机器相关的报错
     */
    public final static int MACHINE_ERROR_CODE = 9;


    /**
     * 注册相关的报错
     */
    public final static int REGISTERED_ERROR_CODE = 10;


    /**
     * api相关的报错
     */
    public final static int API_ERROR_CODE = 11;


    /**
     * 账号机没有注册
     */
    public final static int NO_REGISTERED_CODE = 12;


    /**
     * 账号机不能注册
     */
    public final static int CAN_NOT_REGISTERED_CODE = 13;


    /**
     * 没有任务
     */
    public final static int NO_JOB_CODE = 14;


    /**
     * 找不到元素
     */
    public final static int NO_FIND_DOM_CODE = 15;


    /**
     * 页面返回超时并没有返回响应码
     */
    public final static int GOTO_TIMEOUT_CODE = 16;


    /**
     * 亚马逊后台需要重置密码
     */
    public final static int NEED_TO_RESET_PASSWORD = 17;


    /**
     * 亚马逊后台大洲没有授权
     */
    public final static int NOT_AUTHORIZED = 18;


    /**
     * 亚马逊后台大洲没有选择默认站点
     */
    public final static int NEED_TO_CHOSE_DEFAULT_SITE = 19;


    /**
     * 亚马逊后台大洲没有设置二步验证
     */
    public final static int NEED_TO_SET_TWO_STEP_VERIFICATION = 20;


    /**
     * 亚马逊后台大洲没有设置单点登录
     */
    public final static int NEED_TO_SET_SINGLE_LOGIN = 21;


    /**
     * 亚马逊后台未知登录失败
     */
    public final static int UNKNOWN_LOGIN_ERROR = 22;


    /**
     * 亚马逊账号被锁定
     */
    public final static int ACCOUNT_LOCK = 23;


    /**
     * 亚马逊密码错误
     */
    public final static int PASSWORD_ERROR = 24;


    /**
     * 亚马逊站点没有绑定信用卡
     */
    public final static int NO_CREDIT_CARD = 25;


    /**
     * 图片验证错误
     */
    public final static int CAPTCHA_IMAGE_ERROR = 26;


    /**
     * 二步验证错误
     */
    public final static int TWO_STEP_ERROR = 27;


    /**
     * 二步验证二维码无效
     */
    public final static int VERIFY_CODE_ERROR = 28;


    /**
     * 没有开启二步验证的二维码功能
     */
    public final static int NO_OPEN_VERIFY = 29;


    /**
     * 需要上传二维码
     */
    public final static int NEED_TO_UPLOAD_QCCODE = 30;


    /**
     * 找不到站点的选项
     */
    public final static int NOT_SITE = 31;


    /**
     * 切换站点失败
     */
    public final static int CHOSE_SITE_ERROR = 32;


    /**
     * 切换语言失败
     */
    public final static int CHOSE_LANG_ERROR = 33;


    /**
     * 切换站点失败
     */
    public final static int NOT_FULFILLMENT_REPORTS_BUTTON = 34;


    /**
     * 选择日期失败
     */
    public final static int FULFILLMENT_REPORTS_DATE_ERROR = 35;


    /**
     * fba非工作状态
     */
    public final static int FBA_BOX_NO_IN_WORKING = 36;


    /**
     * fba 没有上传按钮
     */
    public final static int FBA_BOX_NO_FILE_INPUT = 37;

    /**
     * fba 没有上传按钮
     */
    public final static int FBA_BOX_CLICK_FILE_INPUT_ERROR = 38;


    /**
     * fba 没有下载按钮
     */
    public final static int FBA_BOX_NO_DOWNLOAD_BUTTON = 39;


    /**
     * fba 需要先下载Excel
     */
    public final static int NEED_TO_DOWNLOAD_EXCEL = 40;


    /**
     * 退款订单
     */
    public final static int REFUNDS_ORDER_CODE = 41;


    /**
     * 店铺站点被禁
     */
    public final static int ACCOUNT_SITE_DEACTIVATED = 42;


    /**
     * 店铺假日模式
     */
    public final static int ACCOUNT_HOLIDAY = 43;

    /**
     * 没有银行卡信息
     */
    public final static int EMPTY_BANK_CARD = 45;

    /**
     * 获取后台二步验证二维码失败
     */
    public final static int QC_CODE_ERROR = 51;

    /**
     * 客户端超时(未取任务、结果无返回)
     */
    public final static int CLIENT_SERVER_TIMEOUT = 55;

    /**
     * 店铺状态无效拦截
     */
    public final static int INVALID_ACCOUNT = 56;

    /**
     * lambda执行异常
     */
    public final static int LAMBDA_ERROR = 57;

    /**
     * 未知错误
     */
    public final static int UNkOWN_ERROR = 406;

    public final static int NO_MACHINE_ERROR = 408;

    /**
     * 注册报错: unique_id 传入参数是空
     */
    public final static int REGISTER_PARAM_ERROR_CODE = 501;

    /**
     * 注册报错: db is empty 数据库没有注册的IP
     */
    public final static int REGISTER_DB_EMPTY_ERROR_CODE = 503;

    /**
     * 客户端请求头没有携带token
     */
    public final static int CLIENT_NO_TOKEN_ERROR_CODE = 511;

    /**
     * 客户端token验证失败
     */
    public final static int CLIENT_UNAUTHORIZED_TOKEN_ERROR_CODE = 522;

    /**
     * 业务端端token验证失败
     */
    public final static int BUSINESS_UNAUTHORIZED_TOKEN_ERROR_CODE = 532;

    static {
        ACCOUNT_CODE_MAP.put(Constant.ACCOUNT_STATUS_NORMAL_NOT_OPERATION, "正常未运营的店铺");
        ERROR_CODE_MAP.put(Constant.ACCOUNT_STATUS_NORMAL_NOT_OPERATION, "ACCOUNT_STATUS_CLOSE_SHOP_CANNOT_LOGIN");

        ACCOUNT_CODE_MAP.put(Constant.ACCOUNT_STATUS_NORMAL_IN_OPERATION, "正常运营中的店铺");
        ERROR_CODE_MAP.put(Constant.ACCOUNT_STATUS_NORMAL_IN_OPERATION, "ACCOUNT_STORES_IN_NORMAL_OPERATION");

        ACCOUNT_CODE_MAP.put(Constant.ACCOUNT_STATUS_CLOSE_SHOP_CANNOT_LOGIN, "关店不可登录");
        ERROR_CODE_MAP.put(Constant.ACCOUNT_STATUS_CLOSE_SHOP_CANNOT_LOGIN, "ACCOUNT_CLOSED_STORE_CANNOT_LOGIN");

        ACCOUNT_CODE_MAP.put(Constant.ACCOUNT_STATUS_CLOSE_SHOP_CAN_LOGIN, "关店可登录");
        ERROR_CODE_MAP.put(Constant.ACCOUNT_STATUS_CLOSE_SHOP_CAN_LOGIN, "ACCOUNT_STATUS_CLOSE_SHOP_CAN_LOGIN");

        ACCOUNT_CODE_MAP.put(Constant.ACCOUNT_STATUS_SUSPENSION_OPERATIONS, "假期模式");
        ERROR_CODE_MAP.put(Constant.ACCOUNT_STATUS_SUSPENSION_OPERATIONS, "ACCOUNT_VACATION_MODE");

        ACCOUNT_CODE_MAP.put(Constant.ACCOUNT_STATUS_INVALID_SHOP, "无效店铺");
        ERROR_CODE_MAP.put(Constant.ACCOUNT_STATUS_INVALID_SHOP, "ACCOUNT_STATUS_INVALID_SHOP");

        RESPONSE_CODE_MAP.put(DONE_ERROR_CODE, "无效店铺,机器状态没有被更新到");
        ERROR_CODE_MAP.put(DONE_ERROR_CODE, "无效店铺,机器状态没有被更新到");

        RESPONSE_CODE_MAP.put(PRODUCT_DELETED_CODE, "商品被删除");
        ERROR_CODE_MAP.put(PRODUCT_DELETED_CODE, "PRODUCT_DELETED_CODE");

        RESPONSE_CODE_MAP.put(HTTP_ERROR_CODE, "非20状态的开头的");
        ERROR_CODE_MAP.put(HTTP_ERROR_CODE, "HTTP_ERROR_CODE");

        RESPONSE_CODE_MAP.put(IP_BLOCKED_CODE, "页面被封禁");
        ERROR_CODE_MAP.put(IP_BLOCKED_CODE, "IP_BLOCKED_CODE");

        RESPONSE_CODE_MAP.put(GOTO_ERROR_CODE, "访问失败 页面没有响应complete");
        ERROR_CODE_MAP.put(GOTO_ERROR_CODE, "GOTO_ERROR_CODE");

        RESPONSE_CODE_MAP.put(GET_HTTP_CHECK_ERROR_CODE, "页面成功但是没有指定元素");
        ERROR_CODE_MAP.put(GET_HTTP_CHECK_ERROR_CODE, "GET_HTTP_CHECK_ERROR_CODE");

        RESPONSE_CODE_MAP.put(SCRIPT_ERROR_CODE, "客户端执行任务失败");
        ERROR_CODE_MAP.put(SCRIPT_ERROR_CODE, "SCRIPT_ERROR_CODE");

        RESPONSE_CODE_MAP.put(ERROR_CODE, "通用报错");
        ERROR_CODE_MAP.put(ERROR_CODE, "ERROR_CODE");

        RESPONSE_CODE_MAP.put(MACHINE_ERROR_CODE, "机器相关的报错");
        ERROR_CODE_MAP.put(MACHINE_ERROR_CODE, "MACHINE_ERROR_CODE");

        RESPONSE_CODE_MAP.put(REGISTERED_ERROR_CODE, "注册相关的报错");
        ERROR_CODE_MAP.put(REGISTERED_ERROR_CODE, "REGISTERED_ERROR_CODE");

        RESPONSE_CODE_MAP.put(API_ERROR_CODE, "api相关的报错");
        ERROR_CODE_MAP.put(API_ERROR_CODE, "API_ERROR_CODE");

        RESPONSE_CODE_MAP.put(NO_REGISTERED_CODE, "账号机没有注册");
        ERROR_CODE_MAP.put(NO_REGISTERED_CODE, "NO_REGISTERED_CODE");

        RESPONSE_CODE_MAP.put(CAN_NOT_REGISTERED_CODE, "账号机不能注册");
        ERROR_CODE_MAP.put(CAN_NOT_REGISTERED_CODE, "CAN_NOT_REGISTERED_CODE");

        RESPONSE_CODE_MAP.put(NO_JOB_CODE, "没有任务");
        ERROR_CODE_MAP.put(NO_JOB_CODE, "NO_JOB_CODE");

        RESPONSE_CODE_MAP.put(NO_FIND_DOM_CODE, "找不到元素");
        ERROR_CODE_MAP.put(NO_FIND_DOM_CODE, "NO_FIND_DOM_CODE");

        RESPONSE_CODE_MAP.put(GOTO_TIMEOUT_CODE, "页面返回超时并没有返回响应码");
        ERROR_CODE_MAP.put(GOTO_TIMEOUT_CODE, "GOTO_TIMEOUT_CODE");

        RESPONSE_CODE_MAP.put(NEED_TO_RESET_PASSWORD, "亚马逊后台需要重置密码");
        ERROR_CODE_MAP.put(NEED_TO_RESET_PASSWORD, "NEED_TO_RESET_PASSWORD");

        RESPONSE_CODE_MAP.put(NOT_AUTHORIZED, "亚马逊后台大洲没有授权");
        ERROR_CODE_MAP.put(NOT_AUTHORIZED, "NOT_AUTHORIZED");

        RESPONSE_CODE_MAP.put(NEED_TO_CHOSE_DEFAULT_SITE, "亚马逊后台大洲没有选择默认站点");
        ERROR_CODE_MAP.put(NEED_TO_CHOSE_DEFAULT_SITE, "NEED_TO_CHOSE_DEFAULT_SITE");

        RESPONSE_CODE_MAP.put(NEED_TO_SET_TWO_STEP_VERIFICATION, "亚马逊后台大洲没有设置二步验证");
        ERROR_CODE_MAP.put(NEED_TO_SET_TWO_STEP_VERIFICATION, "NEED_TO_SET_TWO_STEP_VERIFICATION");


        RESPONSE_CODE_MAP.put(NEED_TO_SET_SINGLE_LOGIN, "亚马逊后台大洲没有设置单点登录");
        ERROR_CODE_MAP.put(NEED_TO_SET_SINGLE_LOGIN, "NEED_TO_SET_SINGLE_LOGIN");

        RESPONSE_CODE_MAP.put(UNKNOWN_LOGIN_ERROR, "亚马逊后台未知登录失败");
        ERROR_CODE_MAP.put(UNKNOWN_LOGIN_ERROR, "UNKNOWN_LOGIN_ERROR");

        RESPONSE_CODE_MAP.put(ACCOUNT_LOCK, "亚马逊账号被锁定");
        ERROR_CODE_MAP.put(ACCOUNT_LOCK, "ACCOUNT_LOCK");

        RESPONSE_CODE_MAP.put(PASSWORD_ERROR, "亚马逊密码错误");
        ERROR_CODE_MAP.put(PASSWORD_ERROR, "PASSWORD_ERROR");

        RESPONSE_CODE_MAP.put(NO_CREDIT_CARD, "亚马逊站点没有绑定信用卡");
        ERROR_CODE_MAP.put(NO_CREDIT_CARD, "NO_CREDIT_CARD");

        RESPONSE_CODE_MAP.put(CAPTCHA_IMAGE_ERROR, "图片验证错误");
        ERROR_CODE_MAP.put(CAPTCHA_IMAGE_ERROR, "CAPTCHA_IMAGE_ERROR");

        RESPONSE_CODE_MAP.put(TWO_STEP_ERROR, "二步验证错误");
        ERROR_CODE_MAP.put(TWO_STEP_ERROR, "TWO_STEP_ERROR");

        RESPONSE_CODE_MAP.put(VERIFY_CODE_ERROR, "二步验证二维码无效");
        ERROR_CODE_MAP.put(VERIFY_CODE_ERROR, "VERIFY_CODE_ERROR");

        RESPONSE_CODE_MAP.put(NO_OPEN_VERIFY, "没有开启二步验证的二维码功能");
        ERROR_CODE_MAP.put(NO_OPEN_VERIFY, "NO_OPEN_VERIFY");

        RESPONSE_CODE_MAP.put(NEED_TO_UPLOAD_QCCODE, "需要上传二维码");
        ERROR_CODE_MAP.put(NEED_TO_UPLOAD_QCCODE, "NEED_TO_UPLOAD_QCCODE");

        RESPONSE_CODE_MAP.put(NOT_SITE, "找不到站点的选项");
        ERROR_CODE_MAP.put(NOT_SITE, "NOT_SITE");

        RESPONSE_CODE_MAP.put(CHOSE_SITE_ERROR, "切换站点失败");
        ERROR_CODE_MAP.put(CHOSE_SITE_ERROR, "CHOSE_SITE_ERROR");

        RESPONSE_CODE_MAP.put(CHOSE_LANG_ERROR, "切换语言失败");
        ERROR_CODE_MAP.put(CHOSE_LANG_ERROR, "CHOSE_LANG_ERROR");

        RESPONSE_CODE_MAP.put(NOT_FULFILLMENT_REPORTS_BUTTON, "切换站点失败");
        ERROR_CODE_MAP.put(NOT_FULFILLMENT_REPORTS_BUTTON, "NOT_FULFILLMENT_REPORTS_BUTTON");

        RESPONSE_CODE_MAP.put(FULFILLMENT_REPORTS_DATE_ERROR, "选择日期失败");
        ERROR_CODE_MAP.put(FULFILLMENT_REPORTS_DATE_ERROR, "FULFILLMENT_REPORTS_DATE_ERROR");

        RESPONSE_CODE_MAP.put(FBA_BOX_NO_IN_WORKING, "fba非工作状态");
        ERROR_CODE_MAP.put(FBA_BOX_NO_IN_WORKING, "FBA_BOX_NO_IN_WORKING");

        RESPONSE_CODE_MAP.put(FBA_BOX_NO_FILE_INPUT, "fba 没有上传按钮");
        ERROR_CODE_MAP.put(FBA_BOX_NO_FILE_INPUT, "FBA_BOX_NO_FILE_INPUT");

        RESPONSE_CODE_MAP.put(FBA_BOX_CLICK_FILE_INPUT_ERROR, "fba 没有上传按钮");
        ERROR_CODE_MAP.put(FBA_BOX_CLICK_FILE_INPUT_ERROR, "FBA_BOX_CLICK_FILE_INPUT_ERROR");

        RESPONSE_CODE_MAP.put(FBA_BOX_NO_DOWNLOAD_BUTTON, "fba 没有下载按钮");
        ERROR_CODE_MAP.put(FBA_BOX_NO_DOWNLOAD_BUTTON, "FBA_BOX_NO_DOWNLOAD_BUTTON");

        RESPONSE_CODE_MAP.put(NEED_TO_DOWNLOAD_EXCEL, "fba 需要先下载Excel");
        ERROR_CODE_MAP.put(NEED_TO_DOWNLOAD_EXCEL, "NEED_TO_DOWNLOAD_EXCEL");

        RESPONSE_CODE_MAP.put(REFUNDS_ORDER_CODE, "退款订单");
        ERROR_CODE_MAP.put(REFUNDS_ORDER_CODE, "REFUNDS_ORDER_CODE");

        RESPONSE_CODE_MAP.put(ACCOUNT_SITE_DEACTIVATED, "店铺站点被禁");
        ERROR_CODE_MAP.put(ACCOUNT_SITE_DEACTIVATED, "ACCOUNT_SITE_DEACTIVATED");

        RESPONSE_CODE_MAP.put(ACCOUNT_HOLIDAY, "店铺假日模式");
        ERROR_CODE_MAP.put(ACCOUNT_HOLIDAY, "ACCOUNT_HOLIDAY");

        RESPONSE_CODE_MAP.put(EMPTY_BANK_CARD, "没有银行卡信息");
        ERROR_CODE_MAP.put(EMPTY_BANK_CARD, "EMPTY_BANK_CARD");

        RESPONSE_CODE_MAP.put(QC_CODE_ERROR, "获取后台二步验证二维码失败");
        ERROR_CODE_MAP.put(QC_CODE_ERROR, "QC_CODE_ERROR");

        RESPONSE_CODE_MAP.put(REGISTER_PARAM_ERROR_CODE, "注册报错: unique_id 传入参数是空");
        ERROR_CODE_MAP.put(REGISTER_PARAM_ERROR_CODE, "REGISTER_PARAM_ERROR_CODE");

        RESPONSE_CODE_MAP.put(REGISTER_DB_EMPTY_ERROR_CODE, "注册报错: db is empty 数据库没有注册的IP");
        ERROR_CODE_MAP.put(REGISTER_DB_EMPTY_ERROR_CODE, "REGISTER_DB_EMPTY_ERROR_CODE");

        RESPONSE_CODE_MAP.put(CLIENT_NO_TOKEN_ERROR_CODE, "客户端请求头没有携带token");
        ERROR_CODE_MAP.put(CLIENT_NO_TOKEN_ERROR_CODE, "CLIENT_NO_TOKEN_ERROR_CODE");

        RESPONSE_CODE_MAP.put(CLIENT_UNAUTHORIZED_TOKEN_ERROR_CODE, "客户端token验证失败");
        ERROR_CODE_MAP.put(CLIENT_UNAUTHORIZED_TOKEN_ERROR_CODE, "CLIENT_UNAUTHORIZED_TOKEN_ERROR_CODE");

        RESPONSE_CODE_MAP.put(BUSINESS_UNAUTHORIZED_TOKEN_ERROR_CODE, "业务端端token验证失败");
        ERROR_CODE_MAP.put(BUSINESS_UNAUTHORIZED_TOKEN_ERROR_CODE, "BUSINESS_UNAUTHORIZED_TOKEN_ERROR_CODE");

        RESPONSE_CODE_MAP.put(UNkOWN_ERROR, "未知错误");
        RESPONSE_CODE_MAP.put(NO_MACHINE_ERROR, "账号大洲没有可用机器");
        ERROR_CODE_MAP.put(UNkOWN_ERROR, "UNKNOWN_ERROR_CODE");
    }
}
