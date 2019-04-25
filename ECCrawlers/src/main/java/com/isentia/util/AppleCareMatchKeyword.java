package com.isentia.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.isentia.entity.AppleLayer;

public class AppleCareMatchKeyword {

	public static String absolutePath = "/Projects/Apple";
	
	public String formLayer1Keyword(String insightsFileName,String negativeFileName) throws Exception {
			FileInputStream fstream = new FileInputStream(absolutePath+"/keywords/"+insightsFileName+".txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream, "UTF8"));
			String strLine;
			String negativeLine;
			StringBuffer sb = new StringBuffer();
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				FileInputStream negativeStream = new FileInputStream(absolutePath+"/keywords/"+negativeFileName+".txt");
				BufferedReader br2 = new BufferedReader(new InputStreamReader(negativeStream, "UTF8"));
				while ((negativeLine = br2.readLine()) != null)   {
					System.out.println("(" + strLine + " AND " + negativeLine + ") OR ");
					sb.append("(" + strLine + " AND " + negativeLine + ") OR ");
				}
			 
			}
			String queryString = sb.toString().substring(0,sb.toString().length()-3);
		return queryString;
	}
	
	private HashMap<String,String> formLayer2Keyword(String directoryName) throws Exception{
		String rootDirectory = absolutePath+"/keywords/";
		HashMap<String,String> layer2Keyword = new HashMap<String,String>(); 
		File folder = new File(rootDirectory + directoryName);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			StringBuffer sb = new StringBuffer();
		    if (file.isFile()) {
//		    	System.out.println("Processing -->" +rootDirectory + directoryName+"/"+file.getName());
//		    	BufferedReader br = new BufferedReader(new FileReader(rootDirectory + directoryName+"/"+file.getName()));
		    	FileInputStream fstream = new FileInputStream(rootDirectory + directoryName+"/"+file.getName());
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream, "UTF8"));
		    	
		    	String line = null;
		    	while ((line = br.readLine()) != null) {
		    		try {
		    			sb.append("("+line+") OR ");
		    		}catch(Exception e) {
		    			e.printStackTrace();
		    		}
		    	}
		    	System.out.println(file.getName().replace(".txt", ""));
		    	System.out.println(sb.toString().substring(0,sb.toString().length()-3));
		    	layer2Keyword.put(file.getName().replace(".txt", ""), sb.toString().substring(0,sb.toString().length()-3));
		    }
		}
