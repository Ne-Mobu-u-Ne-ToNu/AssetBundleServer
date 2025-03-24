package com.usachevsergey.AssetBundleServer.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Controller
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping("/files")
    public String index(Model model) {
        return "files";
    }

    @PostMapping("/api/upload")
    @ResponseBody
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        return saveFile(file);
    }

    @GetMapping("/api/download/{filename}")
    @ResponseBody
    public ResponseEntity<byte[]> downloadFile(@PathVariable String filename) {
        try {
            Path filepath = Path.of(uploadDir, filename).toAbsolutePath();
            byte[] fileBytes = Files.readAllBytes(filepath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(fileBytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    private ResponseEntity<String> saveFile(MultipartFile file) {
        try {
            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            Path targetPath = Path.of(uploadDir, filename).toAbsolutePath();
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok("Файл загружен: " + filename);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка загрузки файла: " + file.getOriginalFilename());
        }
    }
}
