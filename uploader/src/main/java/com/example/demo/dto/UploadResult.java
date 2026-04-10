package com.example.demo.dto;

public record UploadResult(
        String objectName,
        String bucketName,
        String url,
        int chunkCount) {
}
