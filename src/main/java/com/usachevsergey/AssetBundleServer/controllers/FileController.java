package com.usachevsergey.AssetBundleServer.controllers;

import com.usachevsergey.AssetBundleServer.annotations.EmailVerifiedOnly;
import com.usachevsergey.AssetBundleServer.database.enumerations.Role;
import com.usachevsergey.AssetBundleServer.database.services.AssetBundleService;
import com.usachevsergey.AssetBundleServer.database.services.UserService;
import com.usachevsergey.AssetBundleServer.database.tables.User;
import com.usachevsergey.AssetBundleServer.requests.AddAssetBundleRequest;
import com.usachevsergey.AssetBundleServer.security.authorization.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
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
    @Autowired
    private UserService userService;

    @EmailVerifiedOnly
    @PreAuthorize("hasAuthority('DEVELOPER')")
    @PostMapping("/api/secured/upload")
    public ResponseEntity<?> uploadFile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @ModelAttribute AddAssetBundleRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Пользователь не авторизован!"));
        }

        User user = userService.getUser(userDetails.getUsername());

        try {
            Path targetPath = Path.of(uploadDir, request.getFilename(user.getId())).toAbsolutePath();
            Files.copy(request.getBundleFile().getInputStream(), targetPath);

            for (MultipartFile current : request.getImages()) {
                targetPath = Path.of(thumbnailsDir, user.getId() + "_" + StringUtils.cleanPath(current.getOriginalFilename()))
                        .toAbsolutePath();
                Files.copy(current.getInputStream(), targetPath);
            }

            assetBundleService.uploadAssetBundle(request, user);

            return ResponseEntity.ok(Map.of("message", "Файл загружен: " + request.getFilename(user.getId())));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Ошибка загрузки файла: " + request.getFilename(user.getId())));
        }
    }

    @GetMapping("/api/public/search")
    public ResponseEntity<?> searchAssetBundles(
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(assetBundleService.getBundlesBySearch(name, sort, page, size));
    }

    @GetMapping("/api/public/bundle/{id}")
    public ResponseEntity<?> getBundleById(@PathVariable Long id) {
        return ResponseEntity.ok(assetBundleService.getBundleById(id));
    }

    @EmailVerifiedOnly
    @PreAuthorize("hasAuthority('USER')")
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

    @EmailVerifiedOnly
    @GetMapping("/api/secured/myBundles")
    public ResponseEntity<?> getUserBundles(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Role role = Role.valueOf(userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null));

        User user = userService.getUser(userDetails.getUsername());

        switch (role) {
            case USER -> {
                throw new UnsupportedOperationException();
            }
            case DEVELOPER -> {
                return ResponseEntity.ok(Map.of("myBundles", assetBundleService.getBundlesByDeveloper(user)));
            }
            default -> throw new UnsupportedOperationException();
        }
    }
}
