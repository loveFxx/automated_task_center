package com.sailvan.dispatchcenter.db.dao.automated;

import com.sailvan.dispatchcenter.common.domain.CaptchaCodeApiKey;
import org.apache.ibatis.annotations.Mapper;


/**
 * @author mh
 * @date 21-08
 */
@Mapper
public interface CaptchaCodeApiKeyDao {

    /**
     * 搜索所有
     * @return
     */
    CaptchaCodeApiKey getCaptchaCodeApiKey();

    /**
     * 修改编码
     * @param captchaCodeApiKey
     * @return
     */
    int updateCaptchaCodeApiKey(CaptchaCodeApiKey captchaCodeApiKey);

}
