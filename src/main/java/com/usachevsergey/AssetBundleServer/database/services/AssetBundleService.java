package com.usachevsergey.AssetBundleServer.database.services;

import com.usachevsergey.AssetBundleServer.database.dto.AssetBundleDTO;
import com.usachevsergey.AssetBundleServer.database.enumerations.SortOption;
import com.usachevsergey.AssetBundleServer.database.repositories.AssetBundleImageRepository;
import com.usachevsergey.AssetBundleServer.database.repositories.AssetBundleInfoRepository;
import com.usachevsergey.AssetBundleServer.database.repositories.BundleCategoryRepository;
import com.usachevsergey.AssetBundleServer.database.repositories.UserBundleRepository;
import com.usachevsergey.AssetBundleServer.database.tables.*;
import com.usachevsergey.AssetBundleServer.exceptions.FieldNotFoundException;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp"
    );

    private static final Set<String> BUNDLE_EXTENSIONS = Set.of(
            "zip", "rar", "7z", "tar", "gz"
    );

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

        saveImagesDB(assetBundle, request.getImagesNames(user.getId()));
    }

    public void updateBundle(Long bundleId, AddAssetBundleRequest request, User user) throws IOException {
        AssetBundleInfo assetBundle = assetBundleInfoRepository.findById(bundleId).orElseThrow(
                () -> new IllegalArgumentException("Не удалось найти бандл!")
        );
        if (!assetBundleInfoRepository.existsByUploadedBy(user)) {
            throw new IllegalArgumentException("У вас нет прав на редактирование бандла!");
        }
        boolean changed = false;
        boolean categoryChanged = false;
        boolean imagesChanged = false;

        // Смена названия
        String newName = request.getName();
        if (!UserInputValidator.isNullOrEmpty(newName) && !assetBundle.getName().equals(newName)) {
            if (assetBundleInfoRepository.existsByNameAndUploadedBy(newName, user)) {
                throw new IllegalArgumentException("Выберите другое название!");
            }

            assetBundle.setName(newName);
            changed = true;
        }

        // Смена описания
        String newDesc = request.getDescription();
        if (!UserInputValidator.isNullOrEmpty(newDesc) && !assetBundle.getDescription().equals(newDesc)) {
            assetBundle.setDescription(newDesc);
            changed = true;
        }

        // Смена цены
        BigDecimal newPrice = request.getPrice();
        if (!(newPrice == null) && assetBundle.getPrice().compareTo(newPrice) != 0) {
            assetBundle.setPrice(newPrice);
            changed = true;
        }

        // Смена категорий
        List<Long> oldIds = bundleCategoryRepository.findByAssetBundle(assetBundle).stream()
                .map(Category::getId)
                .toList();
        List<Long> newIds = request.getCategoryIds();
        if (!(newIds == null) && !newIds.isEmpty() && !newIds.equals(oldIds)) {
            categoryService.editBundleCategories(newIds, assetBundle);
            categoryChanged = true;
        }

        // Смена файла бандла
        if (!(request.getBundleFile() == null) &&
                !request.getFilename(user.getId()).equals(assetBundle.getFilename()) &&
                checkBundleNames(request.getFilename(user.getId()))) {
            Files.deleteIfExists(Paths.get(uploadDir, assetBundle.getFilename()));
            saveBundleFile(request, user.getId());
            assetBundle.setFilename(request.getFilename(user.getId()));
            changed = true;
        }

        // Смена изображений
        if (!(request.getImages() == null) &&
                !(request.getImages().isEmpty())) {
            if (checkImagesNames(request.getImagesNames(user.getId()))) {
                List<AssetBundleImage> images = assetBundleImageRepository.findImagesByAssetBundle(assetBundle)
                        .orElseThrow(() -> new FieldNotFoundException(HttpStatus.NOT_FOUND, "Не удалось найти изображения бандла!"));
                deleteImagesDB(images);
                saveBundleImages(request, user.getId());
                saveImagesDB(assetBundle, request.getImagesNames(user.getId()));
                imagesChanged = true;
            }
        }

        if (changed) {
            assetBundleInfoRepository.save(assetBundle);
        }
        if (!changed && !categoryChanged && !imagesChanged) {
            throw new IllegalArgumentException("Данные бандла не обновлены!");
        }
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
            case PRICE_ASC -> pageable = PageRequest.of(page, size, Sort.by("price").ascending());
            case PRICE_DESC -> pageable = PageRequest.of(page, size, Sort.by("price").descending());
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

    public void saveBundleFile(AddAssetBundleRequest request, Long userId) throws IOException {
        MultipartFile bundleFile = request.getBundleFile();
        if (bundleFile == null) {
            throw new IllegalArgumentException("Отсутвует бандл!");
        }

        String bundleExt = StringUtils.getFilenameExtension(bundleFile.getOriginalFilename());
        if (!BUNDLE_EXTENSIONS.contains(bundleExt.toLowerCase())) {
            throw new IllegalArgumentException("Недопустимый формат бандла!");
        }

        Path targetPath = Path.of(uploadDir, request.getFilename(userId)).toAbsolutePath();
        Files.copy(bundleFile.getInputStream(), targetPath);
    }

    public void saveBundleImages(AddAssetBundleRequest request, Long userId) throws IOException {
        if (request.getImages() == null) {
            throw new IllegalArgumentException("Отсутствуют изображения!");
        }

        for (MultipartFile current : request.getImages()) {
            if (!IMAGE_CONTENT_TYPES.contains(current.getContentType())) {
                throw new IllegalArgumentException("Недопустимый формат изображения!");
            }
            Path targetPath = Path.of(thumbnailsDir, userId + "_" + StringUtils.cleanPath(current.getOriginalFilename()))
                    .toAbsolutePath();
            Files.copy(current.getInputStream(), targetPath);
        }
    }

    @Transactional
    public void deleteBundle(AssetBundleInfo bundle) {
        List<AssetBundleImage> images = assetBundleImageRepository.findImagesByAssetBundle(bundle).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Изображения не найдены!"));

        try {
            deleteImagesDB(images);

            Path filePath = Paths.get(uploadDir, bundle.getFilename());
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

    private void saveImagesDB(AssetBundleInfo assetBundle, List<String> imageList) {
        for (String current : imageList) {
            AssetBundleImage assetBundleImage = new AssetBundleImage();
            assetBundleImage.setAssetBundle(assetBundle);
            assetBundleImage.setPath(current);
            assetBundleImageRepository.save(assetBundleImage);
        }
    }

    private void deleteImagesDB( List<AssetBundleImage> images) throws IOException {
        Path filePath;
        for (AssetBundleImage image : images) {
            filePath = Paths.get(thumbnailsDir, image.getPath());
            Files.deleteIfExists(filePath);
            assetBundleImageRepository.delete(image);
        }
        assetBundleImageRepository.flush();
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

    private List<String> listFilesInDirectory(String directoryPath) {
        File dir = new File(directoryPath);

        if (!dir.exists() || !dir.isDirectory()) {
            return List.of();
        }

        File[] files = dir.listFiles();

        if (files == null) {
            return List.of();
        }

        return Arrays.stream(files)
                .filter(File::isFile)
                .map(File::getName)
                .toList();
    }
    private boolean checkBundleNames(String newName) {
        List<String> bundleNames = listFilesInDirectory(uploadDir);
        for (String current : bundleNames) {
            if (newName.equals(current)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkImagesNames(List<String> newImagesNames) {
        List<String> imageNames = listFilesInDirectory(thumbnailsDir);
        for (String newCurrent : newImagesNames) {
            for (String current : imageNames) {
                if (newCurrent.equals(current)) {
                    return false;
                }
            }
        }
        return true;
    }
}