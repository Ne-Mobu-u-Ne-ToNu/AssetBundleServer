package com.usachevsergey.AssetBundleServer.controllers;

import com.usachevsergey.AssetBundleServer.annotations.EmailVerifiedOnly;
import com.usachevsergey.AssetBundleServer.database.repositories.AssetBundleInfoRepository;
import com.usachevsergey.AssetBundleServer.database.repositories.CartItemRepository;
import com.usachevsergey.AssetBundleServer.database.repositories.UserRepository;
import com.usachevsergey.AssetBundleServer.database.services.AssetBundleService;
import com.usachevsergey.AssetBundleServer.database.services.CartItemService;
import com.usachevsergey.AssetBundleServer.database.services.UserService;
import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import com.usachevsergey.AssetBundleServer.database.tables.User;
import com.usachevsergey.AssetBundleServer.security.authorization.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    private final String unauthorizedMessage = "Пользователь не авторизован";


    @PostMapping("/add/{bundleId}")
    public ResponseEntity<?> addToCart(@PathVariable Long bundleId,
                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", unauthorizedMessage));
        }

        User user = userService.getUser(userDetails.getUsername());
        AssetBundleInfo assetBundleInfo = assetBundleService.getBundle(bundleId);

        cartItemService.addToCart(user, assetBundleInfo);

        return ResponseEntity.ok(Map.of("message", "Бандл добавлен в корзину"));
    }

    @GetMapping
    public ResponseEntity<?> getCartItems(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", unauthorizedMessage));
        }

        User user = userService.getUser(userDetails.getUsername());

        return ResponseEntity.ok(Map.of("cartItems", cartItemService.getUserCart(user)));
    }

    @DeleteMapping("/remove/{bundleId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long bundleId,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", unauthorizedMessage));
        }

        User user = userService.getUser(userDetails.getUsername());
        AssetBundleInfo assetBundleInfo = assetBundleService.getBundle(bundleId);

        cartItemRepository.deleteByUserAndAssetBundle(user, assetBundleInfo);

        return ResponseEntity.ok(Map.of("message", "Бандл удален из корзины!"));
    }
}
