package com.usachevsergey.AssetBundleServer.database.repositories;

import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import com.usachevsergey.AssetBundleServer.database.tables.BundleCategory;
import com.usachevsergey.AssetBundleServer.database.tables.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BundleCategoryRepository extends JpaRepository<BundleCategory, Long> {
    @Query("""
       SELECT c 
       FROM BundleCategory bc 
       JOIN bc.category c 
       LEFT JOIN FETCH c.parent 
       WHERE bc.assetBundle = :assetBundle
       ORDER BY c.name ASC
    """)
    List<Category> findByAssetBundle(AssetBundleInfo assetBundle);

    @Query("""
    SELECT DISTINCT ab
    FROM AssetBundleInfo ab
    JOIN BundleCategory bc ON bc.assetBundle = ab
    JOIN bc.category c
    WHERE (:name IS NULL OR LOWER(ab.name) LIKE LOWER(CONCAT('%', :name, '%')))
      AND c.id IN :categoryIds
""")
    Page<AssetBundleInfo> findByNameAndCategoryIds(
            @Param("name") String name,
            @Param("categoryIds") List<Long> categoryIds,
            Pageable pageable
    );
}
