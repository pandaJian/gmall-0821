package com.atguigu.gmall.cart.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;

/**
 * @author : panda Jian
 * @date : 2021-02-23 23:56
 * Description
 */
@Configuration
public class AsyncConfig implements AsyncConfigurer {
    @Autowired
    private AsyncUncaughtExceptionHandler exceptionHandler;

    //配置线程池，约束线程数
    @Override
    public Executor getAsyncExecutor() {
        return null;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return exceptionHandler;
    }
}
