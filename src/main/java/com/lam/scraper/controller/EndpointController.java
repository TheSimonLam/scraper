package com.lam.scraper.controller;

import java.util.List;

import com.lam.scraper.models.AutotraderListing;
import com.lam.scraper.service.AutotraderScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EndpointController {

    @Autowired
	AutotraderScraper scraperService;
    
    @GetMapping("/autotrader")
	public List<AutotraderListing> autotraderEndpoint(@RequestParam(value = "postcode") String postcode, @RequestParam(value = "maxDistance") String maxDistance, @RequestParam(value = "make") String make,
			@RequestParam(value = "model") String model, @RequestParam(value = "minPrice") Integer minPrice,
			@RequestParam(value = "maxPrice") Integer maxPrice) {

				List<AutotraderListing> autoTraderResponse = scraperService.scrapeAutotrader(postcode, maxDistance, make, model, minPrice, maxPrice);

		return autoTraderResponse;
	}

}