package com.usachevsergey.AssetBundleServer.database.dto;

import com.usachevsergey.AssetBundleServer.database.tables.Category;
import com.usachevsergey.AssetBundleServer.database.tables.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
public class AssetBundleDTO {
    private Long id;
    private String name;
    private String description;
    private String filename;
    private Date uploadedAt;
    private User uploadedBy;
    private BigDecimal price;
    private List<String> imagePaths;
    private List<Category> categories;
}
