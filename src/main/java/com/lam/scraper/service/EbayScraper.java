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

    public EbayScraper() {
    }

    public CompletableFuture<List<Listing>> scrapeEbay(final String postcode, final Integer maxDistance,
            final String make, final String model, final Integer minPrice, final Integer maxPrice, final String minYear,
            final String maxYear, final String maxMileage, String transmission, String fuelType) {

        final String toStrMaxDistance = filterToUrl("maxDistance", String.valueOf(maxDistance));
        final String toStrMinPrice = filterToUrl("minPrice", String.valueOf(minPrice));
        final String toStrMaxPrice = filterToUrl("maxPrice", String.valueOf(maxPrice));
        final String minAndMaxYear = modelYearRangeToUrl(String.valueOf(minYear), String.valueOf(maxYear));
        final String toStrMaxMileage = maxMileageToUrl(String.valueOf(maxMileage));
        transmission = filterToUrl("transmission", String.valueOf(transmission));

        final Helpers ebayHelper = new Helpers();

        String strMakeAndModel = ebayHelper.encodeSpacesForUrl(String.valueOf(make)) + " "
                + ebayHelper.encodeSpacesForUrl(String.valueOf(model));
        strMakeAndModel = filterToUrl("makeAndModel", strMakeAndModel);
        List<String> fuelTypesToList = ebayHelper.decodeApiInput(fuelType);
        String fuelTypeToUrl = buildFuelTypeForUrl(fuelTypesToList);

        final String html = "https://www.ebay.co.uk/sch/i.html?_sacat=0&_mPrRngCbx=1" + toStrMinPrice + toStrMaxPrice
                + "&_ftrt=901&_ftrv=1&_sabdlo&_sabdhi&_samilow&_samihi" + toStrMaxDistance + "&_stpos=" + postcode
                + "&_fspt=1&_sop=12&_dmd=1&_ipg=50&_fosrp=1" + minAndMaxYear + fuelTypeToUrl + transmission
                + strMakeAndModel + "&_dcat=9844&rt=nc" + toStrMaxMileage;
        System.out.println("THIS IS THE URL LINK ----->" + html);
        return CompletableFuture.completedFuture(scrape(html, ebayHelper));
    }

    public List<Listing> scrape(String html, Helpers ebayHelper) {

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
        final Elements scrapedPrices = doc.select("span.bold").select("*");

        final Elements scrapedUrlListings = doc.select("h3.lvtitle > a").select("*");
        final String[] strUrlListings = extractListingUrls(scrapedUrlListings);

        Elements scrapedListingInfoSection = null;
        Elements scrapedImageUrls = null;
        Elements scrapedListingContainer = null;

        try {
            scrapedListingInfoSection = doc.select("ul.lvdetails.left.space-zero.full-width");
            scrapedImageUrls = doc.select("div.lvpic.pic.img.left > div > a > img");
            scrapedListingContainer = doc.select("ul#ListViewInner > li");
            final int intTotalListings = scrapedListingInfoSection.size();
            final DefaultListModel<String> listMileage = new DefaultListModel<>();
            final DefaultListModel<String> listYear = new DefaultListModel<>();

            for (int x = 0; x < intTotalListings; x++) {
                String endOfListClassName = scrapedListingContainer.get(x).text();
                if (!endOfListClassName.isEmpty() && endOfListClassName.equals("Results matching fewer words")) {
                    break;
                }

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
                ebayListing.setPrice(ebayHelper.formatListingPrice(String.valueOf(scrapedPrices.get(x).text())));

                // SET LISTING URL
                ebayListing.setListingUrl(strUrlListings[x]);

                // SET LISTING IMAGE URL
                Element scrapedImageUrl = scrapedImageUrls.get(x);
                if (scrapedImageUrl.attr("src").contains("1x2.gif")) {
                    ebayListing.setListingImageAddress(scrapedImageUrl.attr("imgurl"));
                } else {
                    ebayListing.setListingImageAddress(scrapedImageUrl.attr("src"));
                }

                // SET WEBSITE SOURCE
                ebayListing.setWebsiteSource("Ebay");

                ebayListings.add(ebayListing);

            }
        } catch (final Exception e) {
            System.out.println("EXCEPTION ERROR trying to apply Ebay listings -> " + e);
        }

        return ebayListings;

    }

    public String buildFuelTypeForUrl(List<String> fuelTypes) {
        String fuelTypeToUrl = "&Fuel=";
        boolean firstFuelTypeAdded = false;
        if (fuelTypes != null && !fuelTypes.isEmpty()) {
            for (String fuelType : fuelTypes) {
                if (firstFuelTypeAdded) {
                    fuelTypeToUrl += "&7c";
                }
                switch (fuelType) {
                    case "petrol":
                        fuelTypeToUrl += "Petrol";
                        break;
                    case "diesel":
                        fuelTypeToUrl += "Diesel";
                        break;
                    case "electric":
                        fuelTypeToUrl += "Electricity";
                    case "hybrid":
                        fuelTypeToUrl += "Hybrid%7CPetrol%252FElectricity&_dcat=9837";
                }
                if (!fuelTypeToUrl.equals("&Fuel=")) {
                    firstFuelTypeAdded = true;
                }
            }
        }
        return fuelTypeToUrl;
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
        if (values.length() == 0) {
            return "";
        }

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

    public String filterToUrl(final String filterToFormat, String filter) {

        String filterToUrl = "";

        if (!filter.equals("null")) {

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
                case ("makeAndModel"):
                    if (filter.equals("")) {
                        filter = "cars for sale";
                    }
                    filterToUrl = "&_nkw=";
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

    public String fuelTypeToUrl(String fuelType) {
        switch (fuelType) {
            case "petrol":
                return "petrol";
            case "diesel":
                return "diesel";
            case "electric":
                break;
            case "hybrid":
                return "Hybrid%20–%20Diesel%2FElectric%20Plug-in&fuel-type=Hybrid%20–%20Petrol%2FElectric&fuel-type=Hybrid%20–%20Petrol%2FElectric%20Plug-in";
            case "unlisted":
                break;
        }
        return "";
    }

    public String modelYearRangeToUrl(String minYear, String maxYear) {

        if (maxYear.equals("null")) {
            maxYear = "2020";
        }
        if (minYear.equals("null")) {
            minYear = "1980";
        }
        final int intMinYear = Integer.parseInt(minYear);
        final int intMaxYear = Integer.parseInt(maxYear);

        String urlModelYears = "&Model%2520Year=";

        for (int i = intMaxYear; i >= intMinYear; i--) {
            if (i == intMinYear) {
                urlModelYears += String.valueOf(i);
            } else {
                urlModelYears += (String.valueOf(i) + "%7C");
            }
        }
        return urlModelYears;

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