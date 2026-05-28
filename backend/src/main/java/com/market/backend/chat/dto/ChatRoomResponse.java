package com.market.backend.chat.dto;

import com.market.backend.chat.entity.ChatRoom;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ChatRoomResponse {

    private final Long id;
    private final Long productId;
    private final String productName;
    private final Long buyerId;
    private final String buyerEmail;
    private final String buyerNickname;
    private final Long sellerId;
    private final String sellerEmail;
    private final String sellerNickname;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ChatRoomResponse(ChatRoom chatRoom) {
        this.id = chatRoom.getId();
        this.productId = chatRoom.getProduct().getId();
        this.productName = chatRoom.getProduct().getProductName();
        this.buyerId = chatRoom.getBuyer().getId();
        this.buyerEmail = chatRoom.getBuyer().getEmail();
        this.buyerNickname = chatRoom.getBuyer().getNickname();
        this.sellerId = chatRoom.getSeller().getId();
        this.sellerEmail = chatRoom.getSeller().getEmail();
        this.sellerNickname = chatRoom.getSeller().getNickname();
        this.createdAt = chatRoom.getCreatedAt();
        this.updatedAt = chatRoom.getUpdatedAt();
    }

    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return new ChatRoomResponse(chatRoom);
    }
}
