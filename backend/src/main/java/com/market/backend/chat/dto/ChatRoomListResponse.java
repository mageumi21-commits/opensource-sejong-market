package com.market.backend.chat.dto;

import com.market.backend.chat.entity.ChatMessage;
import com.market.backend.chat.entity.ChatRoom;
import com.market.backend.user.entity.User;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ChatRoomListResponse {

    private final Long roomId;
    private final Long productId;
    private final String productName;
    private final String opponentEmail;
    private final String opponentNickname;
    private final String lastMessage;
    private final LocalDateTime lastMessageAt;
    private final boolean hasUnreadMessages;
    private final long unreadCount;

    private ChatRoomListResponse(ChatRoom chatRoom, User opponent, ChatMessage lastMessage, long unreadCount) {
        this.roomId = chatRoom.getId();
        this.productId = chatRoom.getProduct().getId();
        this.productName = chatRoom.getProduct().getProductName();
        this.opponentEmail = opponent.getEmail();
        this.opponentNickname = opponent.getNickname();
        this.lastMessage = lastMessage == null ? "" : lastMessage.getContent();
        this.lastMessageAt = lastMessage == null ? chatRoom.getUpdatedAt() : lastMessage.getCreatedAt();
        this.hasUnreadMessages = unreadCount > 0;
        this.unreadCount = unreadCount;
    }

    public static ChatRoomListResponse of(ChatRoom chatRoom, User currentUser, ChatMessage lastMessage, long unreadCount) {
        User opponent = chatRoom.getBuyer().getId().equals(currentUser.getId())
                ? chatRoom.getSeller()
                : chatRoom.getBuyer();
        return new ChatRoomListResponse(chatRoom, opponent, lastMessage, unreadCount);
    }
}
