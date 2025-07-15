package com.usachevsergey.AssetBundleServer.controllers;

import com.usachevsergey.AssetBundleServer.annotations.EmailVerifiedOnly;
import com.usachevsergey.AssetBundleServer.database.services.AssetBundleService;
import com.usachevsergey.AssetBundleServer.requests.AddAssetBundleRequest;
import com.usachevsergey.AssetBundleServer.security.authorization.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@RestController
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.thumbnails-dir}")
    private String thumbnailsDir;
    @Autowired
    private AssetBundleService assetBundleService;

    @EmailVerifiedOnly
    @PreAuthorize("hasAuthority('DEVELOPER')")
    @PostMapping("/api/secured/upload")
    public ResponseEntity<?> uploadFile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @ModelAttribute AddAssetBundleRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Пользователь не авторизован!"));
        }

        try {
            Path targetPath = Path.of(uploadDir, request.getFilename()).toAbsolutePath();
            Files.copy(request.getBundleFile().getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            for (MultipartFile current : request.getImages()) {
                targetPath = Path.of(thumbnailsDir, StringUtils.cleanPath(current.getOriginalFilename()))
                        .toAbsolutePath();
                Files.copy(current.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            assetBundleService.uploadAssetBundle(request, userDetails.getUsername());

            return ResponseEntity.ok(Map.of("message", "Файл загружен: " + request.getFilename()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Ошибка загрузки файла: " + request.getFilename()));
        }
    }

    @GetMapping("/api/public/allBundles")
    public ResponseEntity<?> getAllBundles() {
        return ResponseEntity.ok(assetBundleService.getAllBundles());
    }

    @GetMapping("/api/public/search")
    public ResponseEntity<?> searchAssetBundles(@RequestParam("name") String name) {
        return ResponseEntity.ok(assetBundleService.getBundlesByName(name));
    }

    @EmailVerifiedOnly
    @GetMapping("/api/private/download/{filename}")
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
}
