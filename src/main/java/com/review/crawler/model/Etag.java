package com.review.crawler.model;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class Etag {
    public static final Etag DEFAULT = new Etag("", 0);
    private String rating;
    private int reviews;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Etag etag = (Etag) o;
        return reviews == etag.reviews && rating.equals(etag.rating);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rating, reviews);
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public int getReviews() {
        return reviews;
    }

    public void setReviews(int reviews) {
        this.reviews = reviews;
    }
}
