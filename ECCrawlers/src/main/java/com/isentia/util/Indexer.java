package com.isentia.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.brandtology.analyzer.StandardAnalyzer;
import com.isentia.dao.ECDAO;
import com.isentia.entity.HexagonContent;

public class Indexer {

	
	 private static String TIME_FORMAT = "yyyy-MM-dd";
	 private static String WEIBO_TIME_FORMAT = "dd-MM-yy";
	 private static SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
	 private static SimpleDateFormat weiboSDF = new SimpleDateFormat(WEIBO_TIME_FORMAT);
	 
	public static void addDataToIndex(ArrayList<HexagonContent> hp,String path) {
		
		StandardAnalyzer analyzer = new StandardAnalyzer(new String[0]);
		
		IndexWriter writer = null;
		try {
			writer = new IndexWriter(path, analyzer, true);
		} catch (IOException e) {

			System.out.println("IOException opening Lucene IndexWriter: " + e.getMessage());

		}
		for(HexagonContent aa: hp) {
			Document doc = new Document();
			System.out.println("data- -> " +  aa.getContents());
			doc.add(new Field("post_content", aa.getContents(), Field.Store.YES, Field.Index.ANALYZED));
			doc.add(new Field("url", aa.getUrl(), Field.Store.YES, Field.Index.ANALYZED));
			doc.add(new Field("date", sdf.format(aa.getDate()), Field.Store.YES, Field.Index.ANALYZED));
			doc.add(new Field("site", aa.getType(), Field.Store.YES, Field.Index.ANALYZED));
//			doc.add(new Field("author", aa.getAuthor(), Field.Store.YES, Field.Index.ANALYZED));
			doc.add(new Field("repost", aa.getShareCount()+"", Field.Store.YES, Field.Index.ANALYZED));
			doc.add(new Field("comment", aa.getCommentCount()+"", Field.Store.YES, Field.Index.ANALYZED));
			doc.add(new Field("like", aa.getLikeCount()+"", Field.Store.YES, Field.Index.ANALYZED));
			try {
				writer.addDocument(doc);
	
			} catch (IOException e) {
	
				System.out.println("IOException adding Lucene Document: " + e.getMessage());
	
			}
		}
		try {
			writer.optimize();
			writer.close();
		} catch (IOException e) {

			System.out.println("IOException closing Lucene IndexWriter: " + e.getMessage());

		}
	}

	public static void readFile() throws Exception{
		FileInputStream excelFile = new FileInputStream(new File("/tocanan/data/red.xlsx"));
		Workbook workbook = new XSSFWorkbook(excelFile);
		Sheet datatypeSheet = workbook.getSheetAt(0);
		Iterator<Row> iterator = datatypeSheet.iterator();
		HashMap <Integer,String> insightMapping = new HashMap<Integer,String>();
		ArrayList<HexagonContent> hpList = new ArrayList<HexagonContent>();
		while (iterator.hasNext()) {
			Row currentRow = iterator.next();
			System.out.println(currentRow.getRowNum());
			Iterator<Cell> cellIterator = currentRow.iterator();
			HexagonContent hp = new HexagonContent();
			int aCol = 0;
			int col =1;
			while (cellIterator.hasNext()) {
				if(aCol!=0) {
					Cell currentCell = cellIterator.next();
					if(col==2) {
						if(!currentCell.getStringCellValue().equals("Date")) {
							hp.setDate(weiboSDF.parse(currentCell.getStringCellValue()));
						}else {
							hp.setDate(new Date());
						}
					}
					if(col==3) {
						hp.setUrl(currentCell.getStringCellValue());
					}
					if(col==4) {
						hp.setContents(currentCell.getStringCellValue());
					}
					if(col==5) {
						try {
							currentCell.getStringCellValue().equals("Repost");
							hp.setShareCount(0);
						}catch(Exception e) {
							hp.setShareCount((long)currentCell.getNumericCellValue());
						}
						
					}
					if(col==6) {
						try {
							currentCell.getStringCellValue().equals("Comment");
							hp.setCommentCount(0);
						}catch(Exception e) {
							hp.setCommentCount((long)currentCell.getNumericCellValue());
						}
						
					}
					if(col==7) {
						try {
							currentCell.getStringCellValue().equals("Likes");
							hp.setLikeCount(0);
						}catch(Exception e) {
							hp.setLikeCount((long)currentCell.getNumericCellValue());
						}
						
					}
					if(col==8) {
						hp.setType(currentCell.getStringCellValue());
					}
//					if(col==7) {
//						hp.setAuthor((currentCell.getStringCellValue()));
//					}
					col++;
				}
				aCol++;
			}
			hpList.add(hp);
		}
		addDataToIndex(hpList,"/tocanan/index");
	}

	
	public static void matchTicket(String query, String filterId) throws Exception {
		String queryString = query;
	 	System.out.println(queryString);
	 	String[] fields = {"post_title","post_content"}; 
	 	BooleanQuery.setMaxClauseCount(1000000);
	    QueryParser qp = new QueryParser(fields[1],new StandardAnalyzer(new String[0]));
	    Query q = qp.parse(queryString);
	    IndexSearcher indexSearcher = new IndexSearcher("/tocanan/index");
	    q = q.rewrite(indexSearcher.getIndexReader());
	    Hits hits = indexSearcher.search(q);
		int hitCount = hits.length(); 
		ArrayList<HexagonContent> hexagonPostList = new ArrayList<HexagonContent>();
		System.out.println ("hitcout is:" + hitCount);
		Document doc = new Document();
		for(int i=0; i < hitCount; i++){
		  	doc = hits.doc(i);
		  	HexagonContent hc = new HexagonContent();
//		  	hc.setAuthor(doc.get("author"));
			hc.setContents(doc.get("post_content"));
			hc.setUrl(doc.get("url"));
			hc.setType(doc.get("site"));
//			hc.setViewCount(Long.parseLong(doc.get("view")));
			hc.setShareCount(Long.parseLong(doc.get("repost")));
			hc.setCommentCount(Long.parseLong(doc.get("comment")));
			hc.setLikeCount(Long.parseLong(doc.get("like")));
			hc.setDate(sdf.parse(doc.get("date")));
			hc.setClientAccountId("1");
			hc.setFilter(filterId);
			hexagonPostList.add(hc);
		}
		ECDAO edao = new ECDAO();
		edao.createMasterTicketConnection();
		edao.insertCrimsonDataSocial(hexagonPostList);
	}

