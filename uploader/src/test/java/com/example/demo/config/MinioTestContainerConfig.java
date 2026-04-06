package com.example.demo.config;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.wait.strategy.Wait;

public final class MinioTestContainerConfig {

    public static final String ACCESS_KEY = "minioadmin";
    public static final String SECRET_KEY = "minioadmin";

    private static final int API_PORT = 9000;
    private static final DockerImageName IMAGE = DockerImageName.parse("minio/minio:latest");

    private MinioTestContainerConfig() {
    }

    public static GenericContainer<?> newContainer() {
        return new GenericContainer<>(IMAGE)
                .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
                .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY)
                .withCommand("server", "/data")
                .withExposedPorts(API_PORT)
                .waitingFor(Wait.forHttp("/minio/health/ready").forPort(API_PORT));
    }

    public static String endpoint(GenericContainer<?> container) {
        return "http://" + container.getHost() + ":" + container.getMappedPort(API_PORT);
    }
}
