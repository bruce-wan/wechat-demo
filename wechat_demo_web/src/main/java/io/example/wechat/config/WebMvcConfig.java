package io.example.wechat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Created by bruce.wan on 2021/3/11.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(10);
        threadPoolTaskExecutor.setMaxPoolSize(50);
        threadPoolTaskExecutor.setQueueCapacity(20);
        threadPoolTaskExecutor.setKeepAliveSeconds(120);
        return threadPoolTaskExecutor;
    }

    @Bean
    public TimeoutCallableProcessingInterceptor timeoutInterceptor()
    {
        return new TimeoutCallableProcessingInterceptor();
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer)
    {
        configurer.setDefaultTimeout(60 * 1000);
        configurer.registerCallableInterceptors(timeoutInterceptor());
        configurer.setTaskExecutor(taskExecutor());
    }
}
