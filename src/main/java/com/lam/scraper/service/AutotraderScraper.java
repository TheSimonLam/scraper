package com.lam.scraper.service;

import com.lam.scraper.models.AutotraderListing;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class AutotraderScraper {

    @Value("${site.autotrader.url}")
    private String autotraderUrl;
    @Value("${site.autotrader.parse.timeout.ms}")
    Integer parseTimeoutMillis;

    public AutotraderScraper() {
    }

    public String scrapeAutotrader(String postcode, String maxDistance, String make, String model, Integer minPrice, Integer maxPrice) {

        Helpers autotraderHelper = new Helpers();

        String formattedMake = autotraderHelper.formatFilterForUrl(make);
        String formattedModel = autotraderHelper.formatFilterForUrl(model);

        List<AutotraderListing> autoTraderListings = new ArrayList<>();

        String html = "https://www.autotrader.co.uk/car-search?advertClassification=standard&make=" + formattedMake + "&model=" + formattedModel + "&radius=" + maxDistance + "&postcode=" + postcode + "&price-from=" + minPrice + "&price-to=" + maxPrice + "&onesearchad=Used&onesearchad=Nearly%20New&onesearchad=New&advertising-location=at_cars&is-quick-search=TRUE&page=1";
		try {
			html = Jsoup.connect(html).get().html();
		} catch (Exception e) {
			System.out.println(e);
		}

		Document doc = Jsoup.parse(html);
		Elements elements = doc.getElementsByClass("js-click-handler listing-fpa-link tracking-standard-link").select("*");
		
		for(Element element : elements) {
            AutotraderListing autotraderListing = new AutotraderListing();
            autotraderListing.setTitle(element.text());
            autoTraderListings.add(autotraderListing);
		}
		
		return autoTraderListings.toString();
    }

}