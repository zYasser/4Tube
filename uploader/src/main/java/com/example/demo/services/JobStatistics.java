package com.example.demo.services;

/**
 * Statistics for upload jobs
 */
public class JobStatistics {
    private final long pending;
    private final long processing;
    private final long completed;
    private final long failed;
    private final long retry;
    private final long total;

    public JobStatistics(long pending, long processing, long completed, long failed, long retry, long total) {
        this.pending = pending;
        this.processing = processing;
        this.completed = completed;
        this.failed = failed;
        this.retry = retry;
        this.total = total;
    }

    public long getPending() { return pending; }
    public long getProcessing() { return processing; }
    public long getCompleted() { return completed; }
    public long getFailed() { return failed; }
    public long getRetry() { return retry; }
    public long getTotal() { return total; }
}








