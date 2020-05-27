package com.lam.scraper.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.lam.scraper.models.Listing;
import com.lam.scraper.service.AutotraderScraper;
import com.lam.scraper.service.EbayScraper;
import com.lam.scraper.service.CarsnipScraper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EndpointController {

	@Autowired
	AutotraderScraper autotraderScraper = new AutotraderScraper();

	private static final Logger logger = LoggerFactory.getLogger(EndpointController.class);

	@Autowired
	EbayScraper ebayScraper = new EbayScraper();

	@Autowired
	CarsnipScraper gumtreeScraper = new CarsnipScraper();

	@CrossOrigin(origins = { "http://localhost:3000", "http://www.scraper.com" })
	@GetMapping("/autotrader")
	public CompletableFuture<List<Listing>> autotraderEndpoint(@RequestParam(value = "postcode") String postcode,
			@RequestParam(required = false, value = "maxDistance") Integer maxDistance,
			@RequestParam(value = "make") String make, @RequestParam(value = "model") String model,
			@RequestParam(required = false, value = "minPrice") Integer minPrice,
			@RequestParam(required = false, value = "maxPrice") Integer maxPrice,
			@RequestParam(required = false, value = "minYear") String minYear,
			@RequestParam(required = false, value = "maxYear") String maxYear,
			@RequestParam(required = false, value = "maxMileage") Integer maxMileage,
			@RequestParam(required = false, value = "transmission") String transmission,
			@RequestParam(required = false, value = "fuelType") String fuelType)
			throws InterruptedException, ExecutionException {

		long start = System.currentTimeMillis();

		CompletableFuture<List<Listing>> autoTraderResponse1 = autotraderScraper.scrapeAutotrader(postcode, maxDistance,
				make, model, minPrice, maxPrice, minYear, maxYear, maxMileage, transmission, fuelType);

		// CompletableFuture<List<Listing>> autoTraderResponse2 =
		// autotraderScraper.scrapeAutotrader(postcode, maxDistance,
		// make, model, minPrice, maxPrice, minYear, maxYear, maxMileage, transmission,
		// fuelType);

		// CompletableFuture<List<Listing>> autoTraderResponse3 =
		// autotraderScraper.scrapeAutotrader(postcode, maxDistance,
		// make, model, minPrice, maxPrice, minYear, maxYear, maxMileage, transmission,
		// fuelType);

		CompletableFuture.allOf(autoTraderResponse1/* , autoTraderResponse2, autoTraderResponse3 */).join();

		logger.info("Elapsed time: " + (System.currentTimeMillis() - start));

		return autoTraderResponse1;
	}

	@CrossOrigin(origins = { "http://localhost:3000", "http://www.scraper.com" })
	@GetMapping("/ebay")
	public CompletableFuture<List<Listing>> ebayEndpoint(@RequestParam(value = "postcode") String postcode,
			@RequestParam(required = false, value = "maxDistance") Integer maxDistance,
			@RequestParam(value = "make") String make, @RequestParam(value = "model") String model,
			@RequestParam(required = false, value = "minPrice") Integer minPrice,
			@RequestParam(required = false, value = "maxPrice") Integer maxPrice,
			@RequestParam(required = false, value = "minYear") String minYear,
			@RequestParam(required = false, value = "maxYear") String maxYear,
			@RequestParam(required = false, value = "maxMileage") Integer maxMileage,
			@RequestParam(required = false, value = "transmission") String transmission,
			@RequestParam(required = false, value = "fuelType") String fuelType) {

		CompletableFuture<List<Listing>> ebayResponse = ebayScraper.scrapeEbay(postcode, maxDistance, make, model,
				minPrice, maxPrice, minYear, maxYear, maxMileage, transmission, fuelType);

		return ebayResponse;
	}

	@CrossOrigin(origins = { "http://localhost:3000", "http://www.scraper.com" })
	@GetMapping("/carsnip")
	public CompletableFuture<List<Listing>> gumtreeEndpoint(@RequestParam(value = "postcode") String postcode,
			@RequestParam(required = false, value = "maxDistance") Integer maxDistance,
			@RequestParam(value = "make") String make, @RequestParam(value = "model") String model,
			@RequestParam(required = false, value = "minPrice") Integer minPrice,
			@RequestParam(required = false, value = "maxPrice") Integer maxPrice,
			@RequestParam(required = false, value = "minYear") String minYear,
			@RequestParam(required = false, value = "maxYear") String maxYear,
			@RequestParam(required = false, value = "maxMileage") Integer maxMileage,
			@RequestParam(required = false, value = "transmission") String transmission,
			@RequestParam(required = false, value = "fuelType") String fuelType) {

		CompletableFuture<List<Listing>> gumtreeResponse = gumtreeScraper.scrapeCarsnip(postcode, maxDistance, make, model,
				minPrice, maxPrice, minYear, maxYear, maxMileage, transmission, fuelType);

		return gumtreeResponse;
	}

}