package com.market.backend.chat.service;

import com.market.backend.chat.dto.ChatMessageCreateRequest;
import com.market.backend.chat.dto.ChatMessageResponse;
import com.market.backend.chat.dto.ChatRoomCreateRequest;
import com.market.backend.chat.dto.ChatRoomListResponse;
import com.market.backend.chat.dto.ChatRoomResponse;
import com.market.backend.chat.dto.ChatUnreadResponse;
import com.market.backend.chat.entity.ChatMessage;
import com.market.backend.chat.entity.ChatRoom;
import com.market.backend.chat.repository.ChatMessageRepository;
import com.market.backend.chat.repository.ChatRoomRepository;
import com.market.backend.product.entity.Product;
import com.market.backend.product.repository.ProductRepository;
import com.market.backend.user.entity.User;
import com.market.backend.user.repository.UserRepository;
import java.util.List;
import java.util.Locale;
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
        chatRoom.rejoin(buyer);

        return ChatRoomResponse.from(chatRoom);
    }

    @Transactional(readOnly = true)
    public ChatRoomResponse getChatRoom(Long chatRoomId) {
        return ChatRoomResponse.from(findChatRoom(chatRoomId));
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long chatRoomId) {
        return getMessages(chatRoomId, null);
    }

    @Transactional
    public List<ChatMessageResponse> getMessages(Long chatRoomId, String userEmail) {
        ChatRoom chatRoom = findChatRoom(chatRoomId);
        User currentUser = StringUtils.hasText(userEmail) ? findUserByEmail(userEmail) : null;
        if (currentUser != null) {
            validateParticipant(chatRoom, currentUser);
        }

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);
        if (currentUser != null) {
            messages.stream()
                    .filter(message -> message.isUnreadFor(currentUser))
                    .forEach(ChatMessage::markRead);
        }

        return messages.stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> getMyChatRooms(String userEmail) {
        User user = findUserByEmail(userEmail);

        return chatRoomRepository.findByBuyerOrSellerOrderByUpdatedAtDesc(user, user)
                .stream()
                .filter(chatRoom -> !chatRoom.isLeftBy(user))
                .map(chatRoom -> {
                    ChatMessage lastMessage = chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(chatRoom)
                            .orElse(null);
                    long unreadCount = chatMessageRepository.countByChatRoomAndSenderNotAndReadAtIsNull(chatRoom, user);
                    return ChatRoomListResponse.of(chatRoom, user, lastMessage, unreadCount);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatUnreadResponse getUnreadSummary(String userEmail) {
        User user = findUserByEmail(userEmail);
        long unreadCount = chatRoomRepository.findByBuyerOrSellerOrderByUpdatedAtDesc(user, user)
                .stream()
                .filter(chatRoom -> !chatRoom.isLeftBy(user))
                .mapToLong(chatRoom -> chatMessageRepository.countByChatRoomAndSenderNotAndReadAtIsNull(chatRoom, user))
                .sum();

        return ChatUnreadResponse.of(unreadCount);
    }

    @Transactional
    public void leaveChatRoom(Long chatRoomId, String userEmail) {
        ChatRoom chatRoom = findChatRoom(chatRoomId);
        User user = findUserByEmail(userEmail);

        validateParticipant(chatRoom, user);
        chatRoom.leave(user);
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long chatRoomId, ChatMessageCreateRequest request) {
        ChatRoom chatRoom = findChatRoom(chatRoomId);
        User sender = findUserByEmail(request.getSenderEmail());

        validateParticipant(chatRoom, sender);

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

        return userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private void validateParticipant(ChatRoom chatRoom, User user) {
        if (!chatRoom.isParticipant(user) || chatRoom.isLeftBy(user)) {
            throw new IllegalArgumentException("Only chat participants can send messages.");
        }
    }

    private String normalizeEmail(String email) {
        String trimmedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (trimmedEmail.contains("@")) {
            return trimmedEmail;
        }

        return trimmedEmail + "@sju.ac.kr";
    }
}
