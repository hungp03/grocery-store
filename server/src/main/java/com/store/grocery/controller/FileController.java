package com.store.grocery.controller;

import com.store.grocery.domain.response.file.ResUploadFileDTO;
import com.store.grocery.service.FileService;
import com.store.grocery.util.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;

@RestController
@RequestMapping("api/v2")
@RequiredArgsConstructor
public class FileController {
    @Value("${upload-file.base-uri}")
    private String baseURI;

    private final FileService fileService;

    @PostMapping("files")
    @ApiMessage("Upload single file")
    public ResponseEntity<ResUploadFileDTO> upload(
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam("folder") String folder) throws IOException, URISyntaxException {

        fileService.validateFile(file);
        fileService.createDirectory(baseURI + folder);
        String uploadFile = fileService.store(file, folder);

        ResUploadFileDTO response = new ResUploadFileDTO(uploadFile, Instant.now());
        return ResponseEntity.ok(response);
    }
}
