package com.lam.scraper.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Helpers {

    public String encodeSpacesForUrl(String filter) {
        if (filter.equals("null") || filter.contains("null")) {
            return "";
        }
        String[] words = filter.split(" ");
        StringBuilder sentence = new StringBuilder(words[0]);

        for (int i = 1; i < words.length; ++i) {
            sentence.append("%20");
            sentence.append(words[i]);
        }

        return sentence.toString();
    }

    public String decodeSpacesForTitleCheck(String title) {
        if (title.equals("null") || title.contains("null")) {
            return "";
        }

        return title.replaceAll("%20", " ");
    }

    public List<String> decodeApiInput(String filter) {
        if (filter == null) {
            return Collections.emptyList();
        } else {
            String[] elements = filter.split(",");
            List<String> fixedLengthList = Arrays.asList(elements);
            ArrayList<String> listOfString = new ArrayList<String>(fixedLengthList);
            return listOfString;
        }
    }

    public String formatListingPrice(String price) {
        if (!price.equals("null")) {
            if (price.contains("£")) {
                price = price.replace("£", "");
            }
            if (price.contains(",")) {
                price = price.replace(",", "");
            }
            if (price.contains(".")) {
                int strDecimalPlace = price.indexOf('.');
                price = price.substring(0, strDecimalPlace);
            }
            return price;
        } else {
            return "-";
        }
    }

}