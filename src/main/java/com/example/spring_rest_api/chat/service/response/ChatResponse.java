package com.example.spring_rest_api.chat.service.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ChatResponse {
    private Long chatMessageId;
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private Long roomId;
    private String content;
    private LocalDateTime createdAt;

    public static ChatResponse from(
            Long chatMessageId,
            Long userId,
            String nickname,
            String profileImageUrl,
            Long roomId,
            String content,
            LocalDateTime createdAt
    ) {
        ChatResponse response = new ChatResponse();
        response.chatMessageId = chatMessageId;
        response.userId = userId;
        response.nickname = nickname;
        response.profileImageUrl = profileImageUrl;
        response.roomId = roomId;
        response.content = content;
        response.createdAt = createdAt;
        return response;
    }
}
