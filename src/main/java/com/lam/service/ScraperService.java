package com.lam.service;

import com.lam.models.*;
import org.springframework.beans.factory.annotation.Value;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ScraperService {

    @Value("${site.autotrader.url}")
    private String autotraderUrl;
    @Value("${site.autotrader.parse.timeout.ms}")
    Integer parseTimeoutMillis;

    public ScraperService() {
    }

    public String scrapeAutotrader(String make, String model, Integer minPrice, Integer maxPrice) {
        List<AutotraderListing> autoTraderListings = new ArrayList<>();

        String html = "";
		try {
			html = Jsoup.connect("https://www.autotrader.co.uk/car-search?advertClassification=standard&make=NISSAN&postcode=LL113HR&model=350%20Z&onesearchad=Used&onesearchad=Nearly%20New&onesearchad=New&advertising-location=at_cars&is-quick-search=TRUE&page=1").get().html();
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