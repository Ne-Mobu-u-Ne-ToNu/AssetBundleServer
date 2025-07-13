package com.usachevsergey.AssetBundleServer.database.tables;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "asset_bundle_image")
@Data
public class AssetBundleImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String path;
    @ManyToOne
    @JoinColumn(name = "asset_bundle_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private AssetBundleInfo assetBundle;
}
