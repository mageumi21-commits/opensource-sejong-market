package com.market.backend.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.market.backend.product.dto.ProductListResponse;
import com.market.backend.product.dto.ProductResponse;
import com.market.backend.product.dto.ProductUpdateRequest;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_savesProductWithSeller() {
        User seller = user(1L, "seller@sju.ac.kr");
        when(userRepository.findByEmail("seller@sju.ac.kr")).thenReturn(Optional.of(seller));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.createProduct(
                "seller@sju.ac.kr",
                " 전공책 ",
                "도서",
                "12000",
                " 깨끗합니다 ",
                "직거래",
                3,
                " 학생회관 ",
                null
        );

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();

        assertThat(savedProduct.getSeller()).isEqualTo(seller);
        assertThat(savedProduct.getProductName()).isEqualTo("전공책");
        assertThat(savedProduct.getPrice()).isEqualTo(12000);
        assertThat(savedProduct.getLocationName()).isEqualTo("학생회관");
        assertThat(response.getSellerEmail()).isEqualTo("seller@sju.ac.kr");
        assertThat(response.getProductName()).isEqualTo("전공책");
    }

    @Test
    void createProduct_rejectsInvalidPrice() {
        User seller = user(1L, "seller@sju.ac.kr");
        when(userRepository.findByEmail("seller@sju.ac.kr")).thenReturn(Optional.of(seller));

        assertThatThrownBy(() -> productService.createProduct(
                "seller@sju.ac.kr",
                "전공책",
                "도서",
                "abc",
                "깨끗합니다",
                "직거래",
                3,
                "학생회관",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가격은 숫자만 입력해 주세요.");
    }

    @Test
    void getProducts_returnsProductList() {
        Product product = product(1L, user(1L, "seller@sju.ac.kr"), "전공책", "도서");
        when(productRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(product));

        List<ProductListResponse> responses = productService.getProducts(null, null, null, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getProductName()).isEqualTo("전공책");
        assertThat(responses.get(0).getSellerEmail()).isEqualTo("seller@sju.ac.kr");
    }

    @Test
    void getProducts_filtersByKeywordAndCategory() {
        Product book = product(1L, user(1L, "seller@sju.ac.kr"), "전공책", "도서");
        Product laptop = product(2L, user(1L, "seller@sju.ac.kr"), "노트북", "전자기기");
        when(productRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(book, laptop));

        List<ProductListResponse> responses = productService.getProducts("전공", "도서", null, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getProductName()).isEqualTo("전공책");
    }

    @Test
    void getProduct_returnsProductDetail() {
        Product product = product(1L, user(1L, "seller@sju.ac.kr"), "전공책", "도서");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProduct(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getProductName()).isEqualTo("전공책");
    }

    @Test
    void getProduct_rejectsUnknownProduct() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product not found.");
    }

    @Test
    void updateProduct_updatesProductForSeller() {
        User seller = user(1L, "seller@sju.ac.kr");
        Product product = product(1L, seller, "전공책", "도서");
        ProductUpdateRequest request = updateRequest("seller@sju.ac.kr", "수정된 전공책", "도서", 10000);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("seller@sju.ac.kr")).thenReturn(Optional.of(seller));

        ProductResponse response = productService.updateProduct(1L, request);

        assertThat(response.getProductName()).isEqualTo("수정된 전공책");
        assertThat(response.getPrice()).isEqualTo(10000);
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateProduct_rejectsNonSeller() {
        User seller = user(1L, "seller@sju.ac.kr");
        User requester = user(2L, "buyer@sju.ac.kr");
        Product product = product(1L, seller, "전공책", "도서");
        ProductUpdateRequest request = updateRequest("buyer@sju.ac.kr", "수정된 전공책", "도서", 10000);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("buyer@sju.ac.kr")).thenReturn(Optional.of(requester));

        assertThatThrownBy(() -> productService.updateProduct(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 판매자만 수정 또는 삭제할 수 있습니다.");
    }

    @Test
    void deleteProduct_deletesProductForSeller() {
        User seller = user(1L, "seller@sju.ac.kr");
        Product product = product(1L, seller, "전공책", "도서");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("seller@sju.ac.kr")).thenReturn(Optional.of(seller));

        productService.deleteProduct(1L, "seller@sju.ac.kr");

        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_rejectsNonSeller() {
        User seller = user(1L, "seller@sju.ac.kr");
        User requester = user(2L, "buyer@sju.ac.kr");
        Product product = product(1L, seller, "전공책", "도서");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("buyer@sju.ac.kr")).thenReturn(Optional.of(requester));

        assertThatThrownBy(() -> productService.deleteProduct(1L, "buyer@sju.ac.kr"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 판매자만 수정 또는 삭제할 수 있습니다.");
    }

    private User user(Long id, String email) {
        User user = new User(email, "password", "테스트유저", "23011234");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Product product(Long id, User seller, String productName, String category) {
        Product product = new Product(
                seller,
                productName,
                category,
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

    private ProductUpdateRequest updateRequest(String sellerEmail, String productName, String category, Integer price) {
        ProductUpdateRequest request = new ProductUpdateRequest();
        ReflectionTestUtils.setField(request, "sellerEmail", sellerEmail);
        ReflectionTestUtils.setField(request, "productName", productName);
        ReflectionTestUtils.setField(request, "category", category);
        ReflectionTestUtils.setField(request, "price", price);
        ReflectionTestUtils.setField(request, "description", "수정된 설명입니다.");
        ReflectionTestUtils.setField(request, "tradeMethod", "직거래");
        ReflectionTestUtils.setField(request, "locationNumber", 5);
        ReflectionTestUtils.setField(request, "locationName", "도서관");
        return request;
    }
}
