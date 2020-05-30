package com.lam.scraper.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Helpers {

    public String encodeSpacesForUrl(String filter) {
        String[] words = filter.split(" ");
        StringBuilder sentence = new StringBuilder(words[0]);

        for (int i = 1; i < words.length; ++i) {
            sentence.append("%20");
            sentence.append(words[i]);
        }

        return sentence.toString();
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

}