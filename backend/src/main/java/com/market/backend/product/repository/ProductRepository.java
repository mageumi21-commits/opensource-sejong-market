package com.market.backend.product.repository;

import com.market.backend.product.entity.Product;
import com.market.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByUserOrderByCreatedAtDesc(User user);
}
