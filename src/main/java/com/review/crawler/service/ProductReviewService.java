package com.review.crawler.service;

import com.review.crawler.model.Etag;
import com.review.crawler.domain.Product;
import com.review.crawler.domain.Review;
import com.review.crawler.repository.ProductRepository;
import com.review.crawler.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductReviewService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductReviewService.class);

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final CrawlerService crawlerService;

    @Autowired
    public ProductReviewService(ProductRepository productRepository,
                                ReviewRepository reviewRepository,
                                CrawlerService crawlerService) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.crawlerService = crawlerService;
    }

    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    public Product lookupByIsin(String isin) {
        Product product = null;
        Optional<Product> productOpt = productRepository.findByIsin(isin);
        if (productOpt.isPresent()) {
            product = productOpt.get();
        } else {
            Map<Product, List<Review>> productWithReviews = crawlerService.loadProductWithReviews(isin);
            for (Map.Entry<Product, List<Review>> entry : productWithReviews.entrySet()) {
                product = entry.getKey();
                saveProduct(product);
                saveReviews(entry.getValue());
            }
        }
        return product;
    }

    public Page<Review> lookupReviewsByIsin(String isin, Pageable pageable) {
        Product product = lookupByIsin(isin);
        if (product != null) {
            return reviewRepository.searchByProductId(product.getId(), pageable);
        }
        return null;
    }

    public Page<Review> fuzzyLookupReviewsByIsin(String isin, String searchTerm, Pageable pageable) {
        Product product = lookupByIsin(isin);
        if (product != null) {
            return reviewRepository.searchByProductIdAndReviewText(product.getId(), searchTerm, pageable);
        }
        return null;
    }

    public Page<Review> fuzzyLookupReviews(String searchTerm, Pageable pageable) {
        return reviewRepository.searchByReviewText(searchTerm, pageable);
    }

    public void saveReviews(List<Review> reviews) {
        reviewRepository.saveAll(reviews);
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Review> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    public Optional<Product> getProductByIsin(String isin) {
        return productRepository.findByIsin(isin);
    }

    public Etag getReviewsCountByIsin(String isin) {
        String etag = crawlerService.getCriticalReviewCountByIsin(isin);
        LOGGER.info("Current eTag string information is {} isin {}", etag, isin);
        return parseStringToEtag(etag);
    }

    public Page<Review> lookupUpdatesByIsin(Etag previousEtag, Etag currentETag, String isin, Pageable pageable) {

        Optional<Product> productOpt = productRepository.findByIsin(isin);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();

            if (!previousEtag.getRating().equals(currentETag.getRating())) {
                productRepository.save(product);
            }
            int newReviewsCount = currentETag.getReviews() - previousEtag.getReviews();
            if (newReviewsCount > 0) {
                List<Review> addedReviews = crawlerService.getFirstCriticalReviewForIsin(product, newReviewsCount);
                reviewRepository.saveAll(addedReviews);
            }
            return reviewRepository.searchByProductId(product.getId(), pageable);
        }
        return null;
    }

    public Etag parseStringToEtag(String etagStr) {
        if (etagStr != null && !etagStr.isBlank()) {
            String[] parts = etagStr.trim().split(",");
            if (parts.length == 2) {
                return new Etag(parts[0].trim(), Integer.parseInt(parts[1].trim().split(" ")[0]));
            }
        }
        return null;
    }
}

