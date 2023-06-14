package com.review.crawler.dto;

import lombok.Data;

@Data
public class ProductDto {
    private String title;
    private String description;
    private String minPrice;
    private String maxPrice;
    private String rating;

    public ProductDto(String title, String description, String minPrice, String maxPrice, String rating) {
        this.title = title;
        this.description = description;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.rating = rating;
    }
}
