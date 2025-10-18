package com.usachevsergey.AssetBundleServer.database.repositories;

import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import com.usachevsergey.AssetBundleServer.database.tables.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBundleAndParentCommentIsNullOrderByCreatedAtAsc(AssetBundleInfo bundle);
    List<Comment> findByParentCommentOrderByCreatedAtAsc(Comment parent);
    Long countByBundle(AssetBundleInfo bundle);
}
