package com.usachevsergey.AssetBundleServer.requests;

import com.usachevsergey.AssetBundleServer.database.tables.Category;
import lombok.*;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class AddAssetBundleRequest {
    private String name;
    private String description;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private String filename;
    private Date uploadedAt;
    private BigDecimal price;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private List<String> imagesNames;
    private MultipartFile bundleFile;
    private List<MultipartFile> images;
    private List<Long> categoryIds;

    public String getFilename(Long userId) {
        if (bundleFile != null) {
            return StringUtils.cleanPath(userId + "_" + bundleFile.getOriginalFilename());
        } else {
            return null;
        }
    }

    public List<String> getImagesNames(Long userId) {
        if (images != null) {
            List<String> result = new ArrayList<>();
            for (MultipartFile file : images) {
                result.add(StringUtils.cleanPath(userId + "_" + file.getOriginalFilename()));
            }
            return result;
        } else {
            return null;
        }
    }
}
