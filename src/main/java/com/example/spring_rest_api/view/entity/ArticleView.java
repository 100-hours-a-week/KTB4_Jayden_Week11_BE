package com.example.spring_rest_api.view.entity;

import com.example.spring_rest_api.article.entity.Article;
import com.example.spring_rest_api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "article_views")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long articleViewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime updatedAt;

    public static ArticleView init(Article article, User user) {
        ArticleView articleView = new ArticleView();
        articleView.article = article;
        articleView.user = user;
        articleView.updatedAt = LocalDateTime.now();
        return articleView;
    }

    public void update() {
        this.updatedAt = LocalDateTime.now();
    }
}
