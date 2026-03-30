package com.example.demo.utils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import io.minio.ListPartsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.UploadPartResponse;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Part;

@Component
public class CustomMinioClient extends MinioAsyncClient {

    private static final int DEFAULT_PART_SIZE = 10 * 1024 * 1024; // 10MB

    @Value("${minio.bucket-name}")
    private String bucketName;

    public CustomMinioClient(@Value("${minio.endpoint}") String endpoint,
                             @Value("${minio.access-key}") String accessKey,
                             @Value("${minio.secret-key}") String secretKey,
                             @Value("${minio.bucket-name}") String bucketName) {
        super(MinioAsyncClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build());
        this.bucketName = bucketName;
    }

    // ------------------------------------------------------------------ public API

    /**
     * Initiates a multipart upload and returns the upload ID asynchronously.
     */
    public String initMultiPartUpload(String objectName) {
        try {
            return createMultipartUploadAsync(bucketName, null, objectName, null, null)
                    .thenApply(response -> response.result().uploadId()).join();
        } catch (InvalidKeyException | InsufficientDataException | InternalException
                 | NoSuchAlgorithmException | XmlParserException | IOException e) {
                    throw new MinioOperationException("Failed to initiate multipart upload for: " + objectName, e);
        }
    }

    /**
     * Uploads a single part of a multipart upload.
     */
    public CompletableFuture<UploadPartResponse> uploadPart(String objectName, String uploadId,
                                                             int partNumber, byte[] data) {
        try {
            return uploadPartAsync(
                    bucketName, null, objectName,
                    data, data.length,
                    uploadId, partNumber,
                    null, null
            );
        } catch (InvalidKeyException | InsufficientDataException | InternalException
                 | NoSuchAlgorithmException | XmlParserException | IOException e) {
            return CompletableFuture.failedFuture(
                    new MinioOperationException(
                            "Failed to upload part " + partNumber + " for: " + objectName, e));
        }
    }

    /**
     * Completes a multipart upload asynchronously.
     */
    public CompletableFuture<ObjectWriteResponse> completeMultipartUpload(String objectName,
                                                            String uploadId,int partSize) {
        try {
            ListPartsResponse list=this.listPartsAsync(bucketName, null , objectName, partSize,null,uploadId,null,null ).join();
            Part[] parts = list.result().partList().toArray(new Part[0]);
            return completeMultipartUploadAsync(
                    bucketName, null, objectName,
                    uploadId, parts,
                    null, null
            );
        } catch (InvalidKeyException | InsufficientDataException | InternalException
                 | NoSuchAlgorithmException | XmlParserException | IOException e) {
            return CompletableFuture.failedFuture(
                    new MinioOperationException(
                            "Failed to complete multipart upload for: " + objectName, e));
        }
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Splits a byte array into chunks of {@code chunkSize} bytes.
     * The last chunk may be smaller.
     */
    private List<byte[]> splitIntoChunks(byte[] data, int chunkSize) {
        List<byte[]> chunks = new ArrayList<>();
        int offset = 0;
        while (offset < data.length) {
            int length = Math.min(chunkSize, data.length - offset);
            chunks.add(Arrays.copyOfRange(data, offset, offset + length));
            offset += length;
        }
        return chunks;
    }

    // ------------------------------------------------------------------ exception

    /**
     * Unchecked wrapper for MinIO exceptions to avoid polluting call sites.
     */
    public static class MinioOperationException extends RuntimeException {
        public MinioOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}