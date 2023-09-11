package com.sailvan.dispatchcenter.common.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * 异步线程池
 * @author mh
 * @date 2021-8
 */
@Configuration
@EnableAsync
public class AsyncThreadPoolConfig {
    private static final int CORE_POOL_SIZE = 10;

    private static final int MAX_POOL_SIZE = 200;

    private static final int QUEUE_CAPACITY = Integer.MAX_VALUE;

    public static final String BEAN_EXECUTOR = "async_bean_executor";
    public static final String BEAN_EXECUTOR_TASK = "async_bean_executor_task";

    /**
     * 事件和情感接口线程池执行器配置
     * @return 事件和情感接口线程池执行器bean
     *
     */
    @Bean(BEAN_EXECUTOR)
    public Executor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        // 设置队列容量
        executor.setQueueCapacity(QUEUE_CAPACITY);
        // 设置线程活跃时间(秒)
        executor.setKeepAliveSeconds(60);

        executor.setThreadNamePrefix("Async-Executor-Pool#Task");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(100);
        executor.initialize();
        return executor;
    }


    @Bean(BEAN_EXECUTOR_TASK)
    public Executor executorTask() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(0);
        executor.setMaxPoolSize(4);
        // 设置队列容量
        executor.setQueueCapacity(50);
        // 设置线程活跃时间(秒)
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("Async-Executor-Pool#Task");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(100);
        executor.initialize();
        return executor;
    }
}
