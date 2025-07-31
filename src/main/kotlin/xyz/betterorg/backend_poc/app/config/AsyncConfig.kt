package xyz.betterorg.backend_poc.app.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig : AsyncConfigurer {

    /**
     * Defines and customizes the Executor used by @Async methods.
     */
    override fun getAsyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()

        // Core pool size: The minimum number of threads to keep in the pool, even if idle.
        // Good starting point: number of CPU cores or slightly more for IO-bound tasks.
        executor.corePoolSize = 2

        // Max pool size: The maximum number of threads that the pool can create.
        // If core threads are busy and the queue is full, new threads are created up to this limit.
        // Set based on available RAM and CPU. Too many threads can lead to high context switching.
        executor.maxPoolSize = 5

        // Queue capacity: The capacity for the LinkedBlockingQueue.
        // Tasks are queued here when all core threads are busy. New threads (up to maxPoolSize)
        // are only created AFTER this queue is full. An unbounded queue (default) can cause OOM.
        executor.queueCapacity = 50

        // Sets the prefix for the names of the threads created by this executor.
        // Useful for monitoring and debugging.
        executor.setThreadNamePrefix("GmailSync-")

        // Crucial for graceful shutdown:
        // When true, the executor will wait for currently running tasks to complete
        // during application shutdown before shutting down the executor itself.
        executor.setWaitForTasksToCompleteOnShutdown(true)

        // The maximum time (in seconds) to wait for tasks to complete during shutdown.
        // If tasks don't finish within this time, they will be forcibly interrupted.
        // Adjust based on how long your longest async tasks might reasonably take.
        executor.setAwaitTerminationSeconds(60) // e.g., 60 seconds

        // Initializes the executor. This must be called after setting all properties.
        executor.initialize()
        return executor
    }
}