package com.market.backend.chat.repository;

import com.market.backend.chat.entity.ChatMessage;
import com.market.backend.chat.entity.ChatRoom;
import com.market.backend.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);

    Optional<ChatMessage> findTopByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);

    long countByChatRoomAndSenderNotAndReadAtIsNull(ChatRoom chatRoom, User sender);

    boolean existsByChatRoomAndSenderNotAndReadAtIsNull(ChatRoom chatRoom, User sender);
}
