package com.cmdwrapper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async execution configuration.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "commandExecutor")
    public Executor commandExecutor(CommandWrapperProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getMaxConcurrentCommands());
        executor.setMaxPoolSize(properties.getMaxConcurrentCommands() * 2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("cmd-exec-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
