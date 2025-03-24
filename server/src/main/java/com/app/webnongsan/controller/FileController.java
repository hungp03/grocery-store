package com.app.webnongsan.controller;

import com.app.webnongsan.domain.response.file.ResUploadFileDTO;
import com.app.webnongsan.service.FileService;
import com.app.webnongsan.util.annotation.ApiMessage;
import com.app.webnongsan.util.exception.StorageException;
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
import java.util.List;
import java.util.Arrays;

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