//		System.out.println("Layer 2:" + layer2Keyword);
		return layer2Keyword;
	}
	
	
	public HashMap<String,AppleLayer> formKeywordMap(String topicFileName,String specialTopic) throws Exception{
		FileInputStream fstream = new FileInputStream(absolutePath+"/keywords/"+topicFileName+".txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream, "UTF8"));
		String strLine;
		HashMap<String,AppleLayer> allLayersKeyword = new HashMap<String,AppleLayer>();
		while ((strLine = br.readLine()) != null)   {
			AppleLayer layerData = new AppleLayer();
			layerData.setKeyword(formLayer1Keyword(strLine, "negativeKeyword"));
			layerData.setLayer2KeywordMap(formLayer2Keyword(strLine));
			allLayersKeyword.put(strLine, layerData);
		}
		
		FileInputStream fstream2 = new FileInputStream(absolutePath+"/keywords/"+specialTopic+".txt");
		BufferedReader br2 = new BufferedReader(new InputStreamReader(fstream2, "UTF8"));
		String strLine2;
		while ((strLine2 = br2.readLine()) != null)   {
			AppleLayer layerData = new AppleLayer();
			layerData.setKeyword(formLayer1Keyword(strLine2, "specialNegativeKeyword"));
			allLayersKeyword.put(strLine2, layerData);
		}
		return allLayersKeyword;
	}
	

	public void processData() throws Exception{
		HashMap<String,AppleLayer> dataLayer = formKeywordMap("topics","specialTopic");
		try {
            FileInputStream excelFile = new FileInputStream(new File(absolutePath+"/data.xlsx"));
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();
            HashMap <Integer,String> insightMapping = new HashMap<Integer,String>();
            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                System.out.println(currentRow.getRowNum());
                Iterator<Cell> cellIterator = currentRow.iterator();
                int col = 1;
                while (cellIterator.hasNext()) {
                	String layerByLayerText = "";
                    Cell currentCell = cellIterator.next();
                    if(col==3 && currentRow.getRowNum()!=0) {
                    	 Iterator it = dataLayer.entrySet().iterator();
                    	    while (it.hasNext()) {
                    	        Map.Entry pair = (Map.Entry)it.next();
                    	        AppleLayer al = (AppleLayer)pair.getValue();
                    	        String firstLayer = (String)pair.getKey();
//                    	        System.out.println(al.getKeyword());
                    	        boolean isValid = ECUtil.checkQueryExist(al.getKeyword(),currentCell.getStringCellValue());
                            	if(isValid) {
                            		boolean hasLevel2 = false;
                            		System.out.println("Hit First Layer： " + firstLayer + ": " + al.getKeyword());
                            		layerByLayerText += firstLayer;
                            		HashMap<String,String> hm = al.getLayer2KeywordMap();
                            		if(hm!=null) {
	                            		Iterator hmIterator = hm.entrySet().iterator();
	                             	    while (hmIterator.hasNext()) {
	                             	    	hasLevel2 = true;
	                             	    	Map.Entry layer2pair = (Map.Entry)hmIterator.next();
	                             	    	String keywordLayer2 = (String)layer2pair.getValue();
	                             	        String secondLayer = (String)layer2pair.getKey();
	                             	       System.out.println("Running 2nd Layer： " + secondLayer);
	                             	        boolean isLevel2 = ECUtil.checkQueryExist(keywordLayer2,currentCell.getStringCellValue());
	                             	        if(isLevel2) {
	                             	        	System.out.println("Hit second Layer： " + secondLayer);
	                             	        	layerByLayerText += ("- "+secondLayer);
	                             	        }else {
	                             	        	layerByLayerText += "-complaint-投诉";
	                             	        }
	                             	    }
                            		}
                             	   if(!hasLevel2) {
                             		  layerByLayerText += "- -弯曲";
                             	   }
                            	}
                            	if(layerByLayerText.length()>0) {
                            		insightMapping.put(currentRow.getRowNum(),layerByLayerText);
                            	}
                    	    }
                    	
                    }
                    col++;
                }
            }
            System.out.println("Insights Map" + insightMapping);
            Iterator it = insightMapping.entrySet().iterator();
            while (it.hasNext()) {
            	Map.Entry pair = (Map.Entry)it.next();
     	        Integer rowVal = (Integer)pair.getKey();
     	        String insightsVal = (String)pair.getValue();
	            Row r = datatypeSheet.getRow(rowVal); // 10-1
	              if (r == null) {
	                 // First cell in the row, create
	                 r = datatypeSheet.createRow(rowVal);
	              }
	          	Cell c = r.getCell(3); // 4-1
	          	if (c == null) {
	          	    // New cell
	          	    c = r.createCell(3, Cell.CELL_TYPE_STRING);
	          	}
	          	c.setCellValue(insightsVal.split("-")[0]);
	          	
	        	Cell c4 = r.getCell(4); // 4-1
	          	if (c4 == null) {
	          	    // New cell
	          		c4 = r.createCell(4, Cell.CELL_TYPE_STRING);
	          	}
	          	c4.setCellValue(insightsVal.split("-")[1]);
	          	
	          	
	        	Cell c5 = r.getCell(5); // 4-1
	          	if (c5 == null) {
	          	    // New cell
	          		c5 = r.createCell(5, Cell.CELL_TYPE_STRING);
	          	}
	          	c5.setCellValue(insightsVal.split("-")[2]);
            }

            FileOutputStream outFile =new FileOutputStream(new File(absolutePath+"/result.xlsx"));
            workbook.write(outFile);
            outFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	

	public static void main (String [] args) throws Exception{
		AppleCareMatchKeyword acm = new AppleCareMatchKeyword();
		acm.processData();
	}
}
