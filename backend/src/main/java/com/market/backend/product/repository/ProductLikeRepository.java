package com.market.backend.product.repository;

import com.market.backend.product.entity.Product;
import com.market.backend.product.entity.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {

    boolean existsByUserEmailAndProduct(String userEmail, Product product);

    Optional<ProductLike> findByUserEmailAndProduct(String userEmail, Product product);

    List<ProductLike> findByUserEmailOrderByIdDesc(String userEmail);

    void deleteByProduct(Product product);
}
