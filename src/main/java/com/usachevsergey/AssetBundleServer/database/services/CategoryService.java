package com.usachevsergey.AssetBundleServer.database.services;

import com.usachevsergey.AssetBundleServer.database.dto.CategoryDTO;
import com.usachevsergey.AssetBundleServer.database.repositories.BundleCategoryRepository;
import com.usachevsergey.AssetBundleServer.database.repositories.CategoryRepository;
import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import com.usachevsergey.AssetBundleServer.database.tables.BundleCategory;
import com.usachevsergey.AssetBundleServer.database.tables.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BundleCategoryRepository bundleCategoryRepository;

    public List<CategoryDTO> getAllCategoryTree() {
        List<Category> root = categoryRepository.findByParentIsNull();

        return root.stream()
                .map(this::toDTO)
                .toList();
    }

    public void saveBundleCategories(List<Long> categoryIds, AssetBundleInfo bundle) {
        for (Long categoryId : categoryIds) {
            BundleCategory bundleCategory = new BundleCategory();
            bundleCategory.setCategory(categoryRepository.findById(categoryId).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Не удалось найти категорию!")
            ));
            bundleCategory.setAssetBundle(bundle);
            bundleCategoryRepository.save(bundleCategory);
        }
    }

    @Transactional
    public void editBundleCategories(List<Long> categoryIds, AssetBundleInfo bundle) {
        bundleCategoryRepository.deleteByAssetBundle(bundle);
        bundleCategoryRepository.flush();

        saveBundleCategories(categoryIds, bundle);
    }

    private CategoryDTO toDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSubcategories(
                categoryRepository.findByParent(category).stream()
                        .map(this::toDTO)
                        .toList()
        );

        return dto;
    }
}
