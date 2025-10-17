package com.usachevsergey.AssetBundleServer.database.tables;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment")
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User author;

    @ManyToOne(optional = false)
    private AssetBundleInfo bundle;

    @ManyToOne
    private Comment parentComment;

    private String text;

    private boolean isEdited = false;

    private Instant createdAt = Instant.now();

    @Transient
    private List<Comment> replies = new ArrayList<>();
}
