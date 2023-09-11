package com.sailvan.dispatchcenter.es.plugs;

import com.sailvan.dispatchcenter.es.config.EsMarkerConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


/**
 * 是否开启es，在其他端如 basic_data启用
 * @date 2022-03
 * @author menghui
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EsMarkerConfiguration.class)
public @interface  EnableEsServer {
}
