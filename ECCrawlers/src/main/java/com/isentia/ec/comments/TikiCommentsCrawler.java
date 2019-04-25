package com.isentia.ec.comments;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.isentia.dao.ECDAO;
import com.isentia.entity.ECComments;
import com.isentia.entity.ECSKU;
import com.isentia.util.DateUtil;
import com.isentia.util.ECUtil;

public class TikiCommentsCrawler {
	final static Logger logger = Logger.getLogger(TikiCommentsCrawler.class);
	public static void main (String [] args){
		TikiCommentsCrawler lcc = new TikiCommentsCrawler();
		ECDAO ecdao = null; 
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
			long channelId = 13L;
			ArrayList <ECSKU> allSKU = ecdao.getAllSKU("huggies", channelId);
			ArrayList<ECComments> allComments = new ArrayList<ECComments>();
			for(ECSKU sku: allSKU){
				allComments = lcc.getAllCommentsToCrawl(sku.getProductId(),sku.getContent());
				System.out.println("Total Crawled Comments" + allComments.size());
				if(allComments.size()>0){
					for(ECComments eccom: allComments){
						eccom.setChannelId(channelId);
						try{
							ecdao.insertIntoComments(eccom);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
		public ArrayList<ECComments> getAllCommentsToCrawl(String productId,String content) throws IOException{
			ArrayList <ECComments> allECComments= new ArrayList<ECComments>(); 
			int pageCount = 1;
			int totalPageToCrawl =1;
			try{
				do {
					String url = "https://tiki.vn/ajax/reviews?product_id="+productId+"&limit=5&page="+pageCount+"&sort=thank_count|desc,customer|all,stars|all&_=1495989109889";
					System.out.println(url);
					JSONObject json = ECUtil.readJsonFromUrl(url,"UTF-8");
					logger.debug(json);
					totalPageToCrawl = json.getJSONObject("paging").getInt("total")/5+1;
					logger.debug("totalPageToCrawl :" +totalPageToCrawl);
					JSONArray ja = json.getJSONArray("data");
					logger.debug(ja);
					for (int i = 0; i < ja.size(); i++) {
						ECComments ecCom = new ECComments();
						ecCom.setProductId(productId);
						JSONObject obj = (JSONObject) ja.get(i);
						ecCom.setContent(obj.getString("content"));
						String date = obj.getString("created_at").trim();
						Date convertedDate = new Date(Long.parseLong(date+"000"));
						ecCom.setDatetimePost(convertedDate);
						ecCom.setRating(obj.getInt("rating"));
						ecCom.setVoiceName(obj.getJSONObject("created_by").getString("name").trim());
						allECComments.add(ecCom);
					}
					pageCount++;
				}while(pageCount!=totalPageToCrawl+1);
			}catch(Exception e){
				logger.error(e.getMessage());
			}
			return allECComments;
		}
}
