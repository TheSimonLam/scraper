package com.lam.scraper.service;

import com.lam.scraper.models.Listing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

@Service
public class AutotraderScraper {

    @Value("${site.autotrader.url}")
    private String autotraderUrl;
    @Value("${site.autotrader.parse.timeout.ms}")
    Integer parseTimeoutMillis;

    public AutotraderScraper() {
    }

    public List<Listing> scrapeAutotrader(String postcode, Integer maxDistance, String make, String model,
            Integer minPrice, Integer maxPrice, String minYear, String maxYear, Integer maxMileage, String transmission,
            String fuelType) {

        String toStrMaxDistance = filterToUrl("maxDistance", String.valueOf(maxDistance));
        String toStrMinPrice = filterToUrl("minPrice",String.valueOf(minPrice));
        String toStrMaxPrice = filterToUrl("maxPrice", String.valueOf(maxPrice));
        minYear = filterToUrl("minYear", String.valueOf(minYear));
        maxYear = filterToUrl("maxYear", String.valueOf(maxYear));
        String toStrMaxMileage = filterToUrl("maxMileage", String.valueOf(maxMileage));
        transmission = filterToUrl("transmission", String.valueOf(transmission));
        fuelType = filterToUrl("fuelType", String.valueOf(fuelType));

        Helpers autotraderHelper = new Helpers();

        String formattedMake = autotraderHelper.EncodeSpacesForUrl(make);
        String formattedModel = autotraderHelper.EncodeSpacesForUrl(model);



        String html = "https://www.autotrader.co.uk/car-search?advertClassification=standard&make=" + formattedMake
                + "&model=" + formattedModel + toStrMaxDistance + "&postcode=" + postcode + toStrMinPrice
                + toStrMaxPrice + minYear + maxYear + toStrMaxMileage + transmission + fuelType
                + "&onesearchad=Used&onesearchad=Nearly%20New&onesearchad=New&advertising-location=at_cars&is-quick-search=TRUE&page=1";

        try {
            html = Jsoup.connect(html).get().html();
        } catch (Exception e) {
            System.out.println(e);
        }

        return scrape(html);
    }

    public List<Listing> scrape(String html) {

        List<Listing> autoTraderListings = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements scrapedTitles = doc.getElementsByClass("listing-title title-wrap").select("*");
        Elements scrapedPrices = doc.getElementsByClass("vehicle-price").select("*");

        Elements scrapedUrls = null;
        Elements scrapedListingInfoSection = null;

        try {
        scrapedUrls = doc.select("h2.listing-title.title-wrap > a");
        scrapedListingInfoSection = doc.select("ul.listing-key-specs");
        int intTotalListings = scrapedListingInfoSection.size();
        DefaultListModel<String> listYear = new DefaultListModel<>();
        DefaultListModel<String> listMileage = new DefaultListModel<>();

        for (int x = 0; x < intTotalListings; x++) {

            Listing autotraderListing = new Listing();

            // SET TITLE
            autotraderListing.setTitle(scrapedTitles.get(x).text());

            // SET YEAR & MILEAGE
            String checkIfWriteOffIcon = scrapedListingInfoSection.get(x).getElementsByTag("li").first().text();
            if (checkIfWriteOffIcon.equals("CAT Write-off Category Icon")) {
                listYear.addElement(scrapedListingInfoSection.get(x).getElementsByTag("li").get(1).text());
                listMileage.addElement(scrapedListingInfoSection.get(x).getElementsByTag("li").get(3).text());
            } else {
                listYear.addElement(scrapedListingInfoSection.get(x).getElementsByTag("li").first().text());
                listMileage.addElement(scrapedListingInfoSection.get(x).getElementsByTag("li").get(2).text());
            }
            autotraderListing.setYear(listYear.get(x).toString());
            autotraderListing.setMileage(listMileage.get(x).toString());

            // SET PRICE
            autotraderListing.setPrice(scrapedPrices.get(x).text());

            // SET URL
            autotraderListing.setListingUrl(scrapedUrls.get(x).attr("href").toString());

            autoTraderListings.add(autotraderListing);
        }
    } catch (Exception e) {
        System.out.println(e);
    }
    return autoTraderListings;
}

    public String filterToUrl(String filterToFormat, String filter) {

        String filterToUrl = "";

        if (!filter.equals("null") && filter != null) {
            
            switch (filterToFormat) {
                case ("maxDistance"):
                    filterToUrl = "&radius=";
                    break;
                case ("minPrice"):
                    filterToUrl = "&price-from=";
                    break;
                case ("maxPrice"):
                    filterToUrl = "&price-to=";
                    break;
                case ("minYear"):
                    filterToUrl = "&year-from=";
                    break;
                case ("maxYear"):
                    filterToUrl = "&year-to=";
                    break;
                case ("maxMileage"):
                    filterToUrl = "&maximum-mileage=";
                    break;
                case ("transmission"):
                    filterToUrl = "&transmission=";
                    break;
                case ("fuelType"):
                    filterToUrl = "&fuel-type=";
                    break;
            }
            filterToUrl += filter;
            return filterToUrl;
        } else {
            return "";
        }
    }

}