package com.lam.scraper.controller;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.lam.scraper.models.Listing;
import com.lam.scraper.service.AutotraderScraper;
import com.lam.scraper.service.EbayScraper;
import com.lam.scraper.service.CarsnipScraper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EndpointController {

	@Autowired
	AutotraderScraper autotraderScraper = new AutotraderScraper();

	@Autowired
	EbayScraper ebayScraper = new EbayScraper();

	@Autowired
	CarsnipScraper carsnipScraper = new CarsnipScraper();

	@CrossOrigin(origins = { "http://localhost:3000", "http://www.scraper.com" })
	@GetMapping("/search")
	public List<Listing> carListingEndpoint(
			@RequestParam(value = "postcode") String postcode,
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

				CompletableFuture<List<Listing>> autotraderResponse = autotraderScraper.scrapeAutotrader(postcode, maxDistance,
				make, model, minPrice, maxPrice, minYear, maxYear, maxMileage, transmission, fuelType);

				CompletableFuture<List<Listing>> ebayResponse = ebayScraper.scrapeEbay(postcode, maxDistance, make, model,
				minPrice, maxPrice, minYear, maxYear, maxMileage, transmission, fuelType);
		
				CompletableFuture<List<Listing>> carsnipResponse = carsnipScraper.scrapeCarsnip(postcode, maxDistance, make, model,
				minPrice, maxPrice, minYear, maxYear, maxMileage, transmission, fuelType);

				CompletableFuture.allOf(autotraderResponse, ebayResponse, carsnipResponse).join();

				List<Listing> allListings = autotraderResponse.get();
				allListings.addAll(ebayResponse.get());
				allListings.addAll(carsnipResponse.get());

				return allListings;

	}

}