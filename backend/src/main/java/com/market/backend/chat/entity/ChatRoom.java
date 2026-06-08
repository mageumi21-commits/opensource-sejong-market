package com.market.backend.chat.entity;

import com.market.backend.product.entity.Product;
import com.market.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime buyerLeftAt;

    private LocalDateTime sellerLeftAt;

    public ChatRoom(Product product, User buyer, User seller) {
        this.product = product;
        this.buyer = buyer;
        this.seller = seller;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isParticipant(User user) {
        return user != null
                && (user.getId().equals(buyer.getId()) || user.getId().equals(seller.getId()));
    }

    public boolean isLeftBy(User user) {
        if (user == null) {
            return false;
        }

        if (user.getId().equals(buyer.getId())) {
            return buyerLeftAt != null;
        }

        if (user.getId().equals(seller.getId())) {
            return sellerLeftAt != null;
        }

        return false;
    }

    public void leave(User user) {
        if (user.getId().equals(buyer.getId())) {
            buyerLeftAt = LocalDateTime.now();
            return;
        }

        if (user.getId().equals(seller.getId())) {
            sellerLeftAt = LocalDateTime.now();
        }
    }

    public void rejoin(User user) {
        if (user.getId().equals(buyer.getId())) {
            buyerLeftAt = null;
            return;
        }

        if (user.getId().equals(seller.getId())) {
            sellerLeftAt = null;
        }
    }
}
