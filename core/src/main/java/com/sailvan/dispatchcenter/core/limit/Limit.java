package com.sailvan.dispatchcenter.core.limit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author mh
 * @date 20-09-08
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Limit {

    /**
     *  限制的类型 只能取值: {"taskNameLimit","businessInterfaceLimit","businessLimitMonth"};
     *
     * @return String
     */
    String[] types();

    /**
     * 资源的key 业务端的默认是空 客户端的是 client
     *
     * @return String
     */
    String key() default "";

    /**
     * Key的prefix
     *
     * @return String
     */
    String prefix() default "";

    /**
     * 给定的时间段
     * 单位秒
     *
     * @return int
     */
    int period() default 0;

    /**
     * 最多的访问限制次数
     *
     * @return int
     */
    int count() default 0;

    /**
     * 月总次数
     *
     * @return int
     */
    int totalTimesMonths() default 0;

    /**
     * 类型
     *
     * @return LimitType
     */
    LimitType limitType() default LimitType.CUSTOMER;
}
