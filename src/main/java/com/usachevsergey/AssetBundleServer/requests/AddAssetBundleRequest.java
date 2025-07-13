package com.usachevsergey.AssetBundleServer.requests;

import com.usachevsergey.AssetBundleServer.database.tables.User;
import lombok.*;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class AddAssetBundleRequest {
    private String name;
    private String description;
    @Setter(AccessLevel.NONE)
    private String filename;
    private Date uploadedAt;
    @Setter(AccessLevel.NONE)
    private List<String> imagesNames;
    private MultipartFile bundleFile;
    private List<MultipartFile> images;

    public String getFilename() {
        if (bundleFile != null) {
            return StringUtils.cleanPath(bundleFile.getOriginalFilename());
        } else {
            return null;
        }
    }

    public List<String> getImagesNames() {
        if (images != null) {
            List<String> result = new ArrayList<>();
            for (MultipartFile file : images) {
                result.add(StringUtils.cleanPath(file.getOriginalFilename()));
            }
            return result;
        } else {
            return null;
        }
    }
}
