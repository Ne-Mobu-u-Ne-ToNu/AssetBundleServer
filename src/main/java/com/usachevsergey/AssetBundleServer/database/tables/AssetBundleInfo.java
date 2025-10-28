package com.usachevsergey.AssetBundleServer.database.tables;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "asset_bundle_info")
@Data
public class AssetBundleInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String filename;
    @Column(name = "uploaded_at", updatable = false)
    private Date uploadedAt;
    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User uploadedBy;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = new Date();
        }
    }
}
