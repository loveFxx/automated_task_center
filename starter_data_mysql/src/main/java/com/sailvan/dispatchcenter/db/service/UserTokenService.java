package com.sailvan.dispatchcenter.db.service;

import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.TokenUser;
import com.sailvan.dispatchcenter.db.dao.automated.TokenUserDao;
import com.sailvan.dispatchcenter.common.util.WebTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author menghui
 * @date 21-04
 */
@Service
public class UserTokenService implements com.sailvan.dispatchcenter.common.pipe.UserTokenService {

    @Autowired
    TokenUserDao tokenUserDao;

    @Override
    public TokenUser checkTokenUser(TokenUser tokenUser) {
        return tokenUserDao.checkTokenUser(tokenUser);
    }


    /**
     * 创建Token，这里要根据当前时间获取密钥，并且生成Token,更新用户的最后登入时间
     * @param user
     * @return Transactional
     */
    @Override
    public String createWebToken(TokenUser user) {
        Instant now = Instant.now();
        String secretKey = WebTokenUtil.genSecretKey(now);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String token = WebTokenUtil.create(secretKey, String.valueOf(user.getId()), now, Constant.TOKEN_VALIDITY_TIME);
        user.setLastLogin(df.format(LocalDateTime.ofInstant(now, ZoneId.of("+08:00"))));
        tokenUserDao.updateByPrimaryKey(user);
        return token;
    }


}
