package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.GenericContainer;

import com.example.demo.config.MinioTestContainerConfig;
import com.example.demo.services.MinioService;
import com.example.demo.utils.CustomMinioClient;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;

class MinioServiceIntegrationTests {

    private static final String BUCKET_NAME = "4tube-integration-tests";

    static final GenericContainer<?> minioContainer = MinioTestContainerConfig.newContainer();

    private static MinioClient minioClient;
    private static MinioService minioService;

    @BeforeAll
    static void setUp() throws Exception {
        minioContainer.start();
        String endpoint = MinioTestContainerConfig.endpoint(minioContainer);

        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(MinioTestContainerConfig.ACCESS_KEY, MinioTestContainerConfig.SECRET_KEY)
                .build();

        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
        }

        CustomMinioClient customMinioClient = new CustomMinioClient(
                endpoint,
                MinioTestContainerConfig.ACCESS_KEY,
                MinioTestContainerConfig.SECRET_KEY,
                BUCKET_NAME);

        minioService = new MinioService(minioClient, customMinioClient);
        ReflectionTestUtils.setField(minioService, "bucketName", BUCKET_NAME);
        ReflectionTestUtils.setField(minioService, "endpoint", endpoint);
    }

    @AfterAll
    static void tearDown() {
        minioContainer.stop();
    }

    @Test
    @Timeout(30)
    void uploadStoresMultipartObjectInMinio() throws Exception {
        byte[] content = createContent((5 * 1024 * 1024) + 512);
        String objectName = uniqueObjectName("upload");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.bin",
                "application/octet-stream",
                content);

        String resultUrl = minioService.upload(file, objectName, 3);

        assertEquals(MinioTestContainerConfig.endpoint(minioContainer) + "/" + BUCKET_NAME + "/" + objectName, resultUrl);

        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder().bucket(BUCKET_NAME).object(objectName).build())) {
            assertArrayEquals(content, inputStream.readAllBytes());
        } finally {
            removeObject(objectName);
        }
    }

    @Test
    @Timeout(30)
    void getPresignedUrlReturnsSignedUrlForUploadedObject() throws Exception {
        byte[] content = "integration-test-object".getBytes();
        String objectName = uniqueObjectName("presigned");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                content);

        minioService.upload(file, objectName, 3);
        String presignedUrl = minioService.getPresignedUrl(objectName, 1);

        try {
            assertTrue(presignedUrl.contains(BUCKET_NAME + "/" + objectName));
            assertTrue(presignedUrl.contains("X-Amz-Algorithm"));
        } finally {
            removeObject(objectName);
        }
    }

    @Test
    @Timeout(30)
    void deleteRemovesUploadedObjectFromMinio() throws Exception {
        byte[] content = "delete-me".getBytes();
        String objectName = uniqueObjectName("delete");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "delete.txt",
                "text/plain",
                content);

        minioService.upload(file, objectName, 3);
        minioService.delete(objectName);

        assertThrows(Exception.class, () -> minioClient.statObject(
                StatObjectArgs.builder().bucket(BUCKET_NAME).object(objectName).build()));
    }

    private static String uniqueObjectName(String prefix) {
        return prefix + "/" + UUID.randomUUID() + ".bin";
    }

    private static byte[] createContent(int size) {
        byte[] content = new byte[size];
        for (int i = 0; i < size; i++) {
            content[i] = (byte) (i % 251);
        }
        return content;
    }

    private static void removeObject(String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(BUCKET_NAME).object(objectName).build());
    }
}
