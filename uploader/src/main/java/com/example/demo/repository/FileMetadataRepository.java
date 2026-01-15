package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.FileMetadata;

import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    /**
     * Find file metadata by filename
     * @param filename the filename to search for
     * @return Optional containing FileMetadata if found
     */
    Optional<FileMetadata> findByFilename(String filename);

    /**
     * Check if a filename already exists
     * @param filename the filename to check
     * @return true if exists, false otherwise
     */
    boolean existsByFilename(String filename);
}
