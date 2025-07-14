package com.usachevsergey.AssetBundleServer.database.dto;

import com.usachevsergey.AssetBundleServer.database.tables.User;
import lombok.AllArgsConstructor;
import lombok.Data;

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
    private List<String> imagePaths;
}
