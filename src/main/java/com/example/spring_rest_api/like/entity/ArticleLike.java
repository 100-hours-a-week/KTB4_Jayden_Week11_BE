package com.example.spring_rest_api.like.entity;

import com.example.spring_rest_api.article.entity.Article;
import com.example.spring_rest_api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "article_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long articleLikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt;

    public static ArticleLike create(Article article, User user) {
        ArticleLike articleLike = new ArticleLike();
        articleLike.article = article;
        articleLike.user = user;
        articleLike.createdAt = LocalDateTime.now();
        return articleLike;
    }
}
