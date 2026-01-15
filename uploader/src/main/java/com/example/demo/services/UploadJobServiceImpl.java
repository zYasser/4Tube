package com.example.demo.services;

import com.example.demo.exceptions.HttpException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.JobStatus;
import com.example.demo.entity.UploadJob;
import com.example.demo.repository.UploadJobRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UploadJobServiceImpl implements UploadJobService {

    private final UploadJobRepository uploadJobRepository;

    // Default retry configuration
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final int DEFAULT_RETRY_DELAY = 5;
    private static final String DEFAULT_RETRY_DELAY_UNIT = "MINUTES";

    public UploadJobServiceImpl(UploadJobRepository uploadJobRepository) {
        this.uploadJobRepository = uploadJobRepository;
    }

    @Override
    public UploadJob createUploadJob(Long fileId) {
        UploadJob job = new UploadJob();
        job.setFileId(fileId);
        job.setStatus(JobStatus.PENDING);
        job.setCreatedAt(LocalDateTime.now());
        job.setRetries(0);
        job.setMaxRetries(DEFAULT_MAX_RETRIES);
        job.setRetryDelay(DEFAULT_RETRY_DELAY);
        job.setRetryDelayUnit(DEFAULT_RETRY_DELAY_UNIT);

        return uploadJobRepository.save(job);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UploadJob> findById(Long id) {
        return uploadJobRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UploadJob> findAll() {
        return uploadJobRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UploadJob> findByStatus(JobStatus status) {
        return uploadJobRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UploadJob> findByStatusOrderByCreatedAtDesc(JobStatus status) {
        return uploadJobRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    public UploadJob startJob(Long jobId) {
        UploadJob job = uploadJobRepository.findById(jobId)
                .orElseThrow(() -> new HttpException(
                        HttpStatus.NOT_FOUND,
                        "Upload job not found",
                        "JOB_NOT_FOUND"
                ));

        if (job.getStatus() != JobStatus.PENDING && job.getStatus() != JobStatus.RETRY) {
            throw new HttpException(
                    HttpStatus.BAD_REQUEST,
                    "Job cannot be started from current status: " + job.getStatus(),
                    "INVALID_JOB_STATUS"
            );
        }

        job.setStatus(JobStatus.PROCESSING);
        return uploadJobRepository.save(job);
    }

    @Override
    public UploadJob completeJob(Long jobId) {
        UploadJob job = uploadJobRepository.findById(jobId)
                .orElseThrow(() -> new HttpException(
                        HttpStatus.NOT_FOUND,
                        "Upload job not found",
                        "JOB_NOT_FOUND"
                ));

        if (job.getStatus() != JobStatus.PROCESSING) {
            throw new HttpException(
                    HttpStatus.BAD_REQUEST,
                    "Only processing jobs can be completed",
                    "INVALID_JOB_STATUS"
            );
        }

        job.setStatus(JobStatus.COMPLETED);
        job.setCompletedAt(LocalDateTime.now());
        return uploadJobRepository.save(job);
    }

    @Override
    public UploadJob failJob(Long jobId, String reason) {
        UploadJob job = uploadJobRepository.findById(jobId)
                .orElseThrow(() -> new HttpException(
                        HttpStatus.NOT_FOUND,
                        "Upload job not found",
                        "JOB_NOT_FOUND"
                ));

        if (job.getStatus() != JobStatus.PROCESSING) {
            throw new HttpException(
                    HttpStatus.BAD_REQUEST,
                    "Only processing jobs can be failed",
                    "INVALID_JOB_STATUS"
            );
        }

        job.setStatus(JobStatus.FAILED);
        job.setFailedAt(LocalDateTime.now());
        job.setFailedReason(reason);
        return uploadJobRepository.save(job);
    }

    @Override
    public UploadJob retryJob(Long jobId) {
        UploadJob job = uploadJobRepository.findById(jobId)
                .orElseThrow(() -> new HttpException(
                        HttpStatus.NOT_FOUND,
                        "Upload job not found",
                        "JOB_NOT_FOUND"
                ));

        if (!canRetry(job)) {
            throw new HttpException(
                    HttpStatus.BAD_REQUEST,
                    "Job cannot be retried",
                    "JOB_NOT_RETRYABLE"
            );
        }

        job.setStatus(JobStatus.RETRY);
        job.setRetries(job.getRetries() + 1);
        job.setFailedAt(null);
        job.setFailedReason(null);
        return uploadJobRepository.save(job);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canRetry(UploadJob job) {
        return job.getStatus() == JobStatus.FAILED && job.getRetries() < job.getMaxRetries();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UploadJob> getRetryableJobs() {
        List<UploadJob> failedJobs = uploadJobRepository.findByStatus(JobStatus.FAILED);
        return failedJobs.stream()
                .filter(this::canRetry)
                .toList();
    }

    @Override
    public void deleteJob(Long jobId) {
        if (!uploadJobRepository.existsById(jobId)) {
            throw new HttpException(
                    HttpStatus.NOT_FOUND,
                    "Upload job not found",
                    "JOB_NOT_FOUND"
            );
        }
        uploadJobRepository.deleteById(jobId);
    }

    @Override
    @Transactional(readOnly = true)
    public JobStatistics getJobStatistics() {
        List<UploadJob> allJobs = uploadJobRepository.findAll();

        long pending = allJobs.stream().mapToLong(job -> job.getStatus() == JobStatus.PENDING ? 1 : 0).sum();
        long processing = allJobs.stream().mapToLong(job -> job.getStatus() == JobStatus.PROCESSING ? 1 : 0).sum();
        long completed = allJobs.stream().mapToLong(job -> job.getStatus() == JobStatus.COMPLETED ? 1 : 0).sum();
        long failed = allJobs.stream().mapToLong(job -> job.getStatus() == JobStatus.FAILED ? 1 : 0).sum();
        long retry = allJobs.stream().mapToLong(job -> job.getStatus() == JobStatus.RETRY ? 1 : 0).sum();

        return new JobStatistics(pending, processing, completed, failed, retry, allJobs.size());
    }

}
