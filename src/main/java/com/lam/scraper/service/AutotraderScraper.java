package com.lam.scraper.service;

import com.lam.scraper.models.Listing;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.swing.DefaultListModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Service
@Async
public class AutotraderScraper {

    public AutotraderScraper() {
    }

    public CompletableFuture<List<Listing>> scrapeAutotrader(String postcode, Integer maxDistance, String make,
            String model, Integer minPrice, Integer maxPrice, String minYear, String maxYear, String maxMileage,
            String transmission, String fuelType) {

        String toStrMaxDistance = filterToUrl("maxDistance", String.valueOf(maxDistance));
        String toStrMinPrice = filterToUrl("minPrice", String.valueOf(minPrice));
        String toStrMaxPrice = filterToUrl("maxPrice", String.valueOf(maxPrice));
        minYear = filterToUrl("minYear", String.valueOf(minYear));
        maxYear = filterToUrl("maxYear", String.valueOf(maxYear));
        String toStrMaxMileage = filterToUrl("maxMileage", String.valueOf(maxMileage));
        transmission = filterToUrl("transmission", String.valueOf(transmission));

        Helpers autotraderHelper = new Helpers();

        String formattedMake = autotraderHelper.encodeSpacesForUrl(String.valueOf(make));
        String formattedModel = autotraderHelper.encodeSpacesForUrl(String.valueOf(model));
        formattedMake = filterToUrl("make", String.valueOf(formattedMake));
        formattedModel = filterToUrl("model", String.valueOf(formattedModel));
        List<String> fuelTypesToList = autotraderHelper.decodeApiInput(fuelType);
        String fuelTypeToUrl = buildFuelTypeForUrl(fuelTypesToList);

        String html = "https://www.autotrader.co.uk/car-search?sort=relevance&postcode=" + postcode + toStrMaxDistance
                + formattedMake + formattedModel + toStrMinPrice + toStrMaxPrice + minYear + maxYear + toStrMaxMileage
                + transmission + fuelTypeToUrl + "&page=";
        String htmlGetMaxPages = html + "1";

        try {
            htmlGetMaxPages = Jsoup.connect(htmlGetMaxPages).get().html();
        } catch (Exception e) {
            System.out.println("EXCEPTION ERROR trying to connect to Autotrader pages -> " + e);
        }

        int intMaxPages = getMaxPages(htmlGetMaxPages);
        List<String> pageUrlsToScrape = buildUrlsToScrape(intMaxPages, html);
        System.out.println("THIS IS THE URL LINK ----->" + html);
        return CompletableFuture.completedFuture(scrape(pageUrlsToScrape, intMaxPages));
    }

    public String buildFuelTypeForUrl(List<String> fuelTypes) {
        String fuelTypeToUrl = "";
        if (fuelTypes != null && !fuelTypes.isEmpty()) {
            for (String fuelType : fuelTypes) {
                switch (fuelType) {
                    case "petrol":
                        fuelTypeToUrl += "&fuel-type=Petrol";
                        break;
                    case "diesel":
                        fuelTypeToUrl += "&fuel-type=Diesel";
                        break;
                    case "electric":
                        fuelTypeToUrl += "&fuel-type=Electric";
                    case "hybrid":
                        fuelTypeToUrl += "&fuel-type=Hybrid%20–%20Diesel%2FElectric%20Plug-in&fuel-type=Hybrid%20–%20Petrol%2FElectric&fuel-type=Hybrid%20–%20Petrol%2FElectric%20Plug-in";
                }
            }
        }
        return fuelTypeToUrl;
    }

    public int getMaxPages(String html) {

        Document doc = Jsoup.parse(html);
        DefaultListModel<String> pages = new DefaultListModel<>();
        int maxPages = 0;

        try {
            Element scrapedPages = doc.select("ul.pagination--ul").first();
            for (int x = 1; x <= 5; x++) {
                pages.addElement(scrapedPages.getElementsByTag("li").get(x).text());
                String strPageNo = pages.get(0).toString();
                if (strPageNo != null && !strPageNo.toString().equals("null")) {
                    maxPages++;
                }
            }
        } catch (Exception e) {
            return maxPages;
        }
        return maxPages;
    }

