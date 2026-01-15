package com.example.demo.services;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.FileMetadata;

public interface FileStorageService {

    /**
     * Store a file and return the file metadata
     * @param file the multipart file to store
     * @return the stored file metadata
     * @throws IOException if file storage fails
     */
    FileMetadata store(MultipartFile file) throws IOException;

    /**
     * Load a file by its filename
     * @param filename the filename
     * @return the file path
     */
    Path load(String filename);

    /**
     * Get file metadata by filename
     * @param filename the filename
     * @return Optional containing FileMetadata if found
     */
    Optional<FileMetadata> getMetadata(String filename);

    /**
     * Get all file metadata
     * @return List of all FileMetadata
     */
    List<FileMetadata> getAllMetadata();

    /**
     * Delete a file by its filename (removes both file and metadata)
     * @param filename the filename to delete
     * @throws IOException if deletion fails
     */
    void delete(String filename) throws IOException;

    /**
     * Delete a file by its metadata (removes both file and metadata)
     * @param metadata the file metadata
     * @throws IOException if deletion fails
     */
    void delete(FileMetadata metadata) throws IOException;

    /**
     * Download a file by its ID
     * @param id the file ID
     * @param offset the offset
     * @param length the length
     * @return ResponseEntity containing the file resource
     */
    ResponseEntity<Resource> downloadFileById(Long id, long offset, long length);
}
