package com.jmunoz.trip_advisor.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnThreading;
import org.springframework.boot.thread.Threading;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorServiceConfig {

    // IMPORTANTE: Solo queremos crear este bean cuando la property spring.threads.virtual.enabled es true.
    // Si vale false usaremos platformThreadExecutor.
    // Esto lo conseguimos con la anotación @ConditionalOnThreading()
    @Bean
    @ConditionalOnThreading(Threading.VIRTUAL)
    public ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    // IMPORTANTE: Solo queremos crear este bean cuando la property spring.threads.virtual.enabled es false.
    // Si vale true usaremos virtualThreadExecutor.
    // Esto lo conseguimos con la anotación @ConditionalOnThreading()
    @Bean
    @ConditionalOnThreading(Threading.PLATFORM)
    public ExecutorService platformThreadExecutor() {
        // Para que cree los threads que necesite.
        return Executors.newCachedThreadPool();
    }
}
