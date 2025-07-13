package com.usachevsergey.AssetBundleServer.database.repositories;

import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetBundleImageRepository extends JpaRepository<AssetBundleImage, Long> {

}
