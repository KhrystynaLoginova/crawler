package com.review.crawler.service;

import com.review.crawler.domain.Product;
import com.review.crawler.domain.Review;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CrawlerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerService.class);

    private final String amazonApiEndpoint;
    private final ChromeDriver driver;

    @Autowired
    public CrawlerService(@Value("${amazon.api.endpoint}") String amazonApiEndpoint,
                          WebDriver driver) {
        this.amazonApiEndpoint = amazonApiEndpoint;
        this.driver = (ChromeDriver) driver;
    }

    public Map<Product, List<Review>> loadProductWithReviews(String isin) {

        Map<Product, List<Review>> productWithReview = new HashMap<>();
        Product product = loadProduct(isin);

        String reviewFirstPage = amazonApiEndpoint + "db/product-reviews/" + isin + "/ref=cm_cr_dp_d_show_all_btm?ie=UTF8&reviewerType=all_reviews&filterByStar=critical&sortBy=recent";
        List<Review> reviews = new ArrayList<>();
        collectCriticalReviews(product, reviews, reviewFirstPage, Integer.MAX_VALUE);

        productWithReview.put(product, reviews);
        return productWithReview;
    }

    private Product loadProduct(String isin) {

        String productPage = amazonApiEndpoint + "dp/" + isin;
        LOGGER.info("Go to {}", productPage);
        driver.navigate().to(productPage);

        String description = driver.findElement(By.xpath("//meta[@name='description']")).getAttribute("content");
        String title = driver.findElement(By.xpath("//meta[@name='title']")).getAttribute("content");

        String minPrice = "";
        String maxPrice = "";

        List<String> priceRange = driver.findElements(By.xpath("//span[@class='a-price-range']/span[contains(@class, 'a-price ')]"))
                .stream()
                .map(el -> el.findElement(By.className("a-offscreen")).getAttribute("textContent"))
                .sorted()
                .toList();

        if (!priceRange.isEmpty()) {
            minPrice = priceRange.get(0);
            maxPrice = priceRange.get(priceRange.size() - 1);
        }

        String rating = driver.findElement(By.xpath("//div[@id='averageCustomerReviews']/span/span[@id='acrPopover']"))
                .getAttribute("title");

        Product product = new Product();
        product.setIsin(isin);
        product.setDescription(description);
        product.setTitle(title);
        product.setMinPrice(minPrice);
        product.setMaxPrice(maxPrice);
        product.setRating(rating);

        return product;
    }

    private void collectCriticalReviews(Product product, List<Review> reviews, String reviewPage, int limit) {
        driver.navigate().to(reviewPage);
        List<WebElement> reviewElements = new ArrayList<>();
        try {
            reviewElements = driver.findElements(By.xpath("//div[@data-hook='review']"));
            reviews.addAll(reviewElements
                    .stream()
                    .limit(limit)
                    .map(element -> {
                        Review review = new Review();
                        String reviewDate = element
                                .findElement(By.xpath(".//*[@data-hook='review-date']"))
                                .getText();
                        review.setDate(reviewDate);
                        String reviewData = element
                                .findElement(By.xpath(".//*[@data-hook='review-body']/span"))
                                .getText();
                        review.setText(reviewData);
                        String reviewRating = element
                                .findElement(By.xpath(".//*[contains(@data-hook, 'review-star-rating')]/span"))
                                .getAttribute("textContent");
                        review.setRating(reviewRating);
                        review.setProduct(product);
                        return review;
                    })
                    .toList());
        } catch (Exception exception) {
            LOGGER.error("No reviews at this time=(");
        }

        try {
            WebElement nextPageElement = driver.findElement(By.xpath("//ul[@class='a-pagination']/li[contains(@class, 'a-last')]"));
            if (!nextPageElement.getAttribute("class").contains("a-disabled")) {
                String nextPage = nextPageElement.findElement(By.tagName("a")).getAttribute("href");
                collectCriticalReviews(product, reviews, nextPage, limit - reviewElements.size());
            }
        } catch (Exception exception) {
            LOGGER.error("No Next page for this product.");
        }
    }

    public String getCriticalReviewCountByIsin(String isin) {
        String reviewPage = amazonApiEndpoint + "db/product-reviews/" + isin + "/ref=cm_cr_dp_d_show_all_btm?ie=UTF8&reviewerType=all_reviews&filterByStar=critical&sortBy=recent";
        driver.navigate().to(reviewPage);
        return driver.findElement(By.xpath("//*[@data-hook='cr-filter-info-review-rating-count']")).getText();
    }

    public List<Review> getFirstCriticalReviewForIsin(Product product, int newReviewsCount) {
        String reviewFirstPage = amazonApiEndpoint + "db/product-reviews/" + product.getIsin() + "/ref=cm_cr_dp_d_show_all_btm?ie=UTF8&reviewerType=all_reviews&filterByStar=critical&sortBy=recent";
        List<Review> reviews = new ArrayList<>();
        collectCriticalReviews(product, reviews, reviewFirstPage, newReviewsCount);
        return reviews;
    }
}