package com.lam.scraper.models;

import java.io.Serializable;

public class AutotraderListing implements Serializable{
    private static final long serialVersionUID = 1L;

    private String title;
    private String year;
    private String price;
    private String mileage;
    private String listingUrl;

    public AutotraderListing() {}
    public AutotraderListing(String title, String year, String price, String mileage, String listingUrl) {
        super();
        this.title = title;
        this.year = year;
        this.price = price;
        this.mileage = mileage;
        this.listingUrl = listingUrl;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getListingUrl() {
        return this.listingUrl;
    }

    public void setListingUrl(String listingUrl) {
        this.listingUrl = listingUrl;
    }

    public String getYear() {
        return this.year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getPrice() {
        return this.price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getMileage() {
        return this.mileage;
    }

    public void setMileage(String mileage) {
        this.mileage = mileage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}