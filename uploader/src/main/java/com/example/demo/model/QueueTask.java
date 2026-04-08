package com.example.demo.model;

import java.util.UUID;
import java.util.concurrent.Callable;

import lombok.Getter;
@Getter
public class QueueTask<T> {

    private Callable<T> task;
    private String id;

    public QueueTask(Callable<T> task) {
        this.task = task;
        this.id = UUID.randomUUID().toString();
    }
}
