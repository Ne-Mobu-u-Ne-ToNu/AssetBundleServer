package com.usachevsergey.AssetBundleServer.database.repositories;

import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleImage;
import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetBundleImageRepository extends JpaRepository<AssetBundleImage, Long> {
    Optional<List<AssetBundleImage>> findImagesByAssetBundle(AssetBundleInfo assetBundle);
}
