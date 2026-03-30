package com.example.demo.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;

import com.example.demo.utils.CustomMinioClient;

import io.minio.ObjectWriteResponse;
import io.minio.UploadPartResponse;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class UploadJob implements Callable<Void> {
    private boolean[] bucket;
    private String objectName;
    private Queue<PartJob> parts;
    private String uploadId;
    private int totalParts;
    private volatile int completedParts;
    private final int CHUNK_SIZE = 1024 * 1024*5;
    private  boolean  isDone = false;
    private CustomMinioClient customMinioClient;

    public UploadJob(String objectName, String uploadId, byte[] data, CustomMinioClient customMinioClient) {
        this.objectName = objectName;
        parts = new LinkedList<>();
        int part = 1;
        for (int i = 0; i < data.length; i += CHUNK_SIZE) {
            int end = Math.min(data.length, i + CHUNK_SIZE);
            PartJob p = PartJob.builder().data(Arrays.copyOfRange(data, i, end)).partNumber(part++).parent(this)
                    .build();
            parts.offer(p);
        }
        this.bucket = new boolean[parts.size()];

        this.uploadId = uploadId;
        this.totalParts = parts.size();
        this.completedParts = 0;
        this.customMinioClient = customMinioClient;
    }

    public int totalParts() {
        return this.totalParts;
    }

    public void markPartCompleted(int partNumber) {
        synchronized (this) {
            if (!bucket[partNumber - 1]) {
                bucket[partNumber - 1] = true;
                completedParts++;
            }
            else if (bucket[partNumber-1]) {
                throw new RuntimeException("Duplicate Upload");
            }
            if (completedParts == totalParts) {
                this.markDone();
            }
        }
    }

    public synchronized void markDone() {
        isDone = true;
        ObjectWriteResponse res = customMinioClient.completeMultipartUpload(objectName, uploadId, totalParts).join();
        log.info("Result is {}",res);
        this.notifyAll();
    }

    public synchronized boolean isDone() {
        return isDone;
    }

    public String getObjectName() {
        return this.objectName;

    }

    public String getUploadId() {
        return this.uploadId;
    }

    private PartJob pollJob() {
        PartJob job = null;
        if (isDone)
            return null;
        synchronized (parts) {
            if (parts.isEmpty()) {
                return null;
            }
            job = parts.poll();
        }

        return job;
    }

    @Override
    public Void call() throws Exception {
        while (true) {
            PartJob job = pollJob();

            if (job == null) {
                return null; // no more work
            }

            UploadPartResponse uploadPartResponse = customMinioClient
                    .uploadPart(objectName, uploadId, job.getPartNumber(), job.getData())
                    .join();

            log.info(uploadPartResponse.toString());

            markPartCompleted(job.getPartNumber());
        }
    }
}
