package com.usachevsergey.AssetBundleServer.database.repositories;

import com.usachevsergey.AssetBundleServer.database.tables.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNull();
    List<Category> findByParent(Category parent);
}
