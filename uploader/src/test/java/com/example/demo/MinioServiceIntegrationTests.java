package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.example.demo.services.MinioService;
import com.example.demo.utils.CustomMinioClient;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.concurrent.CompletableFuture;
import java.time.Duration;

@SpringBootTest
class MinioServiceIntegrationTests {

    @Mock
    CustomMinioClient customMinioClient;

    @Test
    @Timeout(30)
    void testSuccessfulUpload() throws Exception {
        // Arrange: Mock successful responses for all operations
        Mockito.when(customMinioClient.initMultiPartUpload(Mockito.anyString()))
            .thenAnswer(invocation -> CompletableFuture.completedFuture("upload-id-1"));
        
        Mockito.when(customMinioClient.uploadPart(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyInt(),
                Mockito.any(byte[].class)
        ))
            .thenAnswer(invocation -> CompletableFuture.completedFuture(null));
        
        Mockito.when(customMinioClient.completeMultipartUpload(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyInt()
        ))
            .thenAnswer(invocation -> CompletableFuture.completedFuture(null));

        // Act: Perform upload with default retry count (3)
        MinioService service = new MinioService(null, customMinioClient);
        MultipartFile file = createMockFile();
        String resultUrl = service.upload(file, "test-object.txt", 3);

        // Assert: Verify the URL is returned correctly
        assert(resultUrl.contains("test-object.txt"));
    }

    @Test
    @Timeout(60)
    void testRetryOnTransientFailure() throws Exception {
        // Arrange: Mock uploadPart to fail on first call, succeed on second
        int[] callCount = {0};
        Mockito.when(customMinioClient.initMultiPartUpload(Mockito.anyString()))
            .thenAnswer(invocation -> CompletableFuture.completedFuture("upload-id-1"));
        
        Mockito.when(customMinioClient.uploadPart(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyInt(),
                Mockito.any(byte[].class)
        ))
            .thenAnswer(invocation -> {
                callCount[0]++;
                if (callCount[0] == 1) {
                    return CompletableFuture.failedFuture(new IOException("Transient network error"));
                }
                return CompletableFuture.completedFuture(null);
            });
        
        Mockito.when(customMinioClient.completeMultipartUpload(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyInt()
        ))
            .thenAnswer(invocation -> CompletableFuture.completedFuture(null));

        // Act: Perform upload with retry enabled
        MinioService service = new MinioService(null, customMinioClient);
        MultipartFile file = createMockFile();
        String resultUrl = service.upload(file, "test-object.txt", 3);

        // Assert: Verify that retry occurred (at least one failure followed by success)
        assert(callCount[0] >= 2); // Should have retried at least once
        assert(resultUrl.contains("test-object.txt"));
    }

    @Test
    @Timeout(30)
    void testMaxRetriesExhausted() throws Exception {
        // Arrange: Mock uploadPart to always fail
        Mockito.when(customMinioClient.initMultiPartUpload(Mockito.anyString()))
            .thenAnswer(invocation -> CompletableFuture.completedFuture("upload-id-1"));
        
        Mockito.when(customMinioClient.uploadPart(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyInt(),
                Mockito.any(byte[].class)
        ))
            .thenAnswer(invocation -> CompletableFuture.failedFuture(new IOException("Permanent failure")));

        // Act: Perform upload with max retries exhausted
        MinioService service = new MinioService(null, customMinioClient);
        MultipartFile file = createMockFile();
        
        RuntimeException exception = null;
        try {
            service.upload(file, "test-object.txt", 2); // Only 2 retries allowed
        } catch (RuntimeException e) {
            exception = e;
        }

        // Assert: Verify that upload fails after max retries are exhausted
        assert(exception != null);
        assert(exception.getMessage().contains("failed after 2 attempts"));
    }

    @Test
    @Timeout(60)
    void testExponentialBackoffTiming() throws Exception {
        // Arrange: Mock uploadPart to fail on first call only
        int[] callCount = {0};
        Duration[] delays = new Duration[1];
        
        Mockito.when(customMinioClient.initMultiPartUpload(Mockito.anyString()))
            .thenAnswer(invocation -> CompletableFuture.completedFuture("upload-id-1"));
        
        Mockito.when(customMinioClient.uploadPart(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyInt(),
                Mockito.any(byte[].class)
        ))
            .thenAnswer(invocation -> {
                callCount[0]++;
                if (callCount[0] == 1) {
                    return CompletableFuture.failedFuture(new IOException("Transient error"));
                }
                return CompletableFuture.completedFuture(null);
            });
        
        Mockito.when(customMinioClient.completeMultipartUpload(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyInt()
        ))
            .thenAnswer(invocation -> CompletableFuture.completedFuture(null));

        // Act: Perform upload and measure timing between retries
        MinioService service = new MinioService(null, customMinioClient);
        MultipartFile file = createMockFile();
        
        long startTime = System.currentTimeMillis();
        String resultUrl = service.upload(file, "test-object.txt", 3);
        long endTime = System.currentTimeMillis();

        // Assert: Verify that retry occurred and total time includes backoff delay
        assert(callCount[0] >= 2); // Should have retried at least once
        assert(endTime - startTime > 1000); // Should include at least 1 second of backoff
    }

    private MultipartFile createMockFile() {
        return Mockito.mock(MultipartFile.class);
    }
}
