package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "upload_job")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private JobStatus status;


    @Column(name = "created_at")
    private LocalDateTime createdAt;    


    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failed_reason")
    private String failedReason;

    @Column(name = "retries")
    private int retries;

    @Column(name = "max_retries")
    private int maxRetries;

    @Column(name = "retry_delay")
    private int retryDelay;

    @Column(name = "retry_delay_unit")
    private String retryDelayUnit;

    @Column(name = "file_id")
    private Long fileId;

}
