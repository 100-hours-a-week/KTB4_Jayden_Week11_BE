package com.example.spring_rest_api.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long chatRoomId;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "chatRoom", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private ChatRoomMember chatRoomMember;

    public static ChatRoom create() {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.createdAt = LocalDateTime.now();
        return chatRoom;
    }
}