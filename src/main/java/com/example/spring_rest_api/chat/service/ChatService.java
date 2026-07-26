package com.example.spring_rest_api.chat.service;

import com.example.spring_rest_api.chat.entity.ChatMessage;
import com.example.spring_rest_api.chat.entity.ChatRoom;
import com.example.spring_rest_api.chat.repository.ChatMessageRepository;
import com.example.spring_rest_api.chat.repository.ChatRoomMemberRepository;
import com.example.spring_rest_api.chat.repository.ChatRoomRepository;
import com.example.spring_rest_api.chat.service.request.ChatRequest;
import com.example.spring_rest_api.chat.service.response.ChatResponse;
import com.example.spring_rest_api.common.exception.ForbiddenException;
import com.example.spring_rest_api.common.exception.NotFoundException;
import com.example.spring_rest_api.image.util.ImageFileUtil;
import com.example.spring_rest_api.user.entity.User;
import com.example.spring_rest_api.user.repository.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final ChatMessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserQueryRepository userRepository;

    @Transactional
    public ChatResponse sendText(Long senderId, Long roomId, ChatRequest request) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("ROOM_NOT_FOUND"));

        boolean isMember = chatRoomMemberRepository.findByChatRoom_ChatRoomIdAndUser_userId(roomId, senderId).isPresent();
        if (!isMember) {
            throw new ForbiddenException("ROOM_ACCESS_DENIED");
        }

        User user = userRepository.findByIdWithProfileImage(senderId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));

        ChatMessage textMessage = ChatMessage.createTextMessage(
                room,
                user,
                request.getContent()
        );
        ChatMessage saved = messageRepository.save(textMessage);
        return ChatResponse.from(
                saved.getChatMessageId(),
                user.getUserId(),
                user.getNickname(),
                resolveProfileImageUrl(user),
                room.getChatRoomId(),
                saved.getContent(),
                saved.getCreatedAt()
        );
    }

    private String resolveProfileImageUrl(User user) {
        if(user.getProfileImage() == null) return null;
        return ImageFileUtil.toFullUrl(user.getProfileImage().getFilePath());
    }
}
