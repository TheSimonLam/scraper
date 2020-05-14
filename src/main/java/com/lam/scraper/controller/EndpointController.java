package com.lam.scraper.controller;

import java.util.List;

import com.lam.scraper.models.AutotraderListing;
import com.lam.scraper.service.AutotraderScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EndpointController {

	@Autowired
	AutotraderScraper scraperService;

	@CrossOrigin(origins = {"http://localhost:3000", "http://www.scraper.com"})
	@GetMapping("/autotrader")
	public List<AutotraderListing> autotraderEndpoint(@RequestParam(value = "postcode") String postcode,
			@RequestParam(required = false, value = "maxDistance") Integer maxDistance, @RequestParam(value = "make") String make,
			@RequestParam(value = "model") String model, @RequestParam(required = false, value = "minPrice") Integer minPrice,
			@RequestParam(required = false, value = "maxPrice") Integer maxPrice, @RequestParam(required = false, value = "minYear") String minYear,
			@RequestParam(required = false, value = "maxYear") String maxYear, @RequestParam(required = false, value = "maxMileage") Integer maxMileage,
			@RequestParam(required = false, value = "transmission") String transmission, @RequestParam(required = false, value = "fuelType") String fuelType) {

		List<AutotraderListing> autoTraderResponse = scraperService.scrapeAutotrader(postcode, maxDistance, make, model,
				minPrice, maxPrice, minYear, maxYear, maxMileage, transmission, fuelType);

		return autoTraderResponse;
	}

}