package com.lam.service;

public class Helpers {

    public String formatFilterForUrl(String filter) {
        String[] words = filter.split(" ");
        StringBuilder sentence = new StringBuilder(words[0]);
    
        for (int i = 1; i < words.length; ++i) {
            sentence.append("%20");
            sentence.append(words[i]);
        }
    
        return sentence.toString();
    }
    
}