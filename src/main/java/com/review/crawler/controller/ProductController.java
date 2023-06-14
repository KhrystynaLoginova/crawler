package com.review.crawler.controller;

import com.review.crawler.domain.Product;
import com.review.crawler.dto.ProductDto;
import com.review.crawler.service.ProductReviewService;
import com.review.crawler.utils.UtilsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
public class ProductController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewController.class);
    private final ProductReviewService productReviewService;

    @Autowired
    public ProductController(ProductReviewService productReviewService) {
        this.productReviewService = productReviewService;
    }

    @GetMapping("/products")
    public PagedModel<EntityModel<Product>> getAllProducts(Pageable pageable) {
        LOGGER.info("Get all products.");

        Page<Product> productPage = productReviewService.getAllProducts(pageable);

        List<EntityModel<Product>> productModels = productPage.getContent().stream()
                .map(product -> {
                    EntityModel<Product> productModel = EntityModel.of(product);

                    Link selfLink = WebMvcLinkBuilder.linkTo(
                                    WebMvcLinkBuilder.methodOn(ProductController.class).getProduct(product.getIsin()))
                            .withSelfRel();
                    productModel.add(selfLink);
                    return productModel;
                })
                .collect(Collectors.toList());

        // Create a self-link for the product page
        Link selfLink = WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(ProductController.class).getAllProducts(pageable))
                .withSelfRel();

        // Create the PagedModel with self-link and pagination metadata
        return PagedModel.of(productModels, new PagedModel.PageMetadata(
                productPage.getSize(), productPage.getNumber(), productPage.getTotalElements()), selfLink);
    }

    @GetMapping("/products/{isin}")
    public EntityModel<Product> getProduct(@PathVariable String isin) {
        Optional<Product> productOpt = productReviewService.getProductByIsin(isin);

        if (productOpt.isPresent()) {
            EntityModel<Product> productModel = EntityModel.of(productOpt.get());

            Link selfLink = WebMvcLinkBuilder.linkTo(
                            WebMvcLinkBuilder.methodOn(ProductController.class).getProduct(isin))
                    .withSelfRel();
            productModel.add(selfLink);

            return productModel;
        }
        return null;
    }

    @PostMapping("/products/search")
    public ProductDto searchReviews(@RequestParam("isin") String isin) {
        LOGGER.info("Search for product {}", isin);
        Product product = productReviewService.lookupByIsin(isin);
        return UtilsDto.toProductDto(product);
    }
}
