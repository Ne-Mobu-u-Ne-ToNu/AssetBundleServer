package com.usachevsergey.AssetBundleServer.controllers;

import com.usachevsergey.AssetBundleServer.annotations.EmailVerifiedOnly;
import com.usachevsergey.AssetBundleServer.database.repositories.CartItemRepository;
import com.usachevsergey.AssetBundleServer.database.services.AssetBundleService;
import com.usachevsergey.AssetBundleServer.database.services.CartItemService;
import com.usachevsergey.AssetBundleServer.database.services.UserService;
import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import com.usachevsergey.AssetBundleServer.database.tables.User;
import com.usachevsergey.AssetBundleServer.security.authorization.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/secured/cart")
@EmailVerifiedOnly
@PreAuthorize("hasAuthority('USER')")
public class CartController {

    @Autowired
    private CartItemService cartItemService;
    @Autowired
    private UserService userService;
    @Autowired
    private AssetBundleService assetBundleService;
    @Autowired
    private CartItemRepository cartItemRepository;


    @GetMapping("/check/{bundleId}")
    public ResponseEntity<?> checkIsInCart(@PathVariable Long bundleId,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getUser(userDetails.getUsername());
        AssetBundleInfo assetBundleInfo = assetBundleService.getBundle(bundleId);

        boolean isInCart = cartItemRepository.existsByUserAndAssetBundle(user, assetBundleInfo);

        return ResponseEntity.ok(Map.of("inCart", isInCart));
    }
    @PostMapping("/add/{bundleId}")
    public ResponseEntity<?> addToCart(@PathVariable Long bundleId,
                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getUser(userDetails.getUsername());
        AssetBundleInfo assetBundleInfo = assetBundleService.getBundle(bundleId);

        cartItemService.addToCart(user, assetBundleInfo);

        return ResponseEntity.ok(Map.of("message", "Бандл добавлен в корзину"));
    }

    @GetMapping
    public ResponseEntity<?> getCartItems(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = userService.getUser(userDetails.getUsername());

        return ResponseEntity.ok(Map.of("cartItems", cartItemService.getUserCart(user)));
    }

    @DeleteMapping("/remove/{bundleId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long bundleId,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = userService.getUser(userDetails.getUsername());
        AssetBundleInfo assetBundleInfo = assetBundleService.getBundle(bundleId);

        cartItemService.removeFromCart(user, assetBundleInfo);

        return ResponseEntity.ok(Map.of("message", "Бандл удален из корзины!"));
    }
}
