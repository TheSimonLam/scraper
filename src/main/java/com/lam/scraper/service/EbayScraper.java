package com.lam.scraper.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.lam.scraper.models.Listing;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.swing.DefaultListModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Service
@Async
public class EbayScraper {

    // @Value("${site.ebay.url}")
    // private String ebayUrl;
    // @Value("${site.ebay.parse.timeout.ms}")
    // Integer parseTimeoutMillis;

    public EbayScraper() {
    }

    public CompletableFuture<List<Listing>> scrapeEbay(final String postcode, final Integer maxDistance,
            final String make, final String model, final Integer minPrice, final Integer maxPrice, final String minYear,
            final String maxYear, final Integer maxMileage, String transmission, String fuelType) {

        final String toStrMaxDistance = filterToUrl("maxDistance", String.valueOf(maxDistance));
        final String toStrMinPrice = filterToUrl("minPrice", String.valueOf(minPrice));
        final String toStrMaxPrice = filterToUrl("maxPrice", String.valueOf(maxPrice));
        final String minAndMaxYear = modelYearRangeToUrl(minYear, maxYear);
        final String toStrMaxMileage = maxMileageToUrl(String.valueOf(maxMileage));
        transmission = filterToUrl("transmission", String.valueOf(transmission));
        fuelType = filterToUrl("fuelType", String.valueOf(fuelType));

        final Helpers ebayHelper = new Helpers();

        final String strMakeAndModel = make + " " + model;
        final String formattedMakeAndModel = ebayHelper.encodeSpacesForUrl(strMakeAndModel);

        final String html = "https://www.ebay.co.uk/sch/i.html?_sacat=0&_mPrRngCbx=1" + toStrMinPrice + toStrMaxPrice
                + "&_ftrt=901&_ftrv=1&_sabdlo&_sabdhi&_samilow&_samihi" + toStrMaxDistance + "&_stpos=" + postcode
                + "&_fspt=1&_sop=12&_dmd=1&_ipg=50&_fosrp=1" + minAndMaxYear + fuelType + transmission + "&_nkw="
                + formattedMakeAndModel + "&_dcat=9844&rt=nc" + toStrMaxMileage;
        System.out.println("THIS IS THE URL LINK ----->" + html);
        return CompletableFuture.completedFuture(scrape(html));
    }

    public List<Listing> scrape(String html) {

        final List<Listing> ebayListings = new ArrayList<>();

        try {
            html = Jsoup.connect(html).get().html();
        } catch (final Exception e) {
            System.out.println(e);
        }

        final Document doc = Jsoup.parse(html);
        doc.select("span.newly").remove();
        doc.select("wbr").remove();
        final Elements scrapedTitles = doc.select("h3.lvtitle > a").select("*");
        // System.out.println(scrapedTitles);
        final Elements scrapedPrices = doc.select("span.bold").select("*");

        final Elements scrapedUrlListings = doc.select("h3.lvtitle > a").select("*");
        final String[] strUrlListings = extractListingUrls(scrapedUrlListings);

        Elements scrapedListingInfoSection = null;

        try {
            scrapedListingInfoSection = doc.select("ul.lvdetails.left.space-zero.full-width");
            // System.out.println("1234567890 " + scrapedListingInfoSection.toString());
            final int intTotalListings = scrapedListingInfoSection.size();
            final DefaultListModel<String> listMileage = new DefaultListModel<>();
            final DefaultListModel<String> listYear = new DefaultListModel<>();

            for (int x = 0; x < intTotalListings; x++) {

                final Listing ebayListing = new Listing();

                // SET TITLE
                ebayListing.setTitle(scrapedTitles.get(x).text());

                // SET MILEAGE
                final Element listingInfo = scrapedListingInfoSection.get(x);
                final int noOfListItems = listingInfo.getElementsByTag("li").size();
                boolean mileageFound = false;
                boolean regDateFound = false;
                for (int y = 0; y < noOfListItems; y++) {
                    final String strListItem = listingInfo.getElementsByTag("li").get(y).text();
                    if (strListItem.contains("Mileage:")) {
                        listMileage.addElement(strListItem.substring(9, strListItem.length()));
                        mileageFound = true;
                    }
                    if (strListItem.contains("Reg. Date:")) {
                        listYear.addElement(strListItem.substring(11, strListItem.length()));
                        regDateFound = true;
                    }
                }

                if (!mileageFound) {
                    listMileage.addElement("-");
                }
                if (!regDateFound) {
                    String strYearFromTitle = checkTitleForYear(scrapedTitles.get(x).text());
                    if (strYearFromTitle.isEmpty()) {
                        listYear.addElement("-");
                    } else {
                        listYear.addElement(strYearFromTitle);
                    }
                }

                ebayListing.setMileage(listMileage.get(x).toString());

                // SET YEAR
                ebayListing.setYear(listYear.get(x).toString());

                // SET PRICE
                ebayListing.setPrice(scrapedPrices.get(x).text());

                // SET URLS

                ebayListing.setListingUrl(strUrlListings[x]);

                ebayListings.add(ebayListing);

            }
        } catch (final Exception e) {
            System.out.println("EXCEPTION ERROR -> " + e);
        }

        return ebayListings;

    }

