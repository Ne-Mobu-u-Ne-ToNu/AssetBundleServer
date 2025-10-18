package com.usachevsergey.AssetBundleServer.database.dto;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommentDTO {
    private Long id;
    private String text;
    private String authorName;
    private Instant createdAt;
    private boolean isAuthor;
    private boolean isEdited;
    private boolean isBundleAuthor;
    private int likes;
    private boolean likedByUser;
    private List<CommentDTO> replies = new ArrayList<>();
}
