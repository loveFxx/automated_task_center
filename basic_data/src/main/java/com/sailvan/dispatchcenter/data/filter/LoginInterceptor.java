package com.sailvan.dispatchcenter.data.filter;

import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.common.domain.LoginDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *  为了拦截没有登录的请求及登录超时重新登陆
 *  @date 2021-04
 *  @author menghui
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取当前session
        String uri = request.getRequestURI();
//        System.out.println("LoginInterceptor:"+uri);
        HttpSession session = request.getSession();
        // 根据session获取登录用户
        LoginDTO ui = (LoginDTO) session.getAttribute(Constant.SYSTEM_USER_SESSION);
        // 没登录或登录失效，重定向到登录页面
        if (null == ui) {
            logger.info("没登录或登录失效，重定向到登录页面");
            response.sendRedirect(request.getContextPath() + "/");
            return false;
        }
        // 已经登录
        return true;
    }
}
