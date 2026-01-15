package com.example.demo.services;

import java.util.List;
import java.util.Optional;

import com.example.demo.entity.JobStatus;
import com.example.demo.entity.UploadJob;

public interface UploadJobService {

    /**
     * Create a new upload job
     * @param fileId the file ID associated with this upload job
     * @return the created UploadJob
     */
    UploadJob createUploadJob(Long fileId);

    /**
     * Find upload job by ID
     * @param id the job ID
     * @return Optional containing UploadJob if found
     */
    Optional<UploadJob> findById(Long id);

    /**
     * Find all upload jobs
     * @return List of all UploadJob
     */
    List<UploadJob> findAll();

    /**
     * Find upload jobs by status
     * @param status the status to search for
     * @return List of UploadJob with the given status
     */
    List<UploadJob> findByStatus(JobStatus status);

    /**
     * Find upload jobs by status ordered by creation date descending
     * @param status the status to search for
     * @return List of UploadJob ordered by creation date
     */
    List<UploadJob> findByStatusOrderByCreatedAtDesc(JobStatus status);

    /**
     * Start an upload job
     * @param jobId the job ID to start
     * @return the updated UploadJob
     */
    UploadJob startJob(Long jobId);

    /**
     * Complete an upload job
     * @param jobId the job ID to complete
     * @return the updated UploadJob
     */
    UploadJob completeJob(Long jobId);

    /**
     * Fail an upload job
     * @param jobId the job ID to fail
     * @param reason the failure reason
     * @return the updated UploadJob
     */
    UploadJob failJob(Long jobId, String reason);

    /**
     * Retry an upload job
     * @param jobId the job ID to retry
     * @return the updated UploadJob
     */
    UploadJob retryJob(Long jobId);

    /**
     * Check if a job can be retried
     * @param job the upload job to check
     * @return true if the job can be retried, false otherwise
     */
    boolean canRetry(UploadJob job);

    /**
     * Get jobs that are eligible for retry
     * @return List of UploadJob that can be retried
     */
    List<UploadJob> getRetryableJobs();

    /**
     * Delete an upload job
     * @param jobId the job ID to delete
     */
    void deleteJob(Long jobId);

    /**
     * Get job statistics
     * @return a summary of job counts by status
     */
    JobStatistics getJobStatistics();
}
