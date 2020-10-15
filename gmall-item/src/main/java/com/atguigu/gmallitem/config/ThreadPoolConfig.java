package com.atguigu.gmallitem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {
    @Bean
    public ThreadPoolExecutor getThreadPoolExecute(@Value("${corePoolSize}")int corePoolSize,@Value("${maximumPoolSize}")int maximumPoolSize){
        return new ThreadPoolExecutor(corePoolSize,maximumPoolSize,60, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(1024), Executors.defaultThreadFactory());
    }
}
