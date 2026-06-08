package com.market.backend.chat.dto;

import lombok.Getter;

@Getter
public class ChatUnreadResponse {

    private final boolean hasUnreadMessages;
    private final long unreadCount;

    private ChatUnreadResponse(long unreadCount) {
        this.unreadCount = unreadCount;
        this.hasUnreadMessages = unreadCount > 0;
    }

    public static ChatUnreadResponse of(long unreadCount) {
        return new ChatUnreadResponse(unreadCount);
    }
}
