package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.domain.CaptchaCodeApiKey;
import com.sailvan.dispatchcenter.db.dao.automated.CaptchaCodeApiKeyDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * 图片apikey
 * @author mh
 * @date 2021-8
 */
@Service
public class CaptchaCodeApiKeyService implements com.sailvan.dispatchcenter.common.pipe.CaptchaCodeApiKeyService {


    @Resource
    private CaptchaCodeApiKeyDao captchaCodeApiKeyDao;

    /**
     *
     * @return
     */
    @Override
    public CaptchaCodeApiKey getCaptchaCodeApiKey(){
        return captchaCodeApiKeyDao.getCaptchaCodeApiKey();
    }

    @Override
    public int update(CaptchaCodeApiKey captchaCodeApiKey) {
        return captchaCodeApiKeyDao.updateCaptchaCodeApiKey(captchaCodeApiKey);
    }
}
