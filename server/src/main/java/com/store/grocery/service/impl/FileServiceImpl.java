package com.store.grocery.service.impl;

import com.store.grocery.service.FileService;
import com.store.grocery.util.exception.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class FileServiceImpl implements FileService {
    @Value("${upload-file.base-uri}")
    private String baseURI;

    @Override
    public void createDirectory(String folder) throws URISyntaxException {
        URI uri = new URI(folder);
        Path path = Paths.get(uri);
        File tmpDir = new File(path.toString());
        if (!tmpDir.isDirectory()) {
            try {
                Files.createDirectory(tmpDir.toPath());
                System.out.println(">>> CREATE NEW DIRECTORY SUCCESSFUL, PATH = " + tmpDir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(">>> SKIP MAKING DIRECTORY, ALREADY EXISTS");
        }
    }

    @Override
    public String store(MultipartFile file, String folder) throws IOException {
        // Tạo tên file duy nhất
        String originalFileName = file.getOriginalFilename();
        String finalName = System.currentTimeMillis() + "-" + originalFileName;

        String encodedFileName = URLEncoder.encode(finalName, "UTF-8").replace("+", "%20");
        // Tạo URI từ baseURI, folder và tên file đã mã hóa
        URI uri = URI.create(baseURI + folder + "/" + encodedFileName);

        Path path = Paths.get(uri);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        }
        return finalName;
    }
    @Override
    public long getFileLength (String fileName, String folder) throws URISyntaxException {
        URI uri = new URI (baseURI + folder + "/" + fileName);
        Path path = Paths.get(uri);
        File tmpDir = new File(path.toString());
        // file không tồn tại, hoặc file là 1 director => return 0
        if (!tmpDir.exists() || tmpDir.isDirectory())
            return 0;
        return tmpDir.length();
    }
    @Override
    public InputStreamResource getResource(String fileName, String folder)
            throws URISyntaxException, FileNotFoundException {
        URI uri = new URI(baseURI + folder + "/" + fileName);
        Path path = Paths.get(uri);

        File file = new File(path.toString());
        return new InputStreamResource(new FileInputStream(file));
    }
    @Override
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty. Please choose a file");
        }

        String fileName = file.getOriginalFilename();
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png");
        boolean isValid = allowedExtensions.stream().anyMatch(item ->
                fileName != null && fileName.toLowerCase().endsWith(item));

        if (!isValid) {
            throw new StorageException("File not allowed! Please use file " + allowedExtensions);
        }
    }
}
