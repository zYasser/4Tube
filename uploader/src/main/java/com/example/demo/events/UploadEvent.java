package com.example.demo.events;

import lombok.Builder;

@Builder
public class UploadEvent {
    private Long id;
    private String fileId;

}
