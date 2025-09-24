package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public class MultipartFileEmailAttachment implements EmailAttachment {
    private final MultipartFile file;
    
    public MultipartFileEmailAttachment(MultipartFile file) {
        this.file = file;
    }
    
    @Override
    public InputStream getInputStream() throws Exception {
        return file.getInputStream();
    }
    
    @Override
    public String getFileName() {
        return file.getOriginalFilename();
    }
    
    @Override
    public String getContentType() {
        return file.getContentType();
    }
    
    @Override
    public long getSize() {
        return file.getSize();
    }
} 