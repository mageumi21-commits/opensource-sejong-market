package com.market.backend.chat.dto;

import com.market.backend.chat.entity.ChatMessage;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ChatMessageResponse {

    private final Long id;
    private final Long chatRoomId;
    private final Long senderId;
    private final String senderEmail;
    private final String senderNickname;
    private final String content;
    private final LocalDateTime createdAt;

    private ChatMessageResponse(ChatMessage message) {
        this.id = message.getId();
        this.chatRoomId = message.getChatRoom().getId();
        this.senderId = message.getSender().getId();
        this.senderEmail = message.getSender().getEmail();
        this.senderNickname = message.getSender().getNickname();
        this.content = message.getContent();
        this.createdAt = message.getCreatedAt();
    }

    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(message);
    }
}
