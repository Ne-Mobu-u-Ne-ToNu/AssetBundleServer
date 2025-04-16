package com.usachevsergey.AssetBundleServer.controllers;

import com.usachevsergey.AssetBundleServer.annotations.EmailVerifiedOnly;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@RestController
@RequestMapping("/api/private")
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @EmailVerifiedOnly
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        return saveFile(file);
    }

    @EmailVerifiedOnly
    @GetMapping("/download/{filename}")
    public ResponseEntity<?> downloadFile(@PathVariable String filename) {
        try {
            Path filepath = Path.of(uploadDir, filename).toAbsolutePath();
            byte[] fileBytes = Files.readAllBytes(filepath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(Map.of("file", fileBytes));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Не удалось скачать файл!"));
        }
    }

    private ResponseEntity<?> saveFile(MultipartFile file) {
        try {
            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            Path targetPath = Path.of(uploadDir, filename).toAbsolutePath();
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok(Map.of("message", "Файл загружен: " + filename));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Ошибка загрузки файла: " + file.getOriginalFilename()));
        }
    }
}