    public List<String> buildUrlsToScrape(int intMaxPages, String html) {
        List<String> pageUrlsToScrape = new ArrayList<String>();

        for (int x = 1; x <= intMaxPages; x++) {
            pageUrlsToScrape.add(html + String.valueOf(x));
        }

        return pageUrlsToScrape;
    }

    public List<Listing> scrape(List<String> htmlsToScrape, int intMaxPages) {

        List<Listing> autoTraderListings = new ArrayList<>();
        int counter = 0;

        for (String html : htmlsToScrape) {

            try {
                html = Jsoup.connect(html).get().html();
            } catch (Exception e) {
                System.out.println(e);
            }

            Document doc = Jsoup.parse(html);
            Elements scrapedTitles = doc.getElementsByClass("listing-title title-wrap").select("*");
            Elements scrapedPrices = doc.getElementsByClass("vehicle-price").select("*");

            Elements scrapedUrls = null;
            Elements scrapedListingInfoSection = null;
            Elements scrapedImageUrls = null;
            Elements scrapedListingContainer = null;

            try {
                scrapedUrls = doc.select("h2.listing-title.title-wrap > a");
                scrapedListingInfoSection = doc.select("ul.listing-key-specs");
                scrapedImageUrls = doc.select("a.js-click-handler.listing-fpa-link.tracking-standard-link > img");
                scrapedListingContainer = doc.select("li.search-page__result");
                int intTotalListings = scrapedListingInfoSection.size();
                DefaultListModel<String> listYear = new DefaultListModel<>();
                DefaultListModel<String> listMileage = new DefaultListModel<>();

                for (int x = 0; x < intTotalListings; x++) {

                    Listing autotraderListing = new Listing();
                    if (scrapedListingContainer.get(x).hasAttr("span")) {
                        if (scrapedListingContainer.get(x).getElementsByTag("span").first().text() != null) {
                            if (scrapedListingContainer.get(x).getElementsByTag("span").first().text()
                                    .equals("Promoted listing")
                                    || scrapedListingContainer.get(x).getElementsByTag("span").first().text()
                                            .equals("You may also like")) {
                                continue;
                            }
                        } else {
                            // SET TITLE
                            autotraderListing.setTitle(scrapedTitles.get(x).text());

                            // SET YEAR & MILEAGE
                            String checkIfWriteOffIcon = scrapedListingInfoSection.get(x).getElementsByTag("li").first()
                                    .text();
                            if (checkIfWriteOffIcon.equals("CAT Write-off Category Icon")) {
                                listYear.addElement(
                                        scrapedListingInfoSection.get(x).getElementsByTag("li").get(1).text());
                                listMileage.addElement(
                                        scrapedListingInfoSection.get(x).getElementsByTag("li").get(3).text());
                            } else {
                                listYear.addElement(
                                        scrapedListingInfoSection.get(x).getElementsByTag("li").first().text());
                                listMileage.addElement(
                                        scrapedListingInfoSection.get(x).getElementsByTag("li").get(2).text());
                            }
                            autotraderListing.setYear(listYear.get(x).toString());
                            autotraderListing.setMileage(listMileage.get(x).toString());

                            // SET PRICE
                            autotraderListing.setPrice(scrapedPrices.get(x).text());

                            // SET URL
                            autotraderListing.setListingUrl(
                                    "https://www.autotrader.co.uk" + scrapedUrls.get(x).attr("href").toString());

                            // SET IMAGE URL
                            Element scrapedImageUrl = scrapedImageUrls.get(x);
                            autotraderListing.setListingImageAddress(scrapedImageUrl.absUrl("src"));

                            // SET WEBSITE SOURCE
                            autotraderListing.setWebsiteSource("Autotrader");

                            autoTraderListings.add(autotraderListing);
                            counter++;
                        }
                    }

                }
            } catch (Exception e) {
                System.out.println("EXCEPTION ERROR applying Autotrader listings -> " + e);
            }
        }
        return autoTraderListings;
    }

    public String filterToUrl(String filterToFormat, String filter) {

        String filterToUrl = "";

        if (!filter.equals("null") && filter != null) {

            switch (filterToFormat) {
                case ("make"):
                    filterToUrl = "&make=";
                    break;
                case ("model"):
                    filterToUrl = "&model=";
                    break;
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
            }
            if (filter.equals("100000+")) {
                filter = "500000";
            }
            filterToUrl += filter;
            return filterToUrl;
        } else {
            return "";
        }
    }

}