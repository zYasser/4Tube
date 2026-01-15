package com.example.demo.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ErrorResponse;
import com.example.demo.entity.FileMetadata;
import com.example.demo.services.FileStorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/blob")
@Tag(name = "Blob Management", description = "APIs for file upload and blob management")
public class BlobController {

    private final FileStorageService fileStorageService;

    public BlobController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Operation(summary = "Upload a video file",
               description = "Upload a single video file. Only video formats (mp4, mov, avi, mkv, webm) are allowed.")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<FileMetadata>> uploadFile(
            @Parameter(description = "Video file to upload", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            FileMetadata metadata = fileStorageService.store(file);
            return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", metadata));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(ErrorResponse.of("Failed to upload file: " + e.getMessage(), "FILE_UPLOAD_ERROR"));
        }
    }

     @Operation(
         summary = "Download a file",
         description = "Download a file by its ID with optional byte range"
     )
     @GetMapping("/download/{id}")
     public ResponseEntity<Resource> downloadFile(
    
         @Parameter(
             description = "ID of the file to download",
             required = true,
             example = "10"
         )
         @PathVariable Long id,
    
         @Parameter(
             description = "Starting byte offset",
             example = "0"
         )
         @RequestParam(defaultValue = "0") long offset,
    
         @Parameter(
             description = "Number of bytes to read",
             example = "1024"
         )
         @RequestParam(defaultValue = "1024") long length
     ) {
         return fileStorageService.downloadFileById(id, offset, length);
     }
    

     @Operation(summary = "Get all files metadata",
                description = "Retrieve metadata for all uploaded files")
     @GetMapping("/metadata")
     public ResponseEntity<ApiResponse<List<FileMetadata>>> getAllFilesMetadata() {
         try {
             List<FileMetadata> metadataList = fileStorageService.getAllMetadata();
             return ResponseEntity.ok(ApiResponse.success("Files metadata retrieved successfully", metadataList));
         } catch (Exception e) {
             return ResponseEntity.internalServerError()
                     .body(ErrorResponse.of("Failed to retrieve files metadata: " + e.getMessage(), "METADATA_RETRIEVAL_ERROR"));
         }
     }
}
