package com.usachevsergey.AssetBundleServer.database.repositories;

import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import com.usachevsergey.AssetBundleServer.database.tables.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByParentComment(Comment parent);
    Long countByBundle(AssetBundleInfo bundle);
    Long countByBundleAndParentCommentIsNull(AssetBundleInfo bundle);
    Long countByParentComment(Comment parent);

    Page<Comment> findByBundleAndParentCommentIsNull(AssetBundleInfo bundle, Pageable pageable);
    Page<Comment> findByParentComment(Comment parent, Pageable pageable);

    @Query("""
        SELECT c FROM Comment c
        LEFT JOIN c.likedBy lb
        WHERE c.bundle = :bundle AND c.parentComment IS NULL
        GROUP BY c
        ORDER BY COUNT(lb) ASC
        """)
    Page<Comment> findByBundleAndParentCommentIsNullOrderByLikesAsc(
            @Param("bundle") AssetBundleInfo bundle,
            Pageable pageable
    );

    @Query("""
        SELECT c FROM Comment c
        LEFT JOIN c.likedBy lb
        WHERE c.bundle = :bundle AND c.parentComment IS NULL
        GROUP BY c
        ORDER BY COUNT(lb) DESC
        """)
    Page<Comment> findByBundleAndParentCommentIsNullOrderByLikesDesc(
            @Param("bundle") AssetBundleInfo bundle,
            Pageable pageable
            );

    @Query("""
        SELECT c FROM Comment c
        LEFT JOIN c.likedBy lb
        WHERE c.parentComment = :parent
        GROUP BY c
        ORDER BY COUNT(lb) ASC
        """)
    Page<Comment> findByParentCommentOrderByLikesAsc(
            @Param("parent") Comment parent,
            Pageable pageable
    );

    @Query("""
        SELECT c FROM Comment c
        LEFT JOIN c.likedBy lb
        WHERE c.parentComment = :parent
        GROUP BY c
        ORDER BY COUNT(lb) DESC
        """)
    Page<Comment> findByParentCommentOrderByLikesDesc(
            @Param("parent") Comment parent,
            Pageable pageable
    );
}
