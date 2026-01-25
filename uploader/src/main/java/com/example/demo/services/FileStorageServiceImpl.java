package com.example.demo.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.config.RabbitMqConfig;
import com.example.demo.entity.FileMetadata;
import com.example.demo.events.UploadEvent;
import com.example.demo.exceptions.HttpException;
import com.example.demo.reader.FileReader;
import com.example.demo.repository.FileMetadataRepository;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;
    private final FileMetadataRepository metadataRepository;
    private final RabbitTemplate rabbitTemplate;

    public FileStorageServiceImpl(
            @Value("${app.upload.dir:uploads}") String uploadDir,
            FileMetadataRepository metadataRepository,
            RabbitTemplate rabbitTemplate) throws IOException {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.metadataRepository = metadataRepository;
        this.rabbitTemplate = rabbitTemplate;
        System.out.println("fileStorageLocation = " + fileStorageLocation);

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new IOException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public FileMetadata store(MultipartFile file) throws IOException {
        // Generate a unique filename to avoid conflicts
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFilename);

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Create and save file metadata
        FileMetadata metadata = new FileMetadata(
            uniqueFilename,
            originalFilename,
            targetLocation.toString(),
            file.getSize(),
            file.getContentType()
        );
        metadataRepository.save(metadata);
        
        UploadEvent uploadEvent = UploadEvent.builder()
            .id(metadata.getId())
            .fileId(metadata.getFilename())
            .build();
        System.out.println("uploadEvent.toString() = " + uploadEvent.toString());
        rabbitTemplate.convertAndSend(RabbitMqConfig.UPLOAD_EXCHANGE, RabbitMqConfig.UPLOAD_ROUTING_KEY, uploadEvent);

        return metadata;
    }

    @Override
    public Path load(String filename) {
        return fileStorageLocation.resolve(filename).normalize();
    }

    @Override
    public Optional<FileMetadata> getMetadata(String filename) {
        return metadataRepository.findByFilename(filename);
    }

    @Override
    public List<FileMetadata> getAllMetadata() {
        return metadataRepository.findAll();
    }

    @Override
    public void delete(String filename) throws IOException {
        // Delete the physical file
        Path filePath = load(filename);
        Files.deleteIfExists(filePath);

        // Delete the metadata
        metadataRepository.findByFilename(filename)
            .ifPresent(metadata -> metadataRepository.delete(metadata));
    }

    @Override
    public void delete(FileMetadata metadata) throws IOException {
        // Delete the physical file
        Path filePath = Paths.get(metadata.getLocation());
        Files.deleteIfExists(filePath);

        // Delete the metadata
        metadataRepository.delete(metadata);
    }
    @Override
    public ResponseEntity<Resource> downloadFileById(Long id, long offset, long length) {
        FileMetadata metadata = metadataRepository.findById(id)
                .orElseThrow(() -> new HttpException(
                        HttpStatus.NOT_FOUND,
                        "File not found",
                        "FILE_NOT_FOUND"
                ));
    
        File file = new File(metadata.getLocation());
        long fileSize = file.length();
        byte[] content = FileReader.readFile(file, offset, length);
    
    
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(content));
    
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.parseMediaType(
                        metadata.getContentType() != null
                                ? metadata.getContentType()
                                : "application/octet-stream"
                ))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + metadata.getOriginalFilename() + "\"")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE,
                        "bytes " + offset + "-" + (offset + content.length - 1) + "/" + fileSize)
                .contentLength(content.length)
                .body(resource);
    }
    
}
