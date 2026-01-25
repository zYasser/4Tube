package com.example.demo.events;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UploadEvent {
    private Long id;
    private String fileId;

    @Override
    public String toString()
    {
        return "UploadEvent{" +
                "id=" + id +
                ", fileId='" + fileId + '\'' +
                '}';
    }
}
