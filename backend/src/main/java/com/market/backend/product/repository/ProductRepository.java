package com.market.backend.product.repository;

import com.market.backend.product.entity.Product;
import com.market.backend.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findBySellerOrderByCreatedAtDesc(User seller);

    List<Product> findAllByOrderByCreatedAtDesc();
}
