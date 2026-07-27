package com.example.spring_rest_api.image.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "image_files")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String filePath = "/public/images/default.jpg";

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FileCategory fileCategory;

    private Long uploaderId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ImageStatus imageStatus;

    private ImageFile(String filePath, FileCategory fileCategory, Long uploaderId, ImageStatus imageStatus) {
        this.filePath = filePath;
        this.fileCategory = fileCategory;
        this.uploaderId = uploaderId;
        this.imageStatus = imageStatus;
    }

    public static ImageFile createProfileImage(String filePath , Long uploaderId) {
        return new ImageFile(filePath, FileCategory.PROFILE_IMAGE, uploaderId, ImageStatus.TEMP);
    }

    public static ImageFile createArticleImage(String filePath , Long uploaderId) {
        return new ImageFile(filePath, FileCategory.ARTICLE_ATTACHMENT, uploaderId, ImageStatus.TEMP);
    }

    public void changeAttachedStatus() {
        this.imageStatus = ImageStatus.ATTACHED;
    }

    public void changeTempStatus() {
        this.imageStatus = ImageStatus.TEMP;
    }

    public void changeUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }
}
