package com.usachevsergey.AssetBundleServer.database.repositories;

import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import com.usachevsergey.AssetBundleServer.database.tables.User;
import com.usachevsergey.AssetBundleServer.database.tables.UserBundle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserBundleRepository extends JpaRepository<UserBundle, Long> {
    @Query("SELECT ub.assetBundle FROM UserBundle ub WHERE ub.user = :user")
    List<AssetBundleInfo> findBundlesByUser(@Param("user") User user);
    boolean existsByUserAndAssetBundle(User user, AssetBundleInfo assetBundle);
}
