package com.review.crawler.dto;

import lombok.Data;

@Data
public class ReviewDto {
    private String date;
    private String rating;
    private String text;
    private String isin;

    public ReviewDto(String date, String rating, String text, String isin) {
        this.date = date;
        this.rating = rating;
        this.text = text;
        this.isin = isin;
    }
}
