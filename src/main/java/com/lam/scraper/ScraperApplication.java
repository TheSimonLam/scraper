package com.lam.scraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@SpringBootApplication
@RestController
public class ScraperApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScraperApplication.class, args);
	}

	// @GetMapping("/search")
	// public String search(@RequestParam(value = "make") String make,
	// @RequestParam(value = "model") String model,
	// @RequestParam(value = "minPrice") Integer minPrice, @RequestParam(value =
	// "maxPrice") Integer maxPrice) {

	// Document doc = null;

	// try {
	// doc = Jsoup.connect("http://en.wikipedia.org/").get();
	// }
	// catch(Exception e) {
	// System.out.print(e);
	// }

	// log(doc.title());

	// Elements newsHeadlines = doc.select("#mp-itn b a");
	// for (Element headline : newsHeadlines) {
	// log("%s\n\t%s", headline.attr("title"), headline.absUrl("href"));
	// }

	// return String.format("Your make is: %s, your model is: %s, your min price is:
	// %d, your max price is: %d", make,
	// model, minPrice, maxPrice);
	// }

	// private static void log(String msg, String... vals) {
	// System.out.println(String.format(msg, vals));
	// }

	@RequestMapping("/350z")
	public ArrayList firstListing() {
		String html = "";
		try {
			html = Jsoup.connect("https://www.autotrader.co.uk/car-search?advertClassification=standard&make=NISSAN&postcode=LL113HR&model=350%20Z&onesearchad=Used&onesearchad=Nearly%20New&onesearchad=New&advertising-location=at_cars&is-quick-search=TRUE&page=1").get().html();
		} catch (Exception e) {
			System.out.println(e);
		}

		Document doc = Jsoup.parse(html);
		Elements elements = doc.getElementsByClass("js-click-handler listing-fpa-link tracking-standard-link").select("*");
		ArrayList<String> cars = new ArrayList<String>();
		
		for(Element element : elements) {
			cars.add(element.text());
		}
		
		return cars;
	}

}
