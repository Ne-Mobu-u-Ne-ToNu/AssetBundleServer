package com.usachevsergey.AssetBundleServer.database.services;

import com.usachevsergey.AssetBundleServer.database.dto.CommentDTO;
import com.usachevsergey.AssetBundleServer.database.repositories.CommentRepository;
import com.usachevsergey.AssetBundleServer.database.tables.AssetBundleInfo;
import com.usachevsergey.AssetBundleServer.database.tables.Comment;
import com.usachevsergey.AssetBundleServer.database.tables.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    public void addComment(User user, AssetBundleInfo bundle, String text, Long parentId) {
        Comment comment = new Comment();
        comment.setAuthor(user);
        comment.setBundle(bundle);
        comment.setText(text);

        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Родительский комментарий не найден!"));
            comment.setParentComment(parent);
        }

        commentRepository.save(comment);
    }

    @Transactional
    public Map<?,?> getComments(AssetBundleInfo bundle, User currentUser) {
        List<Comment> rootComments = commentRepository.findByBundleAndParentCommentIsNullOrderByCreatedAtAsc(bundle);

        List<CommentDTO> allComments = rootComments.stream()
                .map(c -> mapToDto(c, bundle, currentUser))
                .toList();

        Long count = commentRepository.countByBundle(bundle);

        return Map.of("comments", allComments,
                "count", count);
    }

    public CommentDTO mapToDto(Comment comment, AssetBundleInfo bundle, User currentUser) {
        CommentDTO result = new CommentDTO();
        result.setId(comment.getId());
        result.setText(comment.getText());
        result.setAuthorName(comment.getAuthor().getUsername());
        result.setCreatedAt(comment.getCreatedAt());
        result.setEdited(comment.isEdited());
        result.setAuthor(comment.getAuthor().equals(currentUser));
        result.setBundleAuthor(comment.getAuthor().getId().equals(bundle.getUploadedBy().getId()));
        result.setLikes(comment.getLikedBy().size());
        result.setLikedByUser(comment.getLikedBy().contains(currentUser));

        List<Comment> replies = commentRepository.findByParentCommentOrderByCreatedAtAsc(comment);
        for (Comment reply : replies) {
            result.getReplies().add(mapToDto(reply, bundle, currentUser));
        }

        return result;
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Не удалось найти комментарий!"));
    }

    public void editComment(User user, Comment comment, String newText) {
        if (!comment.getAuthor().equals(user)) {
            throw new RuntimeException("Нет прав на редактирование этого комментария!");
        }

        comment.setText(newText);
        comment.setCreatedAt(Instant.now());
        comment.setEdited(true);
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(User user, Comment comment) {
        if (!comment.getAuthor().equals(user)) {
            throw new RuntimeException("Нет прав на удаление этого комментария!");
        }

        deleteReplies(comment);
        commentRepository.delete(comment);
    }

    @Transactional
    private void deleteReplies(Comment comment) {
        List<Comment> replies = commentRepository.findByParentCommentOrderByCreatedAtAsc(comment);
        for (Comment reply: replies) {
            deleteReplies(reply);
            commentRepository.delete(reply);
        }
    }

    public boolean toggleLike(Comment comment, User user) {
        boolean added;
        if (comment.getLikedBy().contains(user)) {
            comment.getLikedBy().remove(user);
            added = false;
        } else {
            comment.getLikedBy().add(user);
            added = true;
        }

        commentRepository.save(comment);
        return added;
    }
}