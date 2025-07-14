package com.usachevsergey.AssetBundleServer.database.services;

import com.usachevsergey.AssetBundleServer.database.dto.AssetBundleDTO;
import com.usachevsergey.AssetBundleServer.database.repositories.AssetBundleImageRepository;
import com.usachevsergey.AssetBundleServer.database.repositories.AssetBundleInfoRepository;
import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleImage;
import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import com.usachevsergey.AssetBundleServer.requests.AddAssetBundleRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AssetBundleService {

    @Autowired
    private AssetBundleInfoRepository assetBundleInfoRepository;
    @Autowired
    private AssetBundleImageRepository assetBundleImageRepository;
    @Autowired
    private UserService userService;

    public void uploadAssetBundle(AddAssetBundleRequest request, String username) {
        AssetBundleInfo assetBundle = new AssetBundleInfo();
        assetBundle.setUploadedBy(userService.getUser(username));
        assetBundle.setName(request.getName());
        assetBundle.setDescription(request.getDescription());
        assetBundle.setFilename(request.getFilename());
        assetBundleInfoRepository.save(assetBundle);

        saveImages(assetBundle, request.getImagesNames());
    }

    public List<AssetBundleDTO> getAllBundles() {
        List<AssetBundleInfo> allBundles = assetBundleInfoRepository.findAll();

        return allBundles.stream().map(bundle -> {
            List<AssetBundleImage> images = assetBundleImageRepository.findImagesByAssetBundle(bundle).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Изображения не найдены!"));
            List<String> paths = images.stream()
                    .map(AssetBundleImage::getPath)
                    .toList();

            return new AssetBundleDTO(
                    bundle.getId(),
                    bundle.getName(),
                    bundle.getDescription(),
                    bundle.getFilename(),
                    bundle.getUploadedAt(),
                    bundle.getUploadedBy(),
                    paths
            );
        }).toList();
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
