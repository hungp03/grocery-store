package com.store.grocery.controller;

import com.store.grocery.dto.response.file.UploadFileResponse;
import com.store.grocery.service.FileService;
import com.store.grocery.util.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
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
    private final FileService fileService;
    @PostMapping("files")
    @ApiMessage("Upload single file")
    public ResponseEntity<UploadFileResponse> upload(
            @RequestParam(name = "file", required = false) MultipartFile file) throws IOException, URISyntaxException {
        String uploadFile = fileService.upload(file);
        UploadFileResponse response = new UploadFileResponse(uploadFile, Instant.now());
        return ResponseEntity.ok(response);
    }
}
