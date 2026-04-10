package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "file_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Size(max = 255)
    @Column(name = "filename", nullable = false, unique = true)
    private String filename;

    @Size(max = 255)
    @Column(name = "original_filename")
    private String originalFilename;

    @NotNull
    @Size(max = 500)
    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "size")
    private Long size;

    @Size(max = 100)
    @Column(name = "content_type")
    private String contentType;

    @Column(name = "chunk_count")
    private Integer chunkCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public FileMetadata(String filename, String originalFilename, String location, Long size, String contentType,
            Integer chunkCount) {
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.location = location;
        this.size = size;
        this.contentType = contentType;
        this.chunkCount = chunkCount;
    }
}
