package com.sailvan.dispatchcenter.data.plugs;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


/**
 * 是否初始化缓存
 * @date 2022-03
 * @author menghui
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(InitCacheMarkerConfiguration.class)
public @interface EnableInitCache {
}
