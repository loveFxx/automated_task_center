package com.sailvan.dispatchcenter.common.pipe;


import com.sailvan.dispatchcenter.common.domain.TokenUser;

/**
 * @author menghui
 * @date 21-04
 */
public interface UserTokenService {


    public TokenUser checkTokenUser(TokenUser tokenUser);


    /**
     * 创建Token，这里要根据当前时间获取密钥，并且生成Token,更新用户的最后登入时间
     * @param user
     * @return Transactional
     */
    public String createWebToken(TokenUser user) ;


}
