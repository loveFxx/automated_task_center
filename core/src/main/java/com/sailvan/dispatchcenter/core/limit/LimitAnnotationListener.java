package com.sailvan.dispatchcenter.core.limit;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 *  检测limit注解的取值是否正确
 * @author mh
 * @date 2021-07
 */
@Component
public class LimitAnnotationListener implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    final static String[] limitTypes = {"taskNameLimit","businessInterfaceLimit","businessLimitMonth"};

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());

        String businessControllerBeanName = "businessController";
        if (businessControllerBeanName.equals(beanName) && methods != null) {
            List<String> limitTypeLists = Arrays.asList(limitTypes);
            for (Method method : methods) {
                Limit limit = AnnotationUtils.findAnnotation(method, Limit.class);
                if (limit != null ) {
                    if(StringUtils.isEmpty(limit.key()) || limit.key().equals("business")){
                        String[] types = limit.types();
                        for (String type : types) {
                            if (!StringUtils.isEmpty(type) && !limitTypeLists.contains(type)) {
                                throw new RuntimeException("Annotation limit types is not support,method="+method.getName()+" type="+type);
                            }else if(StringUtils.isEmpty(type)){
                                throw new RuntimeException("Annotation limit types is not support null or \"\" method="+method.getName());
                            }
                        }
                    }
                }
            }
        }
        return bean;
    }
}
