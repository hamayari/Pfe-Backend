package com.example.demo.service;

import java.io.InputStream;

public interface EmailAttachment {
    InputStream getInputStream() throws Exception;
    String getFileName();
    String getContentType();
    long getSize();
} 