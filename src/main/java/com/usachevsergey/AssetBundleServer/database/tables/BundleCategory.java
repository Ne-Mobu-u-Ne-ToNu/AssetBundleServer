package com.usachevsergey.AssetBundleServer.database.tables;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "bundle_category")
@Data
public class BundleCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private AssetBundleInfo assetBundle;
    @ManyToOne
    private Category category;
}
