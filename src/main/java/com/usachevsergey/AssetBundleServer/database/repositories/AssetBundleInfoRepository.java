package com.usachevsergey.AssetBundleServer.database.repositories;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetBundleInfoRepository extends JpaRepository<AssetBundleInfo, Long> {
    // name не задан
    List<AssetBundleInfo> findAllByOrderByNameAsc();
    List<AssetBundleInfo> findAllByOrderByUploadedAtAsc();
    List<AssetBundleInfo> findAllByOrderByUploadedAtDesc();

    // name задан
    List<AssetBundleInfo> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
    List<AssetBundleInfo> findByNameContainingIgnoreCaseOrderByUploadedAtAsc(String name);
    List<AssetBundleInfo> findByNameContainingIgnoreCaseOrderByUploadedAtDesc(String name);
}
