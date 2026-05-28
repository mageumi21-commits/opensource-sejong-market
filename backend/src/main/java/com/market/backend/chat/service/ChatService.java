package com.market.backend.chat.service;

import com.market.backend.chat.dto.ChatMessageCreateRequest;
import com.market.backend.chat.dto.ChatMessageResponse;
import com.market.backend.chat.dto.ChatRoomCreateRequest;
import com.market.backend.chat.dto.ChatRoomResponse;
import com.market.backend.chat.entity.ChatMessage;
import com.market.backend.chat.entity.ChatRoom;
import com.market.backend.chat.repository.ChatMessageRepository;
import com.market.backend.chat.repository.ChatRoomRepository;
import com.market.backend.product.entity.Product;
import com.market.backend.product.repository.ProductRepository;
import com.market.backend.user.entity.User;
import com.market.backend.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatRoomResponse createOrGetChatRoom(ChatRoomCreateRequest request) {
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Product id is required.");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        if (product.getSeller() == null) {
            throw new IllegalArgumentException("This product has no seller information.");
        }

        User buyer = findUserByEmail(request.getBuyerEmail());
        User seller = product.getSeller();

        if (buyer.getId().equals(seller.getId())) {
            throw new IllegalArgumentException("You cannot chat with yourself.");
        }

        ChatRoom chatRoom = chatRoomRepository.findByProductAndBuyerAndSeller(product, buyer, seller)
                .orElseGet(() -> chatRoomRepository.save(new ChatRoom(product, buyer, seller)));

        return ChatRoomResponse.from(chatRoom);
    }

    @Transactional(readOnly = true)
    public ChatRoomResponse getChatRoom(Long chatRoomId) {
        return ChatRoomResponse.from(findChatRoom(chatRoomId));
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long chatRoomId) {
        ChatRoom chatRoom = findChatRoom(chatRoomId);

        return chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom)
                .stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long chatRoomId, ChatMessageCreateRequest request) {
        ChatRoom chatRoom = findChatRoom(chatRoomId);
        User sender = findUserByEmail(request.getSenderEmail());

        if (!sender.getId().equals(chatRoom.getBuyer().getId())
                && !sender.getId().equals(chatRoom.getSeller().getId())) {
            throw new IllegalArgumentException("Only chat participants can send messages.");
        }

        if (!StringUtils.hasText(request.getContent())) {
            throw new IllegalArgumentException("Message content is required.");
        }

        ChatMessage message = chatMessageRepository.save(
                new ChatMessage(chatRoom, sender, request.getContent().trim())
        );
        chatRoom.touch();

        return ChatMessageResponse.from(message);
    }

    private ChatRoom findChatRoom(Long chatRoomId) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("Chat room id is required.");
        }

        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found."));
    }

    private User findUserByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("User email is required.");
        }

        return userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }
}
