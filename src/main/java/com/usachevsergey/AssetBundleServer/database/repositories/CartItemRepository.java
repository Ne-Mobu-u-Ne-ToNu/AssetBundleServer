package com.usachevsergey.AssetBundleServer.database.repositories;

import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import com.usachevsergey.AssetBundleServer.database.tables.CartItem;
import com.usachevsergey.AssetBundleServer.database.tables.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    boolean existsByUserAndAssetBundle(User user, AssetBundleInfo bundle);
    void deleteByUserAndAssetBundle(User user, AssetBundleInfo bundle);
}
