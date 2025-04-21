package com.store.grocery.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.store.grocery.service.FileService;
import com.store.grocery.util.exception.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    @Value("${cloudinary.default-folder}")
    private String defaultFolder;

    private final Cloudinary cloudinary;
    @Override
    public void validateFile(MultipartFile file) {
        log.info(">>> VALIDATE FILE");
        if (file == null || file.isEmpty()) {
            log.warn(">>> FILE IS EMPTY");
            throw new StorageException("File is empty. Please choose a file");
        }

        String fileName = file.getOriginalFilename();
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png");
        boolean isValid = allowedExtensions.stream().anyMatch(item ->
                fileName != null && fileName.toLowerCase().endsWith(item));

        if (!isValid) {
            log.warn(">>> FILE NOT ALLOWED");
            throw new StorageException("File not allowed! Please use file " + allowedExtensions);
        }
    }

    public String upload(MultipartFile file, String folder) throws IOException {
        validateFile(file);
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", defaultFolder + "/" + folder
        ));
        return uploadResult.get("url").toString();
    }

}
