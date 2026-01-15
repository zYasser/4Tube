package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.JobStatus;
import com.example.demo.entity.UploadJob;

import java.util.List;
import java.util.Optional;

@Repository
public interface UploadJobRepository extends JpaRepository<UploadJob, Long> {

    /**
     * Find upload job by status
     * @param status the status to search for
     * @return List of UploadJob with the given status
     */
    List<UploadJob> findByStatus(JobStatus status);

    /**
     * Find upload job by status order by created date descending
     * @param status the status to search for
     * @return List of UploadJob ordered by creation date
     */
    List<UploadJob> findByStatusOrderByCreatedAtDesc(JobStatus status);

    /**
     * Find upload job by ID
     * @param id the ID to search for
     * @return Optional containing UploadJob if found
     */
    Optional<UploadJob> findById(Long id);

    /**
     * Check if upload job exists by status
     * @param status the status to check
     * @return true if exists, false otherwise
     */
    boolean existsByStatus(JobStatus status);
}