    public String checkTitleForYear(String strTitle) {
        strTitle = strTitle.replaceAll("[^0-9]", "#");
        String[] arr = strTitle.split("#");
        StringBuilder values = new StringBuilder();
        for (String s : arr) {
            if (s.matches("^[0-9]{4}$")) {
                values.append(s);
            }
        }
        if(values.length() == 0) {
            return "";
        }
        System.out.println(values.toString());

        return values.toString();
    }

    public String[] extractListingUrls(final Elements scrapedUrlListings) {
        final String[] arrUrlListings = new String[scrapedUrlListings.size()];
        for (int i = 0; i < scrapedUrlListings.size(); i++) {
            final Element scrapedUrlListing = scrapedUrlListings.get(i);
            arrUrlListings[i] = scrapedUrlListing.attr("href");
        }
        return arrUrlListings;
    }

    public String filterToUrl(final String filterToFormat, final String filter) {

        String filterToUrl = "";

        if (!filter.equals("null") && filter != null) {

            switch (filterToFormat) {
                case ("maxDistance"):
                    filterToUrl = "&_sadis=";
                    break;
                case ("minPrice"):
                    filterToUrl = "&_udlo=";
                    break;
                case ("maxPrice"):
                    filterToUrl = "&_udhi=";
                    break;
                case ("maxMileage"):
                    filterToUrl = maxMileageToUrl(filter);
                    break;
                case ("transmission"):
                    filterToUrl = "&Transmission=";
                    break;
                case ("fuelType"):
                    filterToUrl = "&Fuel=";
                    break;
                default:
                    return "";
            }
            filterToUrl += filter;
            return filterToUrl;
        } else {
            return "";
        }
    }

    public String modelYearRangeToUrl(final String minYear, final String maxYear) {
        final int intMinYear = Integer.parseInt(minYear);
        final int intMaxYear = Integer.parseInt(maxYear);

        String urlModelYears = "&Model%2520Year=";

        // (!filter.equals("null") && filter != null)

        if (!maxYear.equals("null") && maxYear != null) {
            for (int i = intMaxYear; i >= intMinYear; i--) {
                if (i == intMinYear) {
                    urlModelYears += String.valueOf(i);
                } else {
                    urlModelYears += (String.valueOf(i) + "%7C");
                }
            }
            return urlModelYears;
        } else {
            return "";
        }

    }

    public String maxMileageToUrl(final String strMaxMileage) {
        final String[] mileageRange = { "Less%2520than%252010%252C000%2520miles",
                "%7C25%252C000%2520to%252049%252C999%2520miles", "%7C50%252C000%2520to%252074%252C999%2520miles",
                "%7C75%252C000%2520to%252099%252C999%2520miles", "More%2520than%2520100%252C000%2520miles" };

        String maxMileageUrl = "&Vehicle%2520Mileage=";

        if (!strMaxMileage.equals("null") && strMaxMileage != null) {
            switch (strMaxMileage) {
                case "10000":
                    maxMileageUrl += mileageRange[0];
                    break;
                case "50000":
                    maxMileageUrl += mileageRange[0] + mileageRange[1];
                    break;
                case "75000":
                    maxMileageUrl += mileageRange[0] + mileageRange[1] + mileageRange[2];
                    break;
                case "100000":
                    maxMileageUrl += mileageRange[0] + mileageRange[1] + mileageRange[2] + mileageRange[3];
                    break;
                case "100000+":
                    maxMileageUrl += mileageRange[4];
                    break;
                default: // Default = Up to 100,000 miles
                    return maxMileageUrl + mileageRange[0] + mileageRange[1] + mileageRange[2] + mileageRange[3];
            }
            return maxMileageUrl;
        } else {
            return "";
        }

    }

}