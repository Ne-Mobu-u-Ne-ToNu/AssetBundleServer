package com.usachevsergey.AssetBundleServer.database.repositories;

import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssetBundleInfoRepository extends JpaRepository<AssetBundleInfo, Long> {
    @Query("SELECT a FROM AssetBundleInfo a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<AssetBundleInfo> searchByName(@Param("name") String name);
}
