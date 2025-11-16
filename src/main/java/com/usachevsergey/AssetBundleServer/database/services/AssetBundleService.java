package com.usachevsergey.AssetBundleServer.database.services;

import com.usachevsergey.AssetBundleServer.database.dto.AssetBundleDTO;
import com.usachevsergey.AssetBundleServer.database.enumerations.SortOption;
import com.usachevsergey.AssetBundleServer.database.repositories.AssetBundleImageRepository;
import com.usachevsergey.AssetBundleServer.database.repositories.AssetBundleInfoRepository;
import com.usachevsergey.AssetBundleServer.database.repositories.BundleCategoryRepository;
import com.usachevsergey.AssetBundleServer.database.repositories.UserBundleRepository;
import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleImage;
import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import com.usachevsergey.AssetBundleServer.database.tables.User;
import com.usachevsergey.AssetBundleServer.database.tables.UserBundle;
import com.usachevsergey.AssetBundleServer.requests.AddAssetBundleRequest;
import com.usachevsergey.AssetBundleServer.security.authorization.UserInputValidator;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
public class AssetBundleService {

    @Autowired
    private AssetBundleInfoRepository assetBundleInfoRepository;
    @Autowired
    private AssetBundleImageRepository assetBundleImageRepository;
    @Autowired
    private UserBundleRepository userBundleRepository;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BundleCategoryRepository bundleCategoryRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.thumbnails-dir}")
    private String thumbnailsDir;

    public void uploadAssetBundle(AddAssetBundleRequest request, User user) {
        AssetBundleInfo assetBundle = new AssetBundleInfo();
        assetBundle.setUploadedBy(user);
        assetBundle.setName(request.getName());
        assetBundle.setDescription(request.getDescription());
        assetBundle.setFilename(request.getFilename(user.getId()));
        assetBundle.setPrice(request.getPrice());
        assetBundleInfoRepository.save(assetBundle);

        categoryService.saveBundleCategories(request.getCategoryIds(), assetBundle);

        saveImages(assetBundle, request.getImagesNames(user.getId()));
    }

    public Map<String, ?> getBundlesBySearch(String name, String sort, List<Long> categoryIds,
                                                   int page, int size) {
        Page<AssetBundleInfo> bundlesPage;
        Pageable pageable;
        SortOption sortOption = SortOption.getFromString(sort);

        switch (sortOption) {
            case NAME -> pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            case DATE_ASC -> pageable = PageRequest.of(page, size, Sort.by("uploadedAt").ascending());
            case DATE_DESC -> pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
            default -> throw new IllegalArgumentException("Ну удалось выполнить сортировку!");
        }

        if (categoryIds.size() > 0) {
            bundlesPage = bundleCategoryRepository.findByNameAndCategoryIds(name, categoryIds, pageable);
        } else {
            bundlesPage = assetBundleInfoRepository.findByNameContainingIgnoreCase(name, pageable);
        }

        return Map.of(
                "bundles", createDTOFromInfo(bundlesPage.getContent()),
                "totalPages", bundlesPage.getTotalPages(),
                "currentPage", page,
                "totalElements", bundlesPage.getTotalElements()
        );
    }

    public AssetBundleDTO getBundleById(Long id) {
        return createDTOFromInfo(getBundle(id));
    }

    public List<AssetBundleDTO> getBundlesByDeveloper(User user) {
        return createDTOFromInfo(assetBundleInfoRepository.findByUploadedBy(user));
    }

    public AssetBundleInfo getBundle(Long id) {
        return assetBundleInfoRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Не удалось найти бандл!"));
    }

    public AssetBundleDTO createDTOFromInfo(AssetBundleInfo info) {
        List<AssetBundleImage> images = assetBundleImageRepository.findImagesByAssetBundle(info).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Изображения не найдены!"));
        List<String> paths = images.stream()
                .map(AssetBundleImage::getPath)
                .toList();

        return new AssetBundleDTO(
                info.getId(),
                info.getName(),
                info.getDescription(),
                info.getFilename(),
                info.getUploadedAt(),
                info.getUploadedBy(),
                info.getPrice(),
                paths,
                bundleCategoryRepository.findByAssetBundle(info)
        );
    }

    public void purchaseBundles(User user, List<AssetBundleInfo> bundles) {
        for (AssetBundleInfo bundle : bundles) {
            purchaseBundle(user, bundle);
        }
    }

    public List<AssetBundleDTO> getBundlesByUser(User user) {
        return createDTOFromInfo(userBundleRepository.findBundlesByUser(user));
    }

    @Transactional
    public void deleteBundle(AssetBundleInfo bundle) {
        List<AssetBundleImage> images = assetBundleImageRepository.findImagesByAssetBundle(bundle).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Изображения не найдены!"));

        try {
            Path filePath;
            for (AssetBundleImage image : images) {
                filePath = Paths.get(thumbnailsDir, image.getPath());
                Files.deleteIfExists(filePath);
                assetBundleImageRepository.delete(image);
            }

            filePath = Paths.get(uploadDir, bundle.getFilename());
            Files.deleteIfExists(filePath);
            assetBundleInfoRepository.delete(bundle);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Не удалось удалить бандл: " + bundle.getName());
        }
    }

    @Transactional
    public void deleteAllBundles(List<AssetBundleInfo> bundles) {
        for (AssetBundleInfo bundle : bundles) {
            deleteBundle(bundle);
        }
    }

    private List<AssetBundleDTO> createDTOFromInfo(List<AssetBundleInfo> info) {
        return info.stream().map(this::createDTOFromInfo).toList();
    }

    private void saveImages(AssetBundleInfo assetBundle, List<String> imageList) {
        for (String current : imageList) {
            AssetBundleImage assetBundleImage = new AssetBundleImage();
            assetBundleImage.setAssetBundle(assetBundle);
            assetBundleImage.setPath(current);
            assetBundleImageRepository.save(assetBundleImage);
        }
    }

    private void purchaseBundle(User user, AssetBundleInfo assetBundle) {
        if (userBundleRepository.existsByUserAndAssetBundle(user, assetBundle)) {
            throw new IllegalStateException("Бандл уже куплен!");
        }

        UserBundle userBundle = new UserBundle();
        userBundle.setUser(user);
        userBundle.setAssetBundle(assetBundle);
        userBundleRepository.save(userBundle);
    }
}