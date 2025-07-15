package com.usachevsergey.AssetBundleServer.database.repositories;

import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;



public interface AssetBundleInfoRepository extends JpaRepository<AssetBundleInfo, Long> {
    Page<AssetBundleInfo> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
