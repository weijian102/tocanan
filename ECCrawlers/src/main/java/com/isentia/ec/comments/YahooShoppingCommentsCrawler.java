package com.isentia.ec.comments;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.isentia.dao.ECDAO;
import com.isentia.ec.base.CommentsCrawler;
import com.isentia.entity.ECComments;
import com.isentia.entity.ECSKU;
import com.isentia.util.DateUtil;

public class YahooShoppingCommentsCrawler implements CommentsCrawler{
	final static Logger logger = Logger.getLogger(YahooShoppingCommentsCrawler.class);
	public static void main (String [] args){
		YahooShoppingCommentsCrawler lcc = new YahooShoppingCommentsCrawler();
		ECDAO ecdao = null; 
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
			long channelId = 33L;
			ArrayList <ECSKU> allSKU = ecdao.getAllSKU(args[0], channelId);
			ArrayList<ECComments> allComments = new ArrayList<ECComments>();
			for(ECSKU sku: allSKU){
				allComments = lcc.getAllCommentsToCrawl(sku.getProductId());
				if(allComments.size()>0){
					for(ECComments eccom: allComments){
						eccom.setChannelId(channelId);
						try {
							ecdao.insertIntoComments(eccom);
						}catch(SQLException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
		public ArrayList<ECComments> getAllCommentsToCrawl(String productId) throws IOException{
			ArrayList <ECComments> allECComments= new ArrayList<ECComments>(); 
			String sReviewURL =  productId+"&sort=-latest";
			int pageFlip =20;
			for(int commentPage=1;commentPage<=pageFlip;commentPage++){
				if(commentPage ==1){
					try{
						allECComments = getComments(productId,sReviewURL,allECComments);
					}catch(Exception e){
						e.printStackTrace();
					}
				}else{
					String commentUrl = sReviewURL+"&start="+((commentPage*10)+1);
//					System.out.println(commentUrl);
					try{
						allECComments = getComments(productId,commentUrl,allECComments);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				
			}		
			return allECComments;
		}
		
		
		public ArrayList <ECComments> getComments(String productId,String url,ArrayList<ECComments> eccomList) throws Exception{
			logger.debug(url);
			Document doc = Jsoup.connect(url).timeout(0).get();
			Elements ul = doc.select("div.elItem > ul"); 
			Elements li = ul.select("li");
			for (int i = 0; i < li.size(); i++) {
				String date = li.get(i).select("dd.elDate").text();
				if(date.length()>0){
					ECComments ec = new ECComments();
					ec.setProductId(productId);
					Date dd = DateUtil.convertCorrectDate(date,"yyyy年MM月dd日 hh時mm分","dd-MM-yyyy hh:mm:ss");
					ec.setDatetimePost(dd);
					String username = li.get(i).select("dt.elUser").select("a").text();
					ec.setVoiceName(username);
					String content = li.get(i).select("p.elText").text();
					ec.setContent(content);
					String star = li.get(i).getElementsByClass("elPoint").text();
					ec.setRating(Integer.parseInt(star.replaceAll(".0", "")));
					eccomList.add(ec);
				}
			}
			return eccomList;
		}
}
