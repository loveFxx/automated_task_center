package com.sailvan.dispatchcenter.core.config;

import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.core.filter.UserSecurityHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * token过滤配置，只针对业务端和客户端接口过滤
 * @date 2021-04
 * @author menghui
 */
@Configuration
public class WebTokenConfig implements WebMvcConfigurer {

    @Autowired
    private UserSecurityHandlerInterceptor userSecurityHandlerInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加拦截器，配置拦截地址
        registry.addInterceptor(userSecurityHandlerInterceptor)
                // 只对 业务端和客户端 拦截
                .addPathPatterns(Constant.FILTERED_TOKEN_MAPPING)
                ;//"/","/testLogin"
    }

}
