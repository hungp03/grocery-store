package com.store.grocery.service;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URISyntaxException;

public interface FileService {
//    void createDirectory(String folder) throws URISyntaxException;
//    String store(MultipartFile file, String folder) throws IOException;
//    long getFileLength (String fileName, String folder) throws URISyntaxException;
//    InputStreamResource getResource(String fileName, String folder) throws URISyntaxException, FileNotFoundException;
    void validateFile(MultipartFile file);
    String upload(MultipartFile file, String folder) throws IOException;
}
