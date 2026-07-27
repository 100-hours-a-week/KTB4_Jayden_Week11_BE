package com.example.spring_rest_api.article.entity;

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
@Table(name = "article_update_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleUpdateHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long articleHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    private String title;
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<String> contentImages = new ArrayList<>();

    private LocalDateTime createdAt;

    public static ArticleUpdateHistory create(Article article, String title, String content, List<String> contentImages) {
        ArticleUpdateHistory history = new ArticleUpdateHistory();
        history.article = article;
        history.title = title;
        history.content = content;
        history.contentImages = contentImages == null ? new ArrayList<>() : new ArrayList<>(contentImages);
        history.createdAt = LocalDateTime.now();
        return history;
    }
}
