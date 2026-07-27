package com.example.spring_rest_api.user.entity;

import com.example.spring_rest_api.image.entity.ImageFile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String email;
    private String password;
    private String nickname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_file_id")
    private ImageFile profileImage;

    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private LocalDateTime informationUpdatedAt;
    private LocalDateTime passwordUpdatedAt;

    public static User create(String email, String password, String nickname, ImageFile profileImage) {
        User user = new User();
        user.email = email;
        user.password = password;
        user.nickname = nickname;
        user.profileImage = profileImage;
        user.createdAt = LocalDateTime.now();
        user.deletedAt = null;
        user.informationUpdatedAt = user.createdAt;
        user.passwordUpdatedAt = user.createdAt;
        return user;
    }

    public User updateInformation(String nickname, ImageFile profileImage) {
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.informationUpdatedAt = LocalDateTime.now();
        return this;
    }

    public User updatePassword(String password) {
        this.password = password;
        this.passwordUpdatedAt = LocalDateTime.now();
        return this;
    }

    public User delete() {
        this.deletedAt = LocalDateTime.now();
        return this;
    }
}
