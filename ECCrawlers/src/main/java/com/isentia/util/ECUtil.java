package com.isentia.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import com.brandtology.analyzer.StandardAnalyzer;

public class ECUtil {

	public static void main(String [] args) throws Exception {
		System.out.println(checkQueryExist("你们","你们是猪"));
	}
	
	
	public static boolean checkIsValidProduct(String keyword, String content) throws IOException,ParseException{
		String query = getQueryString(keyword);
		Directory index = new RAMDirectory();
		IndexWriter writer = new IndexWriter(index, new StandardAnalyzer(new String[0]), true, new MaxFieldLength(1000000));
		Document doc = new Document();
		doc.add(new Field("post_content", content, Field.Store.YES,
				Field.Index.ANALYZED));
		try {
			writer.addDocument(doc);
		} catch (IOException e) {
			System.out.println("IOException adding Lucene Document: "
					+ e.getMessage());
		}
		try {
			writer.optimize();
			writer.close();
		} catch (IOException e) {
			System.out.println("IOException closing Lucene IndexWriter: "
					+ e.getMessage());
		}

		String TIME_FORMAT = "yyyy-MM-dd";
		SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);

		Calendar calTo = Calendar.getInstance();
		calTo.add(Calendar.DATE, -3);

		IndexSearcher indexSearcher = new IndexSearcher(index);

		String queryString = query;

		String[] fields = { "post_title", "post_content" };
		BooleanQuery.setMaxClauseCount(1000000);
		QueryParser qp = new QueryParser(fields[1], new StandardAnalyzer(
				new String[0]));
		Query q = qp.parse(queryString);
		q = q.rewrite(indexSearcher.getIndexReader());
		Hits hits = indexSearcher.search(q);
		int hitCount = hits.length();
		if(hitCount >= 1){
			return true;
		}else{
			return false;
		}
	}
	
	
	public static boolean checkQueryExist(String queryString, String content) throws IOException,ParseException{
		Directory index = new RAMDirectory();
		IndexWriter writer = new IndexWriter(index, new StandardAnalyzer(new String[0]), true, new MaxFieldLength(1000000));
		Document doc = new Document();
		doc.add(new Field("post_content", content, Field.Store.YES,
				Field.Index.ANALYZED));
		try {
			writer.addDocument(doc);
		} catch (IOException e) {
			System.out.println("IOException adding Lucene Document: "
					+ e.getMessage());
		}
		try {
			writer.optimize();
			writer.close();
		} catch (IOException e) {
			System.out.println("IOException closing Lucene IndexWriter: "
					+ e.getMessage());
		}

		String TIME_FORMAT = "yyyy-MM-dd";
		SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);

		Calendar calTo = Calendar.getInstance();
		calTo.add(Calendar.DATE, -3);

		IndexSearcher indexSearcher = new IndexSearcher(index);


		String[] fields = { "post_title", "post_content" };
		BooleanQuery.setMaxClauseCount(1000000);
		QueryParser qp = new QueryParser(fields[1], new StandardAnalyzer(
				new String[0]));
		Query q = qp.parse(queryString);
		q = q.rewrite(indexSearcher.getIndexReader());
		Hits hits = indexSearcher.search(q);
		int hitCount = hits.length();
		if(hitCount >= 1){
			return true;
		}else{
			return false;
		}
		
	}
	
	
	private static String getQueryString (String keyword){
	
		if(keyword.equals("クイックルワイパー")){
			return "クイックルワイパー NOT トイレク NOT カーペッ NOT 食卓 NOT キッチン";
		}else if(keyword.equals("クイックルワイパーハンディ")){
			return "クイックルワイパーハンディ NOT トイレク NOT カーペッ NOT 食卓 NOT キッチン";
		}
		
//		return "クイックルワイパー NOT トイレク NOT カーペッ NOT 食卓 NOT キッチン";
		return "";
	}
	
	public static String convertUnicode(String keyword){
		if(keyword.equals("クイックルワイパー")) {
			return "%83N%83C%83b%83N%83%8B%83%8F%83C%83p%81%5B";
		}
		
		if(keyword.equals("クイックルワイパーハンディ")) {
			return "%83N%83C%83b%83N%83%8B%83%8F%83C%83p%81%5B%83n%83%93%83f%83B";
		}
		return "";
	}
	
	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url,String charset) throws IOException,
			JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName(charset)));
			String jsonText = readAll(rd);
			JSONObject json = JSONObject.fromObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}
	
	public static String getChannelName(long channelId) {
			if (channelId == 34){
				return "Amazon";
			}
			if (channelId == 32){
				return "Kakaku";
			}
			if (channelId == 30){
				return "Rakuten";
			}
			if (channelId == 33){
				return "Yahoo Shopping";
			}
			if (channelId == 31){
				return "Lohaco";
			}
			return "";
	}
}
