package com.lam.scraper.service;

import com.lam.scraper.models.AutotraderListing;
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

    public List<AutotraderListing> scrapeAutotrader(String postcode, String maxDistance, String make, String model,
            Integer minPrice, Integer maxPrice) {

        Helpers autotraderHelper = new Helpers();

        String formattedMake = autotraderHelper.formatFilterForUrl(make);
        String formattedModel = autotraderHelper.formatFilterForUrl(model);

        List<AutotraderListing> autoTraderListings = new ArrayList<>();

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

                AutotraderListing autotraderListing = new AutotraderListing();

                //SET TITLE
                autotraderListing.setTitle(scrapedTitles.get(x).text());

                //SET YEAR & MILEAGE
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

                //SET PRICE
                autotraderListing.setPrice(scrapedPrices.get(x).text());

                //SET URL
                autotraderListing.setListingUrl(scrapedUrls.get(x).attr("href").toString());

                autoTraderListings.add(autotraderListing);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        return autoTraderListings;
    }

}