package com.example.demo.model;

import java.util.concurrent.Callable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class PartJob  {
    private int partNumber;
    private byte[] data;
    private UploadJob parent;
    

    public void done(){
        parent.markPartCompleted(partNumber);
    }

}
