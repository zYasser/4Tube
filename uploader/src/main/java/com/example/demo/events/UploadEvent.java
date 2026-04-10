package com.example.demo.events;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UploadEvent {
    private String id;
    private String fileId;
    private String originalFilename;
    private String location;
    private Long size;
    private String contentType;
    private Integer chunkCount;

    @Override
    public String toString()
    {
        return "UploadEvent{" +
                "id=" + id +
                ", fileId='" + fileId + '\'' +
                ", originalFilename='" + originalFilename + '\'' +
                ", location='" + location + '\'' +
                ", size=" + size +
                ", contentType='" + contentType + '\'' +
                ", chunkCount=" + chunkCount +
                '}';
    }
}
