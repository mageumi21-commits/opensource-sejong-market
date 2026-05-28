package com.market.backend.chat.repository;

import com.market.backend.chat.entity.ChatRoom;
import com.market.backend.product.entity.Product;
import com.market.backend.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByProductAndBuyerAndSeller(Product product, User buyer, User seller);
}
