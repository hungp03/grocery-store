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
    private final FileService fileService;
    @PostMapping("files")
    @ApiMessage("Upload single file")
    public ResponseEntity<ResUploadFileDTO> upload(
            @RequestParam(name = "file", required = false) MultipartFile file) throws IOException, URISyntaxException {
        String uploadFile = fileService.upload(file);
        ResUploadFileDTO response = new ResUploadFileDTO(uploadFile, Instant.now());
        return ResponseEntity.ok(response);
    }
}
