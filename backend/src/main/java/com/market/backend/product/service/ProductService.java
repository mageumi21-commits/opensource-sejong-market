package com.market.backend.product.service;

import com.market.backend.product.dto.ProductResponse;
import com.market.backend.product.dto.ProductListResponse;
import com.market.backend.product.dto.ProductLikeResponse;
import com.market.backend.product.dto.ProductUpdateRequest;
import com.market.backend.product.entity.Product;
import com.market.backend.product.entity.ProductLike;
import com.market.backend.product.repository.ProductLikeRepository;
import com.market.backend.product.repository.ProductRepository;
import com.market.backend.user.entity.User;
import com.market.backend.user.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
    private final ProductLikeRepository productLikeRepository;

    @Value("${product.image-upload-dir:uploads/products}")
    private String imageUploadDir;

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {
        return getProduct(productId, null);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId, String userEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        return ProductResponse.from(product, isLikedByEmail(product, userEmail));
    }

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

    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = findProduct(productId);
        validateOwner(product, request.getSellerEmail());
        validateProduct(
                request.getProductName(),
                request.getCategory(),
                request.getPrice(),
                request.getDescription(),
                request.getTradeMethod(),
                request.getLocationNumber()
        );

        product.update(
                request.getProductName().trim(),
                request.getCategory().trim(),
                request.getPrice(),
                request.getDescription().trim(),
                request.getTradeMethod().trim(),
                request.getLocationNumber(),
                StringUtils.hasText(request.getLocationName()) ? request.getLocationName().trim() : null
        );

        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse updateProductWithImages(
            Long productId,
            String sellerEmail,
            String productName,
            String category,
            String price,
            String description,
            String tradeMethod,
            Integer locationNumber,
            String locationName,
            List<String> remainingImagePaths,
            List<MultipartFile> images
    ) {
        Product product = findProduct(productId);
        validateOwner(product, sellerEmail);
        validateProduct(productName, category, price, description, tradeMethod, locationNumber);

        Integer parsedPrice = parsePrice(price);
        List<String> nextImagePaths = new ArrayList<>();
        if (remainingImagePaths != null) {
            nextImagePaths.addAll(remainingImagePaths.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .toList());
        }

        int newImageCount = images == null ? 0 : images.stream()
                .filter(image -> image != null && !image.isEmpty())
                .toList()
                .size();

        if (nextImagePaths.size() + newImageCount > 10) {
            throw new IllegalArgumentException("이미지는 최대 10장까지 등록할 수 있습니다.");
        }

        List<String> savedImagePaths = saveImages(images);
        nextImagePaths.addAll(savedImagePaths);

        product.update(
                productName.trim(),
                category.trim(),
                parsedPrice,
                description.trim(),
                tradeMethod.trim(),
                locationNumber,
                StringUtils.hasText(locationName) ? locationName.trim() : null
        );
        product.replaceImagePaths(nextImagePaths);

        return ProductResponse.from(product);
    }

    @Transactional
    public void deleteProduct(Long productId, String sellerEmail) {
        Product product = findProduct(productId);
        validateOwner(product, sellerEmail);
        productLikeRepository.deleteByProduct(product);
        productRepository.delete(product);
    }

    @Transactional
    public ProductResponse markSoldOut(Long productId, String sellerEmail) {
        Product product = findProduct(productId);
        validateOwner(product, sellerEmail);
        product.markSoldOut();

        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public List<ProductListResponse> getProducts(String keyword, String category, String sort, String type) {
        return getProducts(keyword, category, sort, type, null);
    }

    @Transactional(readOnly = true)
    public List<ProductListResponse> getProducts(String keyword, String category, String sort, String type, String userEmail) {
        List<Product> products = productRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(product -> matchesKeyword(product, keyword))
                .filter(product -> matchesCategory(product, category))
                .toList();

        if ("recommend".equals(type)) {
            List<Product> recommendedProducts = new ArrayList<>(products);
            Collections.shuffle(recommendedProducts);
            return recommendedProducts.stream()
                    .map(product -> ProductListResponse.from(product, isLikedByEmail(product, userEmail)))
                    .toList();
        }

        return products.stream()
                .map(product -> ProductListResponse.from(product, isLikedByEmail(product, userEmail)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductListResponse> getLatestProducts(int limit) {
        return getLatestProducts(limit, null);
    }

    @Transactional(readOnly = true)
    public List<ProductListResponse> getLatestProducts(int limit, String userEmail) {
        return productRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .limit(normalizeLimit(limit))
                .map(product -> ProductListResponse.from(product, isLikedByEmail(product, userEmail)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductListResponse> getRecommendedProducts(int limit) {
        return getRecommendedProducts(limit, null);
    }

    @Transactional(readOnly = true)
    public List<ProductListResponse> getRecommendedProducts(int limit, String userEmail) {
        List<Product> products = new ArrayList<>(productRepository.findAllByOrderByCreatedAtDesc());
        Collections.shuffle(products);

        return products.stream()
                .limit(normalizeLimit(limit))
                .map(product -> ProductListResponse.from(product, isLikedByEmail(product, userEmail)))
                .toList();
    }

    @Transactional
    public ProductLikeResponse likeProduct(Long productId, String userEmail) {
        String normalizedEmail = findUser(userEmail).getEmail();
        Product product = findProduct(productId);

        if (!productLikeRepository.existsByUserEmailAndProduct(normalizedEmail, product)) {
            productLikeRepository.save(new ProductLike(normalizedEmail, product));
        }

        return ProductLikeResponse.of(productId, true);
    }

    @Transactional
    public ProductLikeResponse unlikeProduct(Long productId, String userEmail) {
        String normalizedEmail = findUser(userEmail).getEmail();
        Product product = findProduct(productId);

        productLikeRepository.findByUserEmailAndProduct(normalizedEmail, product)
                .ifPresent(productLikeRepository::delete);

        return ProductLikeResponse.of(productId, false);
    }

    @Transactional(readOnly = true)
    public ProductLikeResponse getLikeStatus(Long productId, String userEmail) {
        String normalizedEmail = findUser(userEmail).getEmail();
        Product product = findProduct(productId);
        return ProductLikeResponse.of(productId, productLikeRepository.existsByUserEmailAndProduct(normalizedEmail, product));
    }

    private boolean matchesKeyword(Product product, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }

        String lowerKeyword = keyword.trim().toLowerCase();

        return containsIgnoreCase(product.getProductName(), lowerKeyword)
                || containsIgnoreCase(product.getDescription(), lowerKeyword)
                || containsIgnoreCase(product.getCategory(), lowerKeyword)
                || containsIgnoreCase(product.getLocationName(), lowerKeyword);
    }

    private boolean matchesCategory(Product product, String category) {
        if (!StringUtils.hasText(category) || "전체".equals(category)) {
            return true;
        }

        return category.trim().equals(product.getCategory());
    }

    private boolean containsIgnoreCase(String value, String lowerKeyword) {
        return value != null && value.toLowerCase().contains(lowerKeyword);
    }

    private boolean isLikedByEmail(Product product, String userEmail) {
        if (!StringUtils.hasText(userEmail)) {
            return false;
        }

        return userRepository.findByEmail(normalizeSejongEmail(userEmail))
                .map(user -> productLikeRepository.existsByUserEmailAndProduct(user.getEmail(), product))
                .orElse(false);
    }

    private long normalizeLimit(int limit) {
        if (limit <= 0) {
            return 5;
        }

        return Math.min(limit, 50);
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

    private User findUser(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("User email is required.");
        }

        return userRepository.findByEmail(normalizeSejongEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private String normalizeSejongEmail(String email) {
        String value = email.trim().toLowerCase(Locale.ROOT);
        if (!value.contains("@")) {
            value = value + "@sju.ac.kr";
        }
        return value;
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

    private void validateProduct(
            String productName,
            String category,
            Integer price,
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

        if (price == null) {
            throw new IllegalArgumentException("가격을 입력해 주세요.");
        }

        if (price < 0) {
            throw new IllegalArgumentException("가격은 0원 이상이어야 합니다.");
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

    private Product findProduct(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("상품 ID가 필요합니다.");
        }

        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
    }

    private void validateOwner(Product product, String sellerEmail) {
        if (product.getSeller() == null) {
            throw new IllegalArgumentException("판매자 정보가 없는 상품입니다.");
        }

        if (!StringUtils.hasText(sellerEmail)) {
            throw new IllegalArgumentException("요청 사용자 이메일이 필요합니다.");
        }

        User requester = userRepository.findByEmail(sellerEmail.trim())
                .orElseThrow(() -> new IllegalArgumentException("요청 사용자를 찾을 수 없습니다."));

        if (!product.getSeller().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("상품 판매자만 수정 또는 삭제할 수 있습니다.");
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
