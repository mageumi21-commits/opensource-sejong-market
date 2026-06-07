package com.market.backend.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatService chatService;

    @Test
    void createOrGetChatRoom_createsNewChatRoom() {
        User seller = user(1L, "seller@sju.ac.kr", "판매자");
        User buyer = user(2L, "buyer@sju.ac.kr", "구매자");
        Product product = product(1L, seller, "전공책");
        ChatRoomCreateRequest request = chatRoomCreateRequest(1L, "buyer@sju.ac.kr");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("buyer@sju.ac.kr")).thenReturn(Optional.of(buyer));
        when(chatRoomRepository.findByProductAndBuyerAndSeller(product, buyer, seller)).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(invocation -> {
            ChatRoom chatRoom = invocation.getArgument(0);
            ReflectionTestUtils.setField(chatRoom, "id", 10L);
            return chatRoom;
        });

        ChatRoomResponse response = chatService.createOrGetChatRoom(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getBuyerEmail()).isEqualTo("buyer@sju.ac.kr");
        assertThat(response.getSellerEmail()).isEqualTo("seller@sju.ac.kr");
        verify(chatRoomRepository).save(any(ChatRoom.class));
    }

    @Test
    void createOrGetChatRoom_returnsExistingChatRoom() {
        User seller = user(1L, "seller@sju.ac.kr", "판매자");
        User buyer = user(2L, "buyer@sju.ac.kr", "구매자");
        Product product = product(1L, seller, "전공책");
        ChatRoom chatRoom = chatRoom(10L, product, buyer, seller);
        ChatRoomCreateRequest request = chatRoomCreateRequest(1L, "buyer@sju.ac.kr");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("buyer@sju.ac.kr")).thenReturn(Optional.of(buyer));
        when(chatRoomRepository.findByProductAndBuyerAndSeller(product, buyer, seller)).thenReturn(Optional.of(chatRoom));

        ChatRoomResponse response = chatService.createOrGetChatRoom(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getProductName()).isEqualTo("전공책");
    }

    @Test
    void createOrGetChatRoom_rejectsUnknownProduct() {
        ChatRoomCreateRequest request = chatRoomCreateRequest(99L, "buyer@sju.ac.kr");
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.createOrGetChatRoom(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product not found.");
    }

    @Test
    void createOrGetChatRoom_rejectsSelfChat() {
        User seller = user(1L, "seller@sju.ac.kr", "판매자");
        Product product = product(1L, seller, "전공책");
        ChatRoomCreateRequest request = chatRoomCreateRequest(1L, "seller@sju.ac.kr");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("seller@sju.ac.kr")).thenReturn(Optional.of(seller));

        assertThatThrownBy(() -> chatService.createOrGetChatRoom(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You cannot chat with yourself.");
    }

    @Test
    void getChatRoom_returnsChatRoom() {
        User seller = user(1L, "seller@sju.ac.kr", "판매자");
        User buyer = user(2L, "buyer@sju.ac.kr", "구매자");
        Product product = product(1L, seller, "전공책");
        ChatRoom chatRoom = chatRoom(10L, product, buyer, seller);

        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(chatRoom));

        ChatRoomResponse response = chatService.getChatRoom(10L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getSellerNickname()).isEqualTo("판매자");
        assertThat(response.getBuyerNickname()).isEqualTo("구매자");
    }

    @Test
    void getChatRoom_rejectsUnknownChatRoom() {
        when(chatRoomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getChatRoom(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Chat room not found.");
    }

    @Test
    void getMessages_returnsMessages() {
        User seller = user(1L, "seller@sju.ac.kr", "판매자");
        User buyer = user(2L, "buyer@sju.ac.kr", "구매자");
        Product product = product(1L, seller, "전공책");
        ChatRoom chatRoom = chatRoom(10L, product, buyer, seller);
        ChatMessage message = message(100L, chatRoom, buyer, "안녕하세요");

        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(chatRoom));
        when(chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom)).thenReturn(List.of(message));

        List<ChatMessageResponse> responses = chatService.getMessages(10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getContent()).isEqualTo("안녕하세요");
        assertThat(responses.get(0).getSenderEmail()).isEqualTo("buyer@sju.ac.kr");
    }

    @Test
    void sendMessage_savesTrimmedMessage() {
        User seller = user(1L, "seller@sju.ac.kr", "판매자");
        User buyer = user(2L, "buyer@sju.ac.kr", "구매자");
        Product product = product(1L, seller, "전공책");
        ChatRoom chatRoom = chatRoom(10L, product, buyer, seller);
        ChatMessageCreateRequest request = messageCreateRequest("buyer@sju.ac.kr", " 안녕하세요 ");

        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findByEmail("buyer@sju.ac.kr")).thenReturn(Optional.of(buyer));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            ReflectionTestUtils.setField(message, "id", 100L);
            return message;
        });

        ChatMessageResponse response = chatService.sendMessage(10L, request);

        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository).save(messageCaptor.capture());

        assertThat(messageCaptor.getValue().getContent()).isEqualTo("안녕하세요");
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getContent()).isEqualTo("안녕하세요");
        assertThat(response.getSenderEmail()).isEqualTo("buyer@sju.ac.kr");
    }

    @Test
    void sendMessage_rejectsNonParticipant() {
        User seller = user(1L, "seller@sju.ac.kr", "판매자");
        User buyer = user(2L, "buyer@sju.ac.kr", "구매자");
        User stranger = user(3L, "stranger@sju.ac.kr", "제3자");
        Product product = product(1L, seller, "전공책");
        ChatRoom chatRoom = chatRoom(10L, product, buyer, seller);
        ChatMessageCreateRequest request = messageCreateRequest("stranger@sju.ac.kr", "안녕하세요");

        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findByEmail("stranger@sju.ac.kr")).thenReturn(Optional.of(stranger));

        assertThatThrownBy(() -> chatService.sendMessage(10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only chat participants can send messages.");
    }

    @Test
    void sendMessage_rejectsBlankContent() {
        User seller = user(1L, "seller@sju.ac.kr", "판매자");
        User buyer = user(2L, "buyer@sju.ac.kr", "구매자");
        Product product = product(1L, seller, "전공책");
        ChatRoom chatRoom = chatRoom(10L, product, buyer, seller);
        ChatMessageCreateRequest request = messageCreateRequest("buyer@sju.ac.kr", "   ");

        when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findByEmail("buyer@sju.ac.kr")).thenReturn(Optional.of(buyer));

        assertThatThrownBy(() -> chatService.sendMessage(10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Message content is required.");
    }

    private ChatRoomCreateRequest chatRoomCreateRequest(Long productId, String buyerEmail) {
        ChatRoomCreateRequest request = new ChatRoomCreateRequest();
        ReflectionTestUtils.setField(request, "productId", productId);
        ReflectionTestUtils.setField(request, "buyerEmail", buyerEmail);
        return request;
    }

    private ChatMessageCreateRequest messageCreateRequest(String senderEmail, String content) {
        ChatMessageCreateRequest request = new ChatMessageCreateRequest();
        ReflectionTestUtils.setField(request, "senderEmail", senderEmail);
        ReflectionTestUtils.setField(request, "content", content);
        return request;
    }

    private User user(Long id, String email, String nickname) {
        User user = new User(email, "password123", nickname, "23011234");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Product product(Long id, User seller, String productName) {
        Product product = new Product(
                seller,
                productName,
                "도서",
                12000,
                "깨끗합니다.",
                "직거래",
                3,
                "학생회관",
                List.of("/uploads/products/test.png")
        );
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private ChatRoom chatRoom(Long id, Product product, User buyer, User seller) {
        ChatRoom chatRoom = new ChatRoom(product, buyer, seller);
        ReflectionTestUtils.setField(chatRoom, "id", id);
        return chatRoom;
    }

    private ChatMessage message(Long id, ChatRoom chatRoom, User sender, String content) {
        ChatMessage message = new ChatMessage(chatRoom, sender, content);
        ReflectionTestUtils.setField(message, "id", id);
        return message;
    }
}
