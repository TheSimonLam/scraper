package com.lam.scraper.service;

import com.lam.scraper.models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Service
public class ScraperService {

    @Value("${site.autotrader.url}")
    private String autotraderUrl;
    @Value("${site.autotrader.parse.timeout.ms}")
    Integer parseTimeoutMillis;

    public ScraperService() {
    }

    public String scrapeAutotrader(String postcode, String maxDistance, String make, String model, Integer minPrice,
            Integer maxPrice) {
        List<AutotraderListing> autoTraderListings = new ArrayList<>();
        Helpers filterFormatter = new Helpers();

        String formattedMake = filterFormatter.formatFilterForUrl(make);
        String formattedModel = filterFormatter.formatFilterForUrl(model);

        String html = "https://www.autotrader.co.uk/car-search?advertClassification=standard&make=" + formattedMake
                + "&model=" + formattedModel + "&radius=" + maxDistance + "&postcode=" + postcode + "&price-from="
                + minPrice + "&price-to=" + maxPrice
                + "&onesearchad=Used&onesearchad=Nearly%20New&onesearchad=New&advertising-location=at_cars&is-quick-search=TRUE&page=1";
        try {
            html = Jsoup.connect(html).get().html();
        } catch (Exception e) {
            System.out.println(e);
        }

        Document doc = Jsoup.parse(html);
        Elements titleElement = doc.getElementsByClass("js-click-handler listing-fpa-link tracking-standard-link")
                .select("*");
        

        for (Element element : titleElement) {
            AutotraderListing autotraderListing = new AutotraderListing();
            autotraderListing.setTitle(element.text());
            autoTraderListings.add(autotraderListing);
        }

        return autoTraderListings.toString();
    }
}