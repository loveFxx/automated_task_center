package com.sailvan.dispatchcenter.core.config;

import com.sailvan.dispatchcenter.common.constant.Constant;
import com.sailvan.dispatchcenter.core.filter.HttpServletGzipFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class HttpServletFilterConfig {
    /**
     * 注册 HttpServletFilter
     *
     * @return
     */
    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new HttpServletGzipFilter());

        List<String> urlPatterns = new ArrayList<>();
        urlPatterns.add(Constant.CLIENT_PREFIX +"/*");
        registrationBean.setUrlPatterns(urlPatterns);

        return registrationBean;
    }
}
