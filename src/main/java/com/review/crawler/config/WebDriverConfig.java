package com.review.crawler.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebDriverConfig {

    @Bean
    public WebDriver chromeDriver() {
        System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "/chromeDir/chromedriver");
        return new ChromeDriver();
    }
}

