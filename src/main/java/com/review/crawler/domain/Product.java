package com.review.crawler.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "isin")
    private String isin;

    @Column(name = "min_price")
    private String minPrice;

    @Column(name = "max_price")
    private String maxPrice;

    @Column(name = "rating")
    private String rating;
}
