package com.sailvan.dispatchcenter.data.config;

import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.data.filter.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 登录过滤配置,为需要登录的接口进行过滤
 * @date 2021-04
 * @author menghui
 */
@Configuration
public class WebLoginConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加拦截器，配置拦截地址
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns(Constant.FILTERED_LOGIN_MAPPING)
                .excludePathPatterns(Constant.NOT_FILTERED_LOGIN_MAPPING);
    }

}
