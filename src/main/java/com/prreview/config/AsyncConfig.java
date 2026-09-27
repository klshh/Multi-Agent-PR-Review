package com.prreview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Enables @Async support and defines the thread pool that runs our agents
 * concurrently.
 *
 * Without a custom executor, Spring's default @Async thread pool is
 * SimpleAsyncTaskExecutor, which creates a brand-new thread for every call
 * and never reuses them - fine for a quick demo, wasteful and unbounded
 * under real load. Defining our own pool here gives us control over how
 * many agent calls can run in parallel.
 */
@Configuration
@EnableAsync(proxyTargetClass = true)
public class AsyncConfig {

    @Bean(name = "agentTaskExecutor")
    public Executor agentTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size: how many threads are kept alive even when idle.
        // We currently have 4 agents (quality/security/tests/docs) that run
        // in parallel per review, so 4 lets all of them fire at once without
        // queuing.
        executor.setCorePoolSize(4);

        // Max pool size: ceiling if more tasks pile up than corePoolSize
        // can handle at once (e.g. two PRs reviewed at almost the same time).
        executor.setMaxPoolSize(8);

        // Queue capacity: how many pending tasks can wait for a free thread
        // before new tasks start getting rejected instead of queued.
        executor.setQueueCapacity(50);

        // Thread naming - makes it much easier to read logs and know which
        // thread handled which agent call.
        executor.setThreadNamePrefix("agent-exec-");

        executor.initialize();
        return executor;
    }

    /**
     * Runs whole PR reviews triggered by the GitHub webhook, so the webhook
     * can answer GitHub immediately instead of waiting for every agent.
     *
     * Kept separate from agentTaskExecutor on purpose: a review thread blocks
     * while waiting for its agents, so if reviews and agents shared one pool,
     * a few concurrent reviews could fill every thread and leave the agents
     * they're waiting on stuck in the queue forever.
     */
    @Bean(name = "reviewTaskExecutor")
    public Executor reviewTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("review-exec-");
        executor.initialize();
        return executor;
    }
}