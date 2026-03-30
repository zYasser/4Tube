package com.example.demo.threads;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class WorkerThreadPool extends Thread {

    private ThreadPool parent;
    private BlockingQueue<Callable<Void>> taskQueue;
    private volatile boolean isRunning = true;

    public WorkerThreadPool(BlockingQueue<Callable<Void>> taskQueue, ThreadPool parent) {

        this.taskQueue = taskQueue;
        this.parent = parent;
        log.debug("Worker thread created: {}", Thread.currentThread().getName());

    }

    public void run() {
        while (isRunning) {
            int retryCount = 0;
            Callable<Void> task;
            try {
                log.debug("Worker {} waiting for task from queue", Thread.currentThread().getName());
                task = taskQueue.take();
                log.info("Worker {} picked up task: {}", Thread.currentThread().getName(), task);
            } catch (InterruptedException e) {
                if (isRunning) {

                    shutdown();
                }
                parent.taskFailed(e);
                break;

            }
            while (retryCount < 3) {
                try {
                    log.debug("Worker {} executing task (attempt {}/3)", Thread.currentThread().getName(), retryCount + 1);
                    task.call();
                    log.info("Worker {} completed task successfully", Thread.currentThread().getName());
                    break; // Task succeeded, exit retry loop
                } catch (Exception e) {
                    log.warn("Worker {} task failed on attempt {}/3: {}. Retrying...", 
                            Thread.currentThread().getName(), retryCount + 1, e.getMessage());
                    retryCount++;
                    if (retryCount >= 3) {
                        log.error("Worker {} task failed after 3 retries: {}", 
                                Thread.currentThread().getName(), e.getMessage());
                        parent.taskFailed(e);

                    }
                }
            }
        }
    }

    public void shutdown() {
        log.info("Shutting down worker thread: {}", Thread.currentThread().getName());
        isRunning = false;
        this.interrupt();
    }

}
