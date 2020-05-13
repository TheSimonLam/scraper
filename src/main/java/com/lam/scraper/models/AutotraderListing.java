package com.lam.scraper.models;

import java.io.Serializable;

public class AutotraderListing implements Serializable{
    private static final long serialVersionUID = 1L;

    private String title;
    
    public AutotraderListing() {}
    public AutotraderListing(String title) {
        super();
        this.title = title;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}