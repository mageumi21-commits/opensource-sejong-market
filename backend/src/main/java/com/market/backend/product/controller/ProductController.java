package com.market.backend.product.controller;

import com.market.backend.product.dto.ProductResponse;
import com.market.backend.product.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    public ProductResponse getProduct(@PathVariable Long productId) {
        return productService.getProduct(productId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponse createProduct(
            @RequestParam(required = false) String sellerEmail,
            @RequestParam String productName,
            @RequestParam String category,
            @RequestParam String price,
            @RequestParam String description,
            @RequestParam String tradeMethod,
            @RequestParam(required = false) Integer locationNumber,
            @RequestParam(required = false) String locationName,
            @RequestParam(required = false) List<MultipartFile> images
    ) {
        return productService.createProduct(
                sellerEmail,
                productName,
                category,
                price,
                description,
                tradeMethod,
                locationNumber,
                locationName,
                images
        );
    }
}
