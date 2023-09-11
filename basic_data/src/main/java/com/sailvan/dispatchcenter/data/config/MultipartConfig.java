package com.sailvan.dispatchcenter.data.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.MultipartConfigElement;

/**
 * 上传文件大小配置
 * @author mh
 * @date 2021-07
 */
@Configuration
public class MultipartConfig {
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //文件最大 KB,MB
        factory.setMaxFileSize("1000MB");
        /// 设置总上传数据总大小
        factory.setMaxRequestSize("2000MB");
        return factory.createMultipartConfig();
    }
}
