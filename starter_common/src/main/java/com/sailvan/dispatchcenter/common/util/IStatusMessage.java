package com.sailvan.dispatchcenter.common.util;

/**
 * 响应状态信息
 * @date 2021-04
 * @author menghui
 */
public interface IStatusMessage {

    /**
     *  获取code
     * @return
     */
    String getCode();

    /**
     * 获取message
     * @return
     */
    String getMessage();

    /**
     *
     */
    public enum SystemStatus implements IStatusMessage{

        //请求成功
        SUCCESS("200","SUCCESS"),
        //请求失败
        ERROR("404","ERROR"),
        //请求参数有误
        PARAM_ERROR("1002","PARAM_ERROR"),
        //表示成功匹配
        SUCCESS_MATCH("1003","SUCCESS_MATCH"),
        //未登录
        NO_LOGIN("1100","NO_LOGIN"),
        //多用户在线（踢出用户）
        MANY_LOGINS("1101","MANY_LOGINS"),
        //用户信息或权限已更新（退出重新登录）
        UPDATE("1102","UPDATE"),
        //用户已锁定
        LOCK("1111","LOCK");
        private String code;
        private String message;

        private SystemStatus(String code,String message){
            this.code = code;
            this.message = message;
        }

        @Override
        public String getCode(){
            return this.code;
        }

        @Override
        public String getMessage(){
            return this.message;
        }
    }

}
