package com.usachevsergey.AssetBundleServer.controllers;

import com.usachevsergey.AssetBundleServer.annotations.EmailVerifiedOnly;
import com.usachevsergey.AssetBundleServer.database.dto.CommentDTO;
import com.usachevsergey.AssetBundleServer.database.services.AssetBundleService;
import com.usachevsergey.AssetBundleServer.database.services.CommentService;
import com.usachevsergey.AssetBundleServer.database.services.UserService;
import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import com.usachevsergey.AssetBundleServer.database.tables.Comment;
import com.usachevsergey.AssetBundleServer.database.tables.User;
import com.usachevsergey.AssetBundleServer.security.authorization.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class CommentController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private UserService userService;
    @Autowired
    private AssetBundleService assetBundleService;

    @EmailVerifiedOnly
    @PostMapping("/api/secured/bundles/{bundleId}/addComment")
    public ResponseEntity<?> addComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @PathVariable Long bundleId,
                                        @RequestBody Map<String, String> body) {
        String text = body.get("text");
        String parentIdStr = body.get("parentId");
        Long parentId = parentIdStr != null ? Long.parseLong(parentIdStr) : null;

        if (text == null || text.isBlank()) {
            throw new IllegalStateException("Комментарий не может быть пустым!");
        }

        User user = userService.getUser(userDetails.getUsername());
        AssetBundleInfo assetBundleInfo = assetBundleService.getBundle(bundleId);

        commentService.addComment(user, assetBundleInfo, text, parentId);
        return ResponseEntity.ok(Map.of("message", "Комментарий добавлен!"));
    }

    @GetMapping("/api/public/bundles/{bundleId}/comments")
    public ResponseEntity<?> getComments(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @PathVariable Long bundleId) {
        User currentUser = null;

        if (userDetails != null) {
            currentUser = userService.getUser(userDetails.getUsername());
        }

        List<CommentDTO> comments = commentService.getComments(assetBundleService.getBundle(bundleId), currentUser);
        return ResponseEntity.ok(comments);
    }

    @EmailVerifiedOnly
    @PutMapping("/api/secured/comments/editComment/{commentId}")
    public ResponseEntity<?> editComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @PathVariable Long commentId,
                                         @RequestBody Map<String, String> body) {
        String newText = body.get("text");
        if (newText == null || newText.isBlank()) {
            throw new IllegalStateException("Новый комментарий не может быть пустым!");
        }

        User user = userService.getUser(userDetails.getUsername());
        Comment comment = commentService.getCommentById(commentId);
        commentService.editComment(user, comment, newText);

        return ResponseEntity.ok(Map.of("message", "Комментарий отредактирован"));
    }

    @EmailVerifiedOnly
    @DeleteMapping("/api/secured/comments/delete/{commentId}")
    public ResponseEntity<?> deleteComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @PathVariable Long commentId) {
        User user = userService.getUser(userDetails.getUsername());
        Comment comment = commentService.getCommentById(commentId);
        commentService.deleteComment(user, comment);

        return ResponseEntity.ok(Map.of("message", "Комментарий удален!"));
    }
}
