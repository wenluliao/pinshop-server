package com.flashbuy.infrastructure.config;

import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Java 25 Virtual Threads Configuration
 * Replaces traditional thread pool with virtual thread executor
 */
@Configuration
@EnableAsync
public class VirtualThreadConfig implements AsyncConfigurer {

    /**
     * Configure virtual thread executor for async operations
     * This enables Spring MVC to handle massive concurrent requests with minimal resources
     */
    @Bean(name = TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor asyncTaskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setThreadFactory(Thread.ofVirtual().factory());
        executor.initialize();
        return executor;
    }

    /**
     * Virtual thread factory for structured concurrency
     * Usage example:
     * try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
     *     var task1 = scope.fork(() -> service.call1());
     *     var task2 = scope.fork(() -> service.call2());
     *     scope.join().throwIfFailed();
     * }
     */
    @Bean
    public ExecutorService virtualThreadFactory() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
