package com.example.demo.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.Set;

public class FileFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_EXT = Set.of(
            "mp4",
            "mov",
            "avi",
            "mkv",
            "webm"
    );
    private static final Set<String> ALLOWED_VIDEO_MIME_TYPES = Set.of(
            "video/mp4",
            "video/webm",
            "video/quicktime", // mov
            "video/x-msvideo", // avi
            "video/x-matroska" // mkv
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {

        System.out.println("request.getClass().getName() = " + request.getClass().getName());
        if(request instanceof MultipartHttpServletRequest multipartHttpServletRequest) {
            if (multipartHttpServletRequest.getFileMap().values().size() == 1) {
                for (MultipartFile file : multipartHttpServletRequest.getFileMap().values()) {
                    String filename = file.getOriginalFilename();
                    if (filename == null || !isAllowed(filename , file.getContentType())) {
                        response.sendError(
                                HttpServletResponse.SC_BAD_REQUEST,
                                "File type not allowed"
                        );
                        return; 
                    }
                }
            }else{
                response.sendError(HttpServletResponse.SC_BAD_REQUEST , "ONLY ONE FILE ALLOWED");
                return; 
            }
        }else{
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        filterChain.doFilter(request, response);
    }
    private boolean isAllowed(String filename , String contentType) {
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return ALLOWED_EXT.contains(ext) &&  ALLOWED_VIDEO_MIME_TYPES.contains(contentType);
    }
}
