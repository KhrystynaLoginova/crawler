package com.review.crawler.utils;

import com.review.crawler.dto.ProductDto;
import com.review.crawler.dto.ReviewDto;
import com.review.crawler.domain.Product;
import com.review.crawler.domain.Review;

public class UtilsDto {
    public static ReviewDto toReviewDto(Review review) {
        return new ReviewDto(review.getDate(), review.getRating(), review.getText(), review.getProduct().getIsin());
    }

    public static ProductDto toProductDto(Product product) {
        return new ProductDto(product.getTitle(), product.getDescription(), product.getMinPrice(), product.getMaxPrice(), product.getRating());
    }
}
