package com.example.demo.threads;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadPool {

    private final int MAX_THREADS = 10;
    private WorkerThreadPool[] workers;
    private final BlockingQueue<Callable<Void>> taskQueue;
    private volatile boolean isShutdown = false;
    private final Object shutdownLock = new Object();

    public ThreadPool() {
        this.taskQueue = new LinkedBlockingDeque<>();
        this.workers = new WorkerThreadPool[MAX_THREADS];
        for (int i = 0; i < MAX_THREADS; i++) {
            workers[i] = new WorkerThreadPool(taskQueue, this);
            workers[i].start();
        }
        log.info("ThreadPool initialized with {} worker threads", MAX_THREADS);
    }

    public void taskFailed(Exception e) {
        if (this.isShutdown)
            return;
        shutDown();
        if (e instanceof InterruptedException) {
            log.error("Thread pool interrupted: {}", e.getMessage());
        } else {
            log.error("Task failed after 3 retries: {}", e.getMessage());
        }

    }

    public void submitTask(Callable<Void> task) throws InterruptedException {
        synchronized (this.shutdownLock) {
            if (isShutdown) {
                throw new IllegalStateException("ThreadPool is shutdown. Cannot accept new tasks.");
            }
            log.debug("Submitting task to queue: {}", task);
            taskQueue.put(task);
            log.info("Task submitted successfully: {}", task);
        }
    }

    public void shutDown() {
        synchronized (this.shutdownLock) {
            if (this.isShutdown)
                return;
            this.isShutdown = true;
            for (WorkerThreadPool worker : workers) {
                worker.shutdown();
            }
        }

    }

}
