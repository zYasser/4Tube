package com.example.demo.services;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.UploadResult;
import com.example.demo.model.UploadJob;
import com.example.demo.threads.ThreadPool;
import com.example.demo.utils.CustomMinioClient;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MinioService {

    private final MinioClient minioClient;
    private final CustomMinioClient customMinioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    public MinioService(MinioClient minioClient, CustomMinioClient customMinioClient) {
        this.minioClient = minioClient;
        this.customMinioClient = customMinioClient;
    }

    /**
     * Uploads a file to MinIO and returns the object URL.
     */
    public UploadResult upload(MultipartFile file, String objectName) {
        return upload(file, objectName, 3);
    }

    /**
     * Uploads a file to MinIO with retry logic and returns the object URL.
     */
    public UploadResult upload(MultipartFile file, String objectName, int maxRetries) {
        Exception lastException = null;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                byte[] data = file.getBytes();
                String id = customMinioClient.initMultiPartUpload(objectName);
                UploadJob job = new UploadJob(objectName, id, data, customMinioClient);
                ThreadPool pool = new ThreadPool();
                int chunkCount = job.totalParts();
                for (int i = 0; i < chunkCount; i++) {
                    pool.submitTask(job);
                }
                synchronized (job) {
                    while (!job.isDone()) {
                        job.wait();
                    }
                }
                log.info("Upload completed on attempt " + (attempt + 1));
                return new UploadResult(
                        objectName,
                        bucketName,
                        buildObjectUrl(objectName),
                        chunkCount);
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxRetries - 1) {
                    log.error("Attempt " + (attempt + 1) + " failed: " + e.getMessage() + ". Retrying...");
                    try {
                        Thread.sleep(1000 * (attempt + 1));
                    } catch (InterruptedException ie) {}
                }
            }
        }
        throw new RuntimeException("Upload failed after " + maxRetries + " attempts: " + lastException);
    }

    /**
     * Deletes an object from MinIO by object name.
     */
    public void delete(String objectName) throws IOException {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IOException("MinIO delete failed for object: " + objectName, e);
        }
    }

    /**
     * Generates a presigned URL valid for the given duration.
     */
    public String getPresignedUrl(String objectName, int expiryDays) throws IOException {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiryDays, TimeUnit.DAYS)
                            .build());
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IOException("Failed to generate presigned URL for: " + objectName, e);
        }
    }

    private String buildObjectUrl(String objectName) {
        return String.format("%s/%s/%s", endpoint, bucketName, objectName);
    }
}
