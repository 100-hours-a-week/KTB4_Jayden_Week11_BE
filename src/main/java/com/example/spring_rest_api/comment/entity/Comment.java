package com.example.spring_rest_api.comment.entity;

import com.example.spring_rest_api.article.entity.Article;
import com.example.spring_rest_api.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;
    private String commentText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static Comment create(Article article, User user, Comment parentComment, String commentText) {
        Comment comment = new  Comment();
        comment.article = article;
        comment.user = user;
        comment.parentComment = parentComment;
        comment.commentText = commentText;
        comment.createdAt = LocalDateTime.now();
        comment.updatedAt = null;
        comment.deletedAt = null;
        return comment;
    }

    public Comment update(String commentText) {
        this.commentText = commentText;
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    public Comment delete() {
        this.deletedAt = LocalDateTime.now();
        return this;
    }
}
