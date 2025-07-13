package com.usachevsergey.AssetBundleServer.database.repositories;

import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetBundleInfoRepository extends JpaRepository<AssetBundleInfo, Long> {

}
