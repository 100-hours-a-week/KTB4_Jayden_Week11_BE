package com.example.spring_rest_api.chat.entity;

import com.example.spring_rest_api.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_room_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatRoomMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_message_id")
    private ChatMessage lastReadMessage;

    public static ChatRoomMember addMember(ChatRoom chatRoom, User user) {
        ChatRoomMember member = new ChatRoomMember();
        member.chatRoom =  chatRoom;
        member.user = user;
        return member;
    }

    public ChatRoomMember updateLastReadMessage(ChatMessage lastReadMessage) {
        this.lastReadMessage = lastReadMessage;
        return this;
    }
}
