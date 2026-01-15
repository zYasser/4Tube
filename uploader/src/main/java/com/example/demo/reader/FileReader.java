package com.example.demo.reader;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileReader {


    public static byte[] readFile(File file, long offset, long length) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {

            raf.seek(offset);
            byte[] buffer = new byte[(int) length];
            int bytesRead = raf.read(buffer);
            if(bytesRead<length) {
                byte[] actual= new byte[(int) length];
                System.arraycopy(buffer, 0, actual, 0, bytesRead);
                return actual;
            }
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }

    }
    

}
