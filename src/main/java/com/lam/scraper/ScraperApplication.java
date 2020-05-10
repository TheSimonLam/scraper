package com.lam.scraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ScraperApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScraperApplication.class, args);
	}

	@GetMapping("/search")
	public String search(@RequestParam(value = "make") String make, @RequestParam(value = "model") String model,
			@RequestParam(value = "minPrice") Integer minPrice, @RequestParam(value = "maxPrice") Integer maxPrice) {

		//TODO: CALL THE JSSoup to scrape all sites
		// 1. AutoTrader
		// 2. Ebay
		// 3. GumTree

		return String.format("Your make is: %s, your model is: %s, your min price is: %d, your max price is: %d", make,
				model, minPrice, maxPrice);
	}
}
