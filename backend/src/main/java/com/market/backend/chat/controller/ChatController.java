package com.market.backend.chat.controller;

import com.market.backend.chat.dto.ChatMessageCreateRequest;
import com.market.backend.chat.dto.ChatMessageResponse;
import com.market.backend.chat.dto.ChatRoomCreateRequest;
import com.market.backend.chat.dto.ChatRoomResponse;
import com.market.backend.chat.service.ChatService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatrooms")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ChatRoomResponse createOrGetChatRoom(@RequestBody ChatRoomCreateRequest request) {
        return chatService.createOrGetChatRoom(request);
    }

    @GetMapping("/{chatRoomId}")
    public ChatRoomResponse getChatRoom(@PathVariable Long chatRoomId) {
        return chatService.getChatRoom(chatRoomId);
    }

    @GetMapping("/{chatRoomId}/messages")
    public List<ChatMessageResponse> getMessages(@PathVariable Long chatRoomId) {
        return chatService.getMessages(chatRoomId);
    }

    @PostMapping("/{chatRoomId}/messages")
    public ChatMessageResponse sendMessage(
            @PathVariable Long chatRoomId,
            @RequestBody ChatMessageCreateRequest request
    ) {
        return chatService.sendMessage(chatRoomId, request);
    }
}
