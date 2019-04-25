package com.isentia.ec.comments;

import java.io.IOException;
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

public class ElevenSTCommentsCrawler implements CommentsCrawler{
	final static Logger logger = Logger.getLogger(ElevenSTCommentsCrawler.class);
	public static void main (String [] args){
		ElevenSTCommentsCrawler lcc = new ElevenSTCommentsCrawler();
		ECDAO ecdao = null; 
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
			long channelId = 8L;
			ArrayList <ECSKU> allSKU = ecdao.getAllSKU(args[0], channelId);
			ArrayList<ECComments> allComments = new ArrayList<ECComments>();
			for(ECSKU sku: allSKU){
				allComments = lcc.getAllCommentsToCrawl(sku.getProductId());
				System.out.println("Total Crawled Comments" + allComments.size());
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
			try{
				for(int pageNum=1;pageNum<2;pageNum++){
					String url = "http://www.11st.co.kr/product/SellerProductDetail.tmall?method=getProductReviewList&prdNo="+productId+"&page="+pageNum+"&pageTypCd=first&reviewDispYn=Y&isPreview=false&reviewOptDispYn=Y&optSearchBtnAndGraphLayer=Y&reviewBottomBtn=Y&openDetailContents=Y&pageSize=10&isIgnoreAuth=false&lctgrNo=1001344";
					Document doc = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A").referrer("http://www.yahoo.com").timeout(0).get();
					Elements ul = doc.select("div.review_list > ul"); 
					Elements li = ul.select("li");
					for (int i = 0; i < li.size(); i++) {
						ECComments ecCom = new ECComments();
						ecCom.setProductId(productId);
						String rating = li.get(i).getElementsByClass("selr_wrap").select("span").text();
						logger.debug(rating);
						rating = rating.substring(rating.indexOf("중"), rating.length());
						logger.debug(rating);
						rating = rating.replaceAll("개", "");
						rating = rating.replaceAll("중", "");
						ecCom.setRating(Integer.parseInt(rating.trim()));
						ecCom.setVoiceName(li.get(i).getElementsByClass("top_r").select("strong").text());
						ecCom.setUrl(li.get(i).getElementsByClass("option_txt").text());
						Date  dd=DateUtil.convertCorrectDate(li.get(i).getElementsByClass("top_r").select("span").text(), "yyyy-MM-dd",  "yyyy-MM-dd");
						ecCom.setDatetimePost(dd);
						ecCom.setContent(li.get(i).getElementsByClass("summ_conts").select("a").text());
						allECComments.add(ecCom);
					}
				}
			}catch(Exception e){
				logger.error(e.getMessage());
			}
			return allECComments;
		}
}
