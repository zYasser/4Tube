package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public FileMetadata(String filename, String originalFilename, String location, Long size, String contentType) {
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.location = location;
        this.size = size;
        this.contentType = contentType;
    }
}
