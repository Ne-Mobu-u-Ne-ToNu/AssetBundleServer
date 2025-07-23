package com.usachevsergey.AssetBundleServer.database.services;

import com.usachevsergey.AssetBundleServer.database.dto.AssetBundleDTO;
import com.usachevsergey.AssetBundleServer.database.enumerations.SortOption;
import com.usachevsergey.AssetBundleServer.database.repositories.AssetBundleImageRepository;
import com.usachevsergey.AssetBundleServer.database.repositories.AssetBundleInfoRepository;
import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleImage;
import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import com.usachevsergey.AssetBundleServer.database.tables.User;
import com.usachevsergey.AssetBundleServer.requests.AddAssetBundleRequest;
import com.usachevsergey.AssetBundleServer.security.authorization.UserInputValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@Service
public class AssetBundleService {

    @Autowired
    private AssetBundleInfoRepository assetBundleInfoRepository;
    @Autowired
    private AssetBundleImageRepository assetBundleImageRepository;

    public void uploadAssetBundle(AddAssetBundleRequest request, User user) {
        AssetBundleInfo assetBundle = new AssetBundleInfo();
        assetBundle.setUploadedBy(user);
        assetBundle.setName(request.getName());
        assetBundle.setDescription(request.getDescription());
        assetBundle.setFilename(request.getFilename(user.getId()));
        assetBundleInfoRepository.save(assetBundle);

        saveImages(assetBundle, request.getImagesNames(user.getId()));
    }

    public Map<String, ?> getBundlesBySearch(String name, String sort,
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

        if (UserInputValidator.isNullOrEmpty(name)) {
            bundlesPage = assetBundleInfoRepository.findAll(pageable);
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
        AssetBundleInfo assetBundleInfo = assetBundleInfoRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Не удалось найти бандл!"));

        return createDTOFromInfo(assetBundleInfo);
    }

    private List<AssetBundleDTO> createDTOFromInfo(List<AssetBundleInfo> info) {
        return info.stream().map(this::createDTOFromInfo).toList();
    }

    private AssetBundleDTO createDTOFromInfo(AssetBundleInfo info) {
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
                paths
        );
    }

    private void saveImages(AssetBundleInfo assetBundle, List<String> imageList) {
        for (String current : imageList) {
            AssetBundleImage assetBundleImage = new AssetBundleImage();
            assetBundleImage.setAssetBundle(assetBundle);
            assetBundleImage.setPath(current);
            assetBundleImageRepository.save(assetBundleImage);
        }
    }
}
