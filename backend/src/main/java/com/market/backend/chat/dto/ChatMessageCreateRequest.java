package com.market.backend.chat.dto;

import lombok.Getter;

@Getter
public class ChatMessageCreateRequest {

    private String senderEmail;

    private String content;
}
