package com.review.crawler.service;

import com.review.crawler.domain.Product;
import com.review.crawler.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductServiceTests {

    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private ProductReviewService productReviewService;


    @Test
    public void testLookupByIsin_existingProduct() {
        // Arrange
        String isin = "B000NZTPMG";
        Product product = new Product();
        product.setId(1L);
        product.setIsin(isin);
        Optional<Product> productOpt = Optional.of(product);

        when(productRepository.findByIsin(isin)).thenReturn(productOpt);

        // Act
        Product result = productReviewService.lookupByIsin(isin);

        // Assert
        assertEquals(product, result);
        verify(productRepository, times(1)).findByIsin(isin);
    }
}

