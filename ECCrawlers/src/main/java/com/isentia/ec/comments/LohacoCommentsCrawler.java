package com.isentia.ec.comments;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.isentia.dao.ECDAO;
import com.isentia.ec.base.CommentsCrawler;
import com.isentia.entity.ECComments;
import com.isentia.entity.ECSKU;
import com.isentia.util.DateUtil;

public class LohacoCommentsCrawler implements CommentsCrawler{
	public static void main (String [] args) throws Exception{

		LohacoCommentsCrawler lcc = new LohacoCommentsCrawler();
		ECDAO ecdao = null; 
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
			long channelId = 31L;
			ArrayList <ECSKU> allSKU = ecdao.getAllSKU(args[0], channelId);
			ArrayList<ECComments> allComments = new ArrayList<ECComments>();
			for(ECSKU sku: allSKU){
				allComments = lcc.getAllCommentsToCrawl(sku.getProductId());
				if(allComments.size()>0){
					for(ECComments eccom: allComments){
						eccom.setChannelId(channelId);
						ecdao.insertIntoComments(eccom);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public ArrayList<ECComments> getAllCommentsToCrawl(String productId) throws IOException{
		ArrayList <ECComments> allECComments= new ArrayList<ECComments>(); 
		for(int page=1;page<20;page++){
			String url = "https://lohaco.jp/product/"+productId+"/review/?page="+page;
			Document doc = Jsoup.connect(url).timeout(0).get();
			Elements elements = doc.getElementsByClass("userReview").not(".bgNone");
			for(Element e:elements){
				ECComments ecCom = new ECComments();
				ecCom.setProductId(productId);
				Elements reviewElement = e.select("div.userReview.bgNone").select("p");
				for(int i=0;i<reviewElement.size();i++){
					Element review = (Element)reviewElement.get(i);
					if(i==1){
						String date = review.text().replace("投稿日時：", "");
						try{
							Date dd = DateUtil.convertCorrectDate(date,"yyyy年MM月dd日","dd-MM-yyyy");
							ecCom.setDatetimePost(dd);
						}catch(ParseException ed){
							ed.printStackTrace();
						}
					}
					if(i==0){
						ecCom.setVoiceName(review.select("a").text());
					}
				}
				Elements usrvoteElement = e.select("div.userCommentInner").select("p:not(.usrVote)");
				ecCom.setContent(usrvoteElement.text());
				Elements scoreElement= e.select("dd.numeric");
				int rating = 0;
				try{
					rating = Integer.parseInt(scoreElement.text());
				}catch(Exception ww){
					ww.printStackTrace();
				}
				ecCom.setRating(rating);
				ecCom.setUrl(url);
				allECComments.add(ecCom);
			}
		}
		return allECComments;
	}
}
