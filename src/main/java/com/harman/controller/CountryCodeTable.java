package com.harman.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import org.apache.poi.ss.usermodel.*;

import com.harman.Model.MariaModel;

public class CountryCodeTable implements Runnable{
	private static CountryCodeTable country_code = null;

	public static CountryCodeTable getInstance(){
		if (country_code == null)
			country_code = new CountryCodeTable();
		return country_code;
	}

	public long getNumericValue(String ipAddress)
	{
		long result = 0;
		String ipArray[] = ipAddress.split("\\.");
		result = (Long.parseLong(ipArray[0]))*16777216 + 
				(Long.parseLong(ipArray[1]))*65536 + 
				(Long.parseLong(ipArray[2]))*256 + (Long.parseLong(ipArray[3])*1);
		return result;
	}
	
	
	@Override
	public void run() {
		tableCreate();
		
	}
	
	public void tableCreate()
	{
			long startRange = 0;
			long endRange = 0; 
			String countryCode = null;
			Connection con = null;
			File file = new File("/country.xls");
			System.out.println("Reading from country code file");
			MariaModel mariaModel = MariaModel.getInstance();
            con = mariaModel.openConnection("COUNTRY_CODE");
			try {
				InputStream myFile = new FileInputStream(file);
				POIFSFileSystem myFileSystem = new POIFSFileSystem(myFile);
				HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);
				HSSFSheet mySheet = myWorkBook.getSheetAt(0);
				Iterator rowIter = mySheet.rowIterator();
				//rowIter.next();
				int columIndx = -1;
				System.out.println("Iterate file \n");
				while (rowIter.hasNext()) {
					columIndx = 0;
	                HSSFRow myRow = (HSSFRow) rowIter.next();
	                Iterator<?> cellIter = myRow.cellIterator();
	                List<HSSFCell> cellRowList = new ArrayList<HSSFCell>();
	                //System.out.println("Row: "+ myRow.toString());
	                HSSFCell myCell = null;
	                while (cellIter.hasNext()) {
	                	columIndx ++;
	                     myCell = (HSSFCell) cellIter.next();
	                    if( columIndx == 1)
	                    	startRange = getNumericValue(myCell.toString());
	                    if (columIndx == 2)
	                    	endRange = getNumericValue(myCell.toString());
	                    if (columIndx ==3)
	                    	countryCode = myCell.toString();
	                    cellRowList.add(myCell);	                                       
	                }
	                System.out.println("Row: "+ myCell +"\n" );
	                String query = 
	                		"INSERT INTO countrycodetable (startRange, endRange, countryCode) VALUES"
	                		+ "(" + startRange + "," + endRange + ",'" + countryCode + "')";
	                
	                mariaModel.insertCountryCode(query, con);
	             	}	
				con.close();
			} catch(IOException e) {
				System.out.println("Error in closing sql connection " +e);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Error in workbook handeling: " +e);
			}
			System.out.println("Success import excel to mysql table");
		}
	}
	


