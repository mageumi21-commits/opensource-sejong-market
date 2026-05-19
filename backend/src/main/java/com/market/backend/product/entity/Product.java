package com.market.backend.product.entity;

import com.market.backend.user.entity.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String tradeMethod;

    private Integer locationNumber;

    private String locationName;

    @ElementCollection
    @CollectionTable(name = "product_image", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_path")
    private List<String> imagePaths = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Product(
            User seller,
            String productName,
            String category,
            Integer price,
            String description,
            String tradeMethod,
            Integer locationNumber,
            String locationName,
            List<String> imagePaths
    ) {
        this.seller = seller;
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.description = description;
        this.tradeMethod = tradeMethod;
        this.locationNumber = locationNumber;
        this.locationName = locationName;
        this.imagePaths = imagePaths;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }
}
