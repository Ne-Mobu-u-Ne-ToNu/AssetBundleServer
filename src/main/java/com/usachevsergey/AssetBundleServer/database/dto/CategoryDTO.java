package com.usachevsergey.AssetBundleServer.database.dto;

import lombok.Data;

import java.util.List;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private List<CategoryDTO> subcategories;
}
