package com.example.spring_rest_api.article.entity;

import com.example.spring_rest_api.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "articles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long articleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String title;
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<String> contentImages = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private boolean isArticleHidden;

    @OneToOne(mappedBy = "article", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private ArticleStat articleStat;

    public static Article create(User user, String title, String content, List<String> contentImages) {
        Article article = new Article();
        article.user = user;
        article.title = title;
        article.content = content;
        article.contentImages = contentImages == null ? new ArrayList<>() : new ArrayList<>(contentImages);
        article.createdAt = LocalDateTime.now();
        article.updatedAt = null;
        article.deletedAt = null;
        article.isArticleHidden = false;

        article.articleStat = ArticleStat.create(article);

        return article;
    }

    public Article update(String title, String content, List<String> contentImages) {
        this.title = title;
        this.content = content;
        this.contentImages = contentImages == null ? new ArrayList<>() : contentImages;
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    public Article delete() {
        this.deletedAt = LocalDateTime.now();
        return this;
    }

    public void hideArticle() {
        this.isArticleHidden = true;
    }
}