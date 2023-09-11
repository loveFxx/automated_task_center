package com.sailvan.dispatchcenter.es.config;

import org.springframework.context.annotation.Bean;


/**
 * es标记配置了
 * @date 2022-03
 * @author menghui
 */
public class EsMarkerConfiguration {
    @Bean("esMarker")
    public EsMarker esMarkerBean() {
        return new EsMarker();
    }
    public class EsMarker {
    }
}
