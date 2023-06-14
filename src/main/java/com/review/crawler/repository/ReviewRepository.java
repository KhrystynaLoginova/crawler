package com.review.crawler.repository;

import com.review.crawler.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "reviews", path = "reviews")
public interface ReviewRepository extends PagingAndSortingRepository<Review, Long> {

    @Query(value = "SELECT r FROM Review r WHERE r.text like :searchTerm")
    Page<Review> searchByReviewText(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query(value = "SELECT r FROM Review r WHERE r.product.id = :productId AND r.text like :searchTerm")
    Page<Review> searchByProductIdAndReviewText(@Param("productId") Long productId, @Param("searchTerm") String fuzzySearch, Pageable pageable);

    Page<Review> searchByProductId(@Param("productId") Long productId, Pageable pageable);

    <S extends Review> Iterable<S> saveAll(Iterable<S> iterable);
}
