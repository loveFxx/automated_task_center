package com.sailvan.dispatchcenter.db.util;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.BusinessSystem;
import com.sailvan.dispatchcenter.common.util.DateUtils;
import com.sailvan.dispatchcenter.common.util.RedisUtils;
import com.sailvan.dispatchcenter.common.util.WebTokenUtil;
import com.sailvan.dispatchcenter.db.dao.automated.BusinessSystemDao;
import com.sailvan.dispatchcenter.db.dao.automated.TokenUserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;

/**
 * 验证业务端携带的token
 * @author mh
 * @date 2021-05
 */
@Service
public class UserSecurityUtil {
    private static final Logger logger = LoggerFactory.getLogger(UserSecurityUtil.class);

    @Autowired
    TokenUserDao tokenUserDao;

    @Autowired
    BusinessSystemDao businessSystemDao;

    @Autowired
    RedisUtils redisUtils;
    /**
     * 验证请求中的token
     * @return
     */
    public boolean verifyWebToken(HttpServletRequest req, HttpServletResponse resp) {
        String token = req.getHeader("Authorization");
        if (StringUtils.isEmpty(token)) {
            return false;
        }
        DecodedJWT jwtToken = WebTokenUtil.decode(token);
        if (jwtToken == null) {
            return false;
        }
        //从JWT里取出存放在payload段里的userid，查询这个用户信息得到用户最后登录时间
        BusinessSystem businessSystem = businessSystemDao.getBusinessById(jwtToken.getSubject());
        req.setAttribute(Constant.AUTHORIZATION_BUSINESS_NAME,businessSystem.getSystemName());
        String lastLogin = businessSystem.getLastLogin();
        //根据用户登录时间，拿到用户申请Token时的secretKey
        String secretKey = "";
        try {
            Instant time = WebTokenUtil.StringToDateToInstant(lastLogin);
            secretKey = WebTokenUtil.genSecretKey(time);
            //校验
            WebTokenUtil.verify(secretKey, token);
        } catch (SignatureVerificationException e) {
            logger.error(e.getMessage());
            return false;
        } catch (TokenExpiredException e) {
            String redisKey = Constant.BUSINESS_REGISTER_PREFIX+businessSystem.getSystemName();
            // 用来重置token
            synchronized (businessSystem.getSystemName().intern()){
                boolean isRefreshToken = false;
                Object o = redisUtils.get(redisKey);
                if(o == null){
                    // 缓存为空
                    isRefreshToken = true;
                }else {
                    try {
                        WebTokenUtil.verify(secretKey, String.valueOf(o));
                    } catch (Exception ex) {
                        //验证失败 这里应该只是失效
                        isRefreshToken = true;
                    }
                }
                if(isRefreshToken){
                    // 允许一段时间有效时间同时返回新的token
                    String newToken = WebTokenUtil.getRefreshToken(secretKey, jwtToken);
                    if (StringUtils.isEmpty(newToken)) {
                        logger.error(e.getMessage());
                        return false;
                    }

                    // 把新的token更新到redis中,与注册的token一致
                    String redisValidityKey = Constant.BUSINESS_REGISTER_VALID_PREFIX+businessSystem.getSystemName();
                    redisUtils.put(redisKey,newToken, Long.valueOf(Constant.TOKEN_VALIDITY_TIME * 60));
                    redisUtils.put(redisValidityKey, DateUtils.getTokenValidityTime(), Long.valueOf(Constant.TOKEN_VALIDITY_TIME * 60));
                    logger.debug("Subject : [" + jwtToken.getSubject() + "] token expired, allow get refresh token [" + newToken + "]");

                    // todo 业务端要从这里获取新的token
                    resp.setHeader("Set-RefreshToken", newToken);
                }else {
                    resp.setHeader("Set-RefreshToken", String.valueOf(o));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
