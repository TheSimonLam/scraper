package com.lam.scraper.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.lam.scraper.models.Listing;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Service
@Async
public class CarsnipScraper {

    public CarsnipScraper() {
    }

    public CompletableFuture<List<Listing>> scrapeCarsnip(final String postcode, final Integer maxDistance,
            final String make, final String model, final Integer minPrice, final Integer maxPrice, final String minYear,
            final String maxYear, final String maxMileage, String transmission, String fuelType) {

        Helpers carsnipHelper = new Helpers();
        final String formattedMake = carsnipHelper.encodeSpacesForUrl(String.valueOf(make));
        final String formattedModel = carsnipHelper.encodeSpacesForUrl(String.valueOf(model));
        final String postcodeToUrl = filterToUrl("postcode", String.valueOf(postcode));
        final String makeToUrl = filterToUrl("make", String.valueOf(formattedMake));
        final String modelToUrl = filterToUrl("model", String.valueOf(formattedModel));
        final String fuelTypeToUrl = filterToUrl("fuelType", String.valueOf(fuelType));
        final String transmissionToUrl = filterToUrl("transmission", String.valueOf(transmission));
        final String maxDistanceToUrl = filterToUrl("maxDistance", String.valueOf(maxDistance));
        final String maxMileageToUrl = filterToUrl("maxMileage", String.valueOf(maxMileage));
        final String priceToAndFromToUrl = combineFromAndToRange(String.valueOf(minPrice), String.valueOf(maxPrice),
                "/price/");
        final String yearToAndFromToUrl = combineFromAndToRange(String.valueOf(minYear), String.valueOf(maxYear),
                "/registrationYear/");

        final String html = "https://www.carsnip.com/search" + postcodeToUrl + maxDistanceToUrl + fuelTypeToUrl
                + transmissionToUrl + makeToUrl + maxMileageToUrl + priceToAndFromToUrl + modelToUrl
                + yearToAndFromToUrl;
        final String htmlCopyForMaxPages = html;
        System.out.println("THIS IS THE URL LINK ----->" + html);
        final int intMaxPages = getMaxPages(htmlCopyForMaxPages);
        List<String> urlsToScrape = buildUrlsToScrape(intMaxPages, html);
        return CompletableFuture.completedFuture(scrape(urlsToScrape, intMaxPages, String.valueOf(maxPrice), carsnipHelper));
    }

    public List<Listing> scrape(List<String> htmlsToScrape, int intMaxPages, String maxPrice, Helpers carsnipHelper) {

        List<Listing> carsnipListings = new ArrayList<>();
        boolean maxPriceReached = false;

        for (String html : htmlsToScrape) {
            if (maxPriceReached) {
                break;
            }

            try {
                html = Jsoup.connect(html).get().html();
            } catch (Exception e) {
                System.out.println(e);
            }

            Document doc = Jsoup.parse(html);
            Elements scrapedTitles = doc.getElementsByClass("Title-sc-1vx0ufl-0 kWPdNG").select("*");
            Elements scrapedPrices = doc.select("a.Price-sc-1xisxeq-0.eskgNq.cs-price > span").select("*");

            Elements scrapedUrls = null;
            Elements scrapedMileage = null;
            Elements scrapedImageUrls = null;

            try {
                scrapedUrls = doc.select("div.Wrapper-sc-6c2rip-0.lenVyH > a");
                scrapedMileage = doc.select("ul.Wrapper-sc-12mo1ed-0.jAHIgj");
                scrapedImageUrls = doc.select("a.AdvertImage__Link-sc-18bwoxc-1.fmqNpw > img");
                int intTotalListings = scrapedTitles.size();
                int intFirstSpan = 0;

                for (int x = 0; x < intTotalListings; x++) {
                    if (!maxPrice.equals("null")) {
                        String listingPriceToInt = scrapedPrices.get(intFirstSpan).text().replace(",", "");
                        listingPriceToInt = listingPriceToInt.replace("£", "");
                        if (Integer.parseInt(listingPriceToInt) > Integer.parseInt(maxPrice)) {
                            maxPriceReached = true;
                            break;
                        }
                    }

                    Listing carsnipListing = new Listing();

                    // SET TITLE
                    carsnipListing.setTitle(scrapedTitles.get(x).text());

                    // SET YEAR & MILEAGE
                    carsnipListing.setYear("-");
                    carsnipListing
                            .setMileage(scrapedMileage.get(x).select("span[itemprop = mileageFromOdometer]").text());

                    // SET PRICE
                    //carsnipListing.setPrice(scrapedPrices.get(intFirstSpan).text());
                    carsnipListing.setPrice(carsnipHelper.formatListingPrice(String.valueOf(scrapedPrices.get(intFirstSpan).text())));
                    intFirstSpan += 3;

                    // SET URL
                    carsnipListing
                            .setListingUrl("https://www.carsnip.com/" + scrapedUrls.get(x).attr("href").toString());

                    // SET IMAGE URL
                    Element scrapedImageUrl = scrapedImageUrls.get(x);
                    carsnipListing.setListingImageAddress(scrapedImageUrl.absUrl("src"));

                    // SET WEBSITE SOURCE
                    carsnipListing.setWebsiteSource("Carsnip");

                    carsnipListings.add(carsnipListing);
                }
            } catch (Exception e) {
                System.out.println("EXCEPTION ERROR trying to apply Carsnip listings -> " + e);
            }
        }

        return carsnipListings;

    }

    public List<String> buildUrlsToScrape(int intMaxPages, String html) {
        List<String> pageUrlsToScrape = new ArrayList<String>();
        pageUrlsToScrape.add(html);

        for (int x = 2; x <= intMaxPages; x++) {
            pageUrlsToScrape.add(html + "?page=" + String.valueOf(x));
        }
        return pageUrlsToScrape;
    }

    public int getMaxPages(String html) {
        try {
            html = Jsoup.connect(html).get().html();
            Document doc = Jsoup.parse(html);
            Element scrapedPages = doc.select("ul.PageList__Page-sc-1g8kg1c-0.bxbxwf.cs-pagination-list").first();
            if (scrapedPages.childrenSize() >= 3) {
                return 3;
            } else {
                return scrapedPages.childrenSize();
            }
        } catch (Exception e) {
            return -1;
        }
    }

    public String combineFromAndToRange(String strFrom, String strTo, String priceOrYear) {
        if (strFrom.equals("null")) {
            strFrom = "";
        }
        if (strTo.equals("null")) {
            strTo = "";
        }

        return priceOrYear + strFrom + "_" + strTo;
    }

    public String filterToUrl(final String filterToFormat, String filter) {

        String filterToUrl = "";

        if (!filter.equals("null") && !filter.equals("")) {

            switch (filterToFormat) {
                case ("make"):
                    filterToUrl = "/manufacturer/";
                    break;
                case ("model"):
                    filterToUrl = "/range/";
                    break;
                case ("maxDistance"):
                    filterToUrl = "_";
                    break;
                case ("minPrice"):
                    filterToUrl = "&min_price=";
                    break;
                case ("maxPrice"):
                    filterToUrl = "&max_price=";
                    break;
                case ("maxMileage"):
                    filterToUrl = "/mileage/_";
                    break;
                case ("transmission"):
                    filterToUrl = "/gearbox/";
                    break;
                case ("fuelType"):
                    filterToUrl = "/fuel/";
                    break;
                case ("postcode"):
                    filterToUrl = "/distance/";
                    break;
                default:
                    return "";
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