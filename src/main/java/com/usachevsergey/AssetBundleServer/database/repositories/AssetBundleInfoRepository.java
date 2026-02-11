package com.usachevsergey.AssetBundleServer.database.repositories;

import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import com.usachevsergey.AssetBundleServer.database.tables.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface AssetBundleInfoRepository extends JpaRepository<AssetBundleInfo, Long> {
    Page<AssetBundleInfo> findByNameContainingIgnoreCase(String name, Pageable pageable);
    List<AssetBundleInfo> findByUploadedBy(User user);
    Boolean existsByUploadedBy(User user);
}
