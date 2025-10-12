package com.usachevsergey.AssetBundleServer.database.tables;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "user_bundle")
@Data
public class UserBundle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    @ManyToOne
    private AssetBundleInfo assetBundle;
    private Instant purchasedAt = Instant.now();
}
