package com.usachevsergey.AssetBundleServer.database.services;

import com.usachevsergey.AssetBundleServer.database.dto.AssetBundleDTO;
import com.usachevsergey.AssetBundleServer.database.repositories.CartItemRepository;
import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import com.usachevsergey.AssetBundleServer.database.tables.CartItem;
import com.usachevsergey.AssetBundleServer.database.tables.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartItemService {
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private AssetBundleService assetBundleService;

    public void addToCart(User user, AssetBundleInfo bundle) {
        if (cartItemRepository.existsByUserAndAssetBundle(user, bundle)) {
            throw new IllegalStateException("Бандл уже в корзине!");
        }

        CartItem cartItem = new CartItem();
        cartItem.setUser(user);
        cartItem.setAssetBundle(bundle);

        cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeFromCart(User user, AssetBundleInfo bundle) {
        cartItemRepository.deleteByUserAndAssetBundle(user, bundle);
    }

    public List<AssetBundleDTO> getUserCart(User user) {
        List<CartItem> items = cartItemRepository.findByUser(user);

        return items.stream().map(item ->
                assetBundleService.createDTOFromInfo(item.getAssetBundle())).toList();
    }
}
