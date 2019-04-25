package com.isentia.ec;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ProductRatingCrawler {

	public static double getRating(String url,String platform) throws Exception{
		if(platform.equalsIgnoreCase("Amazon")){
			return getProductRatingAmazon(url);
		}else if (platform.equalsIgnoreCase("Rakuten")){
			return getProductRatingRakuten(url);
		}
		return 0.0;
	}
	
	private static double getProductRatingAmazon(String url) throws Exception{
		Document doc = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A").referrer("http://www.yahoo.com").timeout(10000).get();
		String rating = doc.select("span.a-declarative").select("span.a-icon-alt").get(0).text().replace("5つ星のうち ", "").trim();
		double dRating = Double.parseDouble(rating);
		return dRating;
	}
	
	private static double getProductRatingRakuten(String url) throws Exception{
		Document doc = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A").referrer("http://www.yahoo.com").timeout(10000).get();
		Elements e = doc.select("table");
		String ratings = "";
		for (int i = 0; i < e.size(); i++){
			if(e.get(i).attr("data-ratid").equals("ratReviewParts")){
				Element table = e.get(i).select("table").get(2);
				Elements rows = table.select("td");
				ratings = rows.get(2).text();
			}
		}
		double dRatings = Double.parseDouble(ratings);
		return dRatings;
	}
}
