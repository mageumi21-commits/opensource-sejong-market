package com.market.backend.product.service;

import com.market.backend.product.dto.ProductResponse;
import com.market.backend.product.dto.ProductListResponse;
import com.market.backend.product.entity.Product;
import com.market.backend.product.repository.ProductRepository;
import com.market.backend.user.entity.User;
import com.market.backend.user.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Value("${product.image-upload-dir:uploads/products}")
    private String imageUploadDir;

    @Transactional
    public ProductResponse createProduct(
            String sellerEmail,
            String productName,
            String category,
            String price,
            String description,
            String tradeMethod,
            Integer locationNumber,
            String locationName,
            List<MultipartFile> images
    ) {
        validateProduct(productName, category, price, description, tradeMethod, locationNumber);

        User seller = findSellerOrNull(sellerEmail);
        Integer parsedPrice = parsePrice(price);
        List<String> imagePaths = saveImages(images);

        Product product = new Product(
                seller,
                productName.trim(),
                category.trim(),
                parsedPrice,
                description.trim(),
                tradeMethod.trim(),
                locationNumber,
                StringUtils.hasText(locationName) ? locationName.trim() : null,
                imagePaths
        );

        Product savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductListResponse> getProducts() {
        return productRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ProductListResponse::from)
                .toList();
    }

    private void validateProduct(
            String productName,
            String category,
            String price,
            String description,
            String tradeMethod,
            Integer locationNumber
    ) {
        if (!StringUtils.hasText(productName)) {
            throw new IllegalArgumentException("상품명을 입력해 주세요.");
        }

        if (!StringUtils.hasText(category)) {
            throw new IllegalArgumentException("카테고리를 선택해 주세요.");
        }

        if (!StringUtils.hasText(price)) {
            throw new IllegalArgumentException("가격을 입력해 주세요.");
        }

        if (!StringUtils.hasText(description)) {
            throw new IllegalArgumentException("상품 설명을 입력해 주세요.");
        }

        if (!StringUtils.hasText(tradeMethod)) {
            throw new IllegalArgumentException("수령 방식을 선택해 주세요.");
        }

        if ("직거래".equals(tradeMethod) && locationNumber == null) {
            throw new IllegalArgumentException("직거래 위치를 선택해 주세요.");
        }
    }

    private User findSellerOrNull(String sellerEmail) {
        if (!StringUtils.hasText(sellerEmail)) {
            return null;
        }

        return userRepository.findByEmail(sellerEmail.trim())
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보를 찾을 수 없습니다."));
    }

    private Integer parsePrice(String price) {
        try {
            int parsedPrice = Integer.parseInt(price);

            if (parsedPrice < 0) {
                throw new IllegalArgumentException("가격은 0원 이상이어야 합니다.");
            }

            return parsedPrice;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("가격은 숫자만 입력해 주세요.");
        }
    }

    private List<String> saveImages(List<MultipartFile> images) {
        List<String> imagePaths = new ArrayList<>();

        if (images == null || images.isEmpty()) {
            return imagePaths;
        }

        if (images.size() > 10) {
            throw new IllegalArgumentException("이미지는 최대 10장까지 등록할 수 있습니다.");
        }

        Path uploadPath = Paths.get(imageUploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(uploadPath);

            for (MultipartFile image : images) {
                if (image == null || image.isEmpty()) {
                    continue;
                }

                String contentType = image.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
                }

                String uploadFilename = image.getOriginalFilename();
                String originalFilename = uploadFilename == null ? "" : StringUtils.cleanPath(uploadFilename);
                String extension = getExtension(originalFilename);
                String savedFilename = UUID.randomUUID() + extension;
                Path savedPath = uploadPath.resolve(savedFilename);

                image.transferTo(savedPath);
                imagePaths.add("/uploads/products/" + savedFilename);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("이미지 저장 중 오류가 발생했습니다.");
        }

        return imagePaths;
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }

        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex == -1) {
            return "";
        }

        return filename.substring(dotIndex);
    }
}