	public static void main(String[] args) throws Exception {
		readFile();
		

		matchTicket("((Revlon OR 露华浓) AND (眉笔 OR 眉粉 OR 眉饼 OR 眼线笔 OR 眼线液 OR 眼线膏 OR 睫毛膏 OR 眼影 OR 眉彩膏)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","1");		
		
		matchTicket("((Revlon OR 露华浓) AND (腮红 OR 胭脂 OR 粉饼 OR 粉底液 OR 妆前乳 OR 粉底霜 OR 粉底膏 OR 遮瑕笔 OR 遮瑕膏 OR 散粉 OR 蜜粉 OR 隔离霜 OR 气垫BB OR BB霜 OR 气垫CC OR CC霜 OR 卸妆水 OR 气垫霜 OR 云雾粉)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","2");
		matchTicket("((Revlon OR 露华浓) AND (唇彩 OR 唇蜜 OR 唇膏 OR 口红 OR 唇釉 OR 唇笔 OR 唇线笔)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","3");
		matchTicket("((Revlon OR 露华浓) AND (彩妆套装)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","4");
		matchTicket("((美宝莲 OR Maybelline) AND (眉笔 OR 眉粉 OR 眉饼 OR 眼线笔 OR 眼线液 OR 眼线膏 OR 睫毛膏 OR 眼影 OR 眉彩膏)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","5");
		matchTicket("((美宝莲 OR Maybelline) AND (腮红 OR 胭脂 OR 粉饼 OR 粉底液 OR 妆前乳 OR 粉底霜 OR 粉底膏 OR 遮瑕笔 OR 遮瑕膏 OR 散粉 OR 蜜粉 OR 隔离霜 OR 气垫BB OR BB霜 OR 气垫CC OR CC霜 OR 卸妆水 OR 气垫霜 OR 云雾粉)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","6");
		matchTicket("((美宝莲 OR Maybelline) AND (唇彩 OR 唇蜜 OR 唇膏 OR 口红 OR 唇釉 OR 唇笔 OR 唇线笔)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","7");
		matchTicket("((美宝莲 OR Maybelline) AND (彩妆套装)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","8");
		matchTicket("((魅可 OR Mac) AND (眉笔 OR 眉粉 OR 眉饼 OR 眼线笔 OR 眼线液 OR 眼线膏 OR 睫毛膏 OR 眼影 OR 眉彩膏)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","9");
		matchTicket("((魅可 OR Mac) AND (腮红 OR 胭脂 OR 粉饼 OR 粉底液 OR 妆前乳 OR 粉底霜 OR 粉底膏 OR 遮瑕笔 OR 遮瑕膏 OR 散粉 OR 蜜粉 OR 隔离霜 OR 气垫BB OR BB霜 OR 气垫CC OR CC霜 OR 卸妆水 OR 气垫霜 OR 云雾粉)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","10");
		matchTicket("((魅可 OR Mac) AND (唇彩 OR 唇蜜 OR 唇膏 OR 口红 OR 唇釉 OR 唇笔 OR 唇线笔)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","11");
		matchTicket("((魅可 OR Mac) AND (彩妆套装)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","12");
		matchTicket("((玛丽黛佳 OR Mariedalgar) AND (眉笔 OR 眉粉 OR 眉饼 OR 眼线笔 OR 眼线液 OR 眼线膏 OR 睫毛膏 OR 眼影 OR 眉彩膏)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","13");
		matchTicket("((玛丽黛佳 OR Mariedalgar) AND (腮红 OR 胭脂 OR 粉饼 OR 粉底液 OR 妆前乳 OR 粉底霜 OR 粉底膏 OR 遮瑕笔 OR 遮瑕膏 OR 散粉 OR 蜜粉 OR 隔离霜 OR 气垫BB OR BB霜 OR 气垫CC OR CC霜 OR 卸妆水 OR 气垫霜 OR 云雾粉)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","14");
		matchTicket("((玛丽黛佳 OR Mariedalgar) AND (唇彩 OR 唇蜜 OR 唇膏 OR 口红 OR 唇釉 OR 唇笔 OR 唇线笔)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","15");
		matchTicket("((玛丽黛佳 OR Mariedalgar) AND (彩妆套装)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","16");
		matchTicket("((完美日记 OR \"Perfect Diary\") AND (眉笔 OR 眉粉 OR 眉饼 OR 眼线笔 OR 眼线液 OR 眼线膏 OR 睫毛膏 OR 眼影 OR 眉彩膏)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","17");
		matchTicket("((完美日记 OR \"Perfect Diary\") AND (腮红 OR 胭脂 OR 粉饼 OR 粉底液 OR 妆前乳 OR 粉底霜 OR 粉底膏 OR 遮瑕笔 OR 遮瑕膏 OR 散粉 OR 蜜粉 OR 隔离霜 OR 气垫BB OR BB霜 OR 气垫CC OR CC霜 OR 卸妆水 OR 气垫霜 OR 云雾粉)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","18");
		matchTicket("((完美日记 OR \"Perfect Diary\") AND (唇彩 OR 唇蜜 OR 唇膏 OR 口红 OR 唇釉 OR 唇笔 OR 唇线笔)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","19");
		matchTicket("((完美日记 OR \"Perfect Diary\") AND (彩妆套装)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","20");
		matchTicket("((Revlon OR 露华浓)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","21");
		matchTicket("((美宝莲 OR Maybelline)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","22");
		matchTicket("((魅可 OR Mac)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","23");
		matchTicket("((玛丽黛佳 OR Mariedalgar)) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","24");
		matchTicket("((完美日记 OR \"Perfect Diary\")) NOT (\"0元\" OR \"1.2折\" OR \"1.3折\" OR \"1.5折\" OR \"1折\" OR \"36团\" OR \"3ce\" OR \"99元\" OR \"ga ga\" OR \"ga妞\" OR \"ga姐\" OR \"G团\" OR \"http://t.cn\" OR \"http://url.cn\" OR \"LIKE团\" OR \"MAC book\" OR \"MAC mini\" OR \"MAC 本\" OR \"MAC本\" OR \"T-mac\" OR \"VC团\" OR \"V亻言\" OR \"V信\" OR \"V店\" OR \"V问\" OR \"we1x1n\" OR \"we1xin\" OR \"wei-xin\" OR \"亻 言\" OR \"优品360\" OR \"分享自 @Qzone\" OR \"卡丝BB\" OR \"咨询V信\" OR \"幻彩EE\" OR \"微 亻言\" OR \"微 信\" OR \"微/信\" OR \"微\\信\" OR \"经常对着电脑的mm不妨入手一支\" OR \"葳 信\" OR 1口價 OR 1手貨源 OR iOS AND 苹果 OR ipad OR iphone OR MAC AND 刷机 OR mac AND 大力水手 OR MAC AND 系统 OR MACbook OR RMB OR sasa OR taobao OR URL OR wangwang OR weixin OR Yasser OR 一口价 OR 一折 OR 一淘玩客 OR 一起买好 OR 万客团 OR 上衣 OR 专售 OR 专柜 OR 买一送一 OR 亻言 OR 仅售 OR 代售 OR 代理 OR 代购 OR 优惠券 OR 低至一折 OR 促销 OR 假1罰 OR 假一罚 OR 假一赔 OR 出货 OR 分享朋友圈 OR 到货 OR 加QQ OR 加威 OR 动心团 OR 包包 OR 包邮 OR 原价 OR 原单 OR 嘀嗒团 OR 四宫格 OR 团购 OR 团靓 OR 威信 OR 宝贝上架 OR 宝贝新上架 OR 实体店 OR 實惠價 OR 實拍 OR 專營 OR 尾货 OR 微亻言 OR 微代 OR 微信 OR 微商 OR 微問 OR 微盤 OR 微號 OR 微購 OR 微问 OR 打折 OR 打渔网 OR 批发 OR 批發 OR 抢购 OR 掌柜自留 OR 接单 OR 染妆网 OR 标题下方蓝字 OR 正品 OR 此微博已被作者刪除 OR 白菜价 OR 美团 OR 美国省钱购物网 OR 葳信 OR 薄利多銷 OR 薇信 OR 詢價 OR 諮-詢 OR 订单 OR 购买网址 OR 购买链接 OR 赶团网 OR 超低价 OR 轉售 OR 转让 OR 闲置 OR 预售 OR 预定 OR 预订 OR 高价回收 OR 高仿 OR 网页链接 OR 券后 OR 优惠券 OR 卷发棒 OR 吹风机 OR 月想衣裳花想容 OR 云想衣裳花想容 OR 直邮 OR 领券 OR 日行一善 OR 吴磊家的叶)","25");
		
	}
}
