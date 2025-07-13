package com.usachevsergey.AssetBundleServer.database.services;

import com.usachevsergey.AssetBundleServer.database.repositories.AssetBundleImageRepository;
import com.usachevsergey.AssetBundleServer.database.repositories.AssetBundleInfoRepository;
import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleImage;
import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import com.usachevsergey.AssetBundleServer.requests.AddAssetBundleRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private void saveImages(AssetBundleInfo assetBundle, List<String> imageList) {
        for (String current : imageList) {
            AssetBundleImage assetBundleImage = new AssetBundleImage();
            assetBundleImage.setAssetBundle(assetBundle);
            assetBundleImage.setPath(current);
            assetBundleImageRepository.save(assetBundleImage);
        }
    }
}
