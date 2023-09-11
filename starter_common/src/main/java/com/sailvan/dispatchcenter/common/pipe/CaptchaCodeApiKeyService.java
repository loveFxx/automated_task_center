package com.sailvan.dispatchcenter.common.pipe;


import com.sailvan.dispatchcenter.common.domain.CaptchaCodeApiKey;

/**
 * 图片apikey
 * @author mh
 * @date 2021-8
 */
public interface CaptchaCodeApiKeyService {

    /**
     *
     * @return
     */
    public CaptchaCodeApiKey getCaptchaCodeApiKey();

    public int update(CaptchaCodeApiKey captchaCodeApiKey) ;

}
