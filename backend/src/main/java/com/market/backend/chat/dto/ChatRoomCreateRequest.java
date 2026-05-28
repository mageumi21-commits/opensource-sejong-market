package com.market.backend.chat.dto;

import lombok.Getter;

@Getter
public class ChatRoomCreateRequest {

    private Long productId;

    private String buyerEmail;
}
