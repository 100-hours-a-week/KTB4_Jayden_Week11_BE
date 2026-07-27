package com.example.spring_rest_api.article.entity;

import com.example.spring_rest_api.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "article_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    private String reason;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    public static Report create(Article article, User user, ReportType reportType, String reason) {
        Report report = new Report();
        report.article = article;
        report.user = user;
        report.reportType = reportType;
        report.reason = reason;
        report.createdAt = LocalDateTime.now();
        report.status = ReportStatus.waiting;
        return report;
    }

    public void reject() {
        this.status = ReportStatus.rejected;
    }

    public void complete() {
        this.status = ReportStatus.completed;
    }
}
