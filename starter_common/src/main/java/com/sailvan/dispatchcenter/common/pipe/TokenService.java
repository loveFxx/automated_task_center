package com.sailvan.dispatchcenter.common.pipe;


public interface TokenService {


    /**
     * 请求token系统拉取token信息
     * @param code 回调返回的code
     * @return token信息
     */
    public String requestToken(String code);

    /**
     * 将token信息放入缓存
     * @param data
     */
    public void setCache(String data);

    /**
     * 获取token系统的access token
     * @return access token
     */
    public String getAccessToken();
    /**
     * 刷新token系统的token信息
     * @param refreshToken refresh token
     * @return 刷新后token信息
     */
    public String refreshAccessToken(Object refreshToken);


    public String getTokenInfo(String account, String platform, String devName);

    /**
     * 获取店铺信息详情
     * @param account 账号
     * @param platform 平台
     * @param area 地区
     * @param site 站点
     * @return 店铺信息
     */
    public String getAccountDetail(String account, String platform, String area, String site);
}
