package com.review.crawler.controller;

import com.review.crawler.domain.Review;
import com.review.crawler.dto.ReviewDto;
import com.review.crawler.model.Etag;
import com.review.crawler.service.ProductReviewService;
import com.review.crawler.utils.UtilsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;


@RestController
public class ReviewController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewController.class);
    private final ProductReviewService productReviewService;

    @Autowired
    public ReviewController(ProductReviewService productReviewService) {
        this.productReviewService = productReviewService;
    }

    @GetMapping("/products/{isin}/reviews")
    public ResponseEntity<Page<ReviewDto>> getReviewsByIsin(@PathVariable(value = "isin") String isin,
                                                            Pageable pageable,
                                                            @RequestHeader(value = "If-None-Match", required = false) String etagHeader
    ) {
        LOGGER.info("Search for product {}", isin);

        Etag previousEtag = productReviewService.parseStringToEtag(etagHeader);
        Etag currentETag = productReviewService.getReviewsCountByIsin(isin);

        if (previousEtag != null && previousEtag.equals(currentETag)) {
            return ResponseEntity.status(304).cacheControl(CacheControl.maxAge(Duration.ofMinutes(1))).build();
        }
        Page<Review> page;
        if (previousEtag == null) {
            page = productReviewService.lookupReviewsByIsin(isin, pageable);
        } else {
            page = productReviewService.lookupUpdatesByIsin(previousEtag, currentETag, isin, pageable);
        }

        List<ReviewDto> reviewDtolist = page
                .getContent()
                .stream()
                .map(UtilsDto::toReviewDto)
                .collect(Collectors.toList());
        Page<ReviewDto> reviewPage = new PageImpl<>(reviewDtolist, pageable, page.getTotalPages());

        // Set the ETag header to the current ETag value
        return ResponseEntity.ok().eTag(currentETag.toString()).body(reviewPage);
    }

    @PostMapping("/products/{isin}/reviews/search")
    public Page<ReviewDto> fuzzySearchReviewsByIsin(@PathVariable(value = "isin") String isin,
                                                    @RequestParam("searchTerm") String searchTerm,
                                                    Pageable pageable) {
        LOGGER.info("Search for reviews {}", isin);
        Page<Review> page = productReviewService.fuzzyLookupReviewsByIsin(isin, searchTerm, pageable);
        List<ReviewDto> reviewDtolist = page
                .getContent()
                .stream()
                .map(UtilsDto::toReviewDto)
                .collect(Collectors.toList());
        return new PageImpl<>(reviewDtolist, pageable, page.getTotalPages());
    }

    @GetMapping("/reviews")
    public Page<ReviewDto> getAllReviews(Pageable pageable) {
        LOGGER.info("Get all reviews.");

        Page<Review> page = productReviewService.getAllReviews(pageable);

        List<ReviewDto> reviews = page
                .getContent()
                .stream()
                .map(UtilsDto::toReviewDto)
                .collect(Collectors.toList());
        return new PageImpl<>(reviews, pageable, page.getTotalPages());
    }

    @GetMapping("/reviews/search")
    public Page<ReviewDto> fuzzySearchReviews(@RequestParam("searchTerm") String searchTerm,
                                              Pageable pageable) {
        LOGGER.info("Search for reviews {}", searchTerm);
        Page<Review> page = productReviewService.fuzzyLookupReviews(searchTerm, pageable);
        List<ReviewDto> reviewDtolist = page
                .getContent()
                .stream()
                .map(UtilsDto::toReviewDto)
                .collect(Collectors.toList());
        return new PageImpl<>(reviewDtolist, pageable, page.getTotalPages());
    }
}
