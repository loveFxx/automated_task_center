package com.sailvan.dispatchcenter.core.monitor;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Monitor {

    /**
     *  redis key;
     *
     * @return String
     */
    String type();
}
