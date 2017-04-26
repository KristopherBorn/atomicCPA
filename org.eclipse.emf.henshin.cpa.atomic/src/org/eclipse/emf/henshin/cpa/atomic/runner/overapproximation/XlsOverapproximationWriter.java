package org.eclipse.emf.henshin.cpa.atomic.runner.overapproximation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.eclipse.emf.henshin.cpa.atomic.compareLogger.Logger2;
import org.eclipse.emf.henshin.model.Rule;

public class XlsOverapproximationWriter {


//	public XlsOverapproximationWriter() {
//	}

	private String essDelUseConflSheetName = "essDelUseConfl";
	private String MODIFIEDessDelUseConflSheetName = "MODIFIEDessDelUseConfl";
	private String lastRowOfFormula;
	private String lastColumnLetter;

	//TODO: ggf. Rückgabewert anpassen. z.B. erzeugtes File Objekt.
	public void export(Logger2 normalLogger, Logger2 modifiedLogger, File resultFile) {
		

		// Project project = Workspace.

		Workbook wb = new HSSFWorkbook();
		
		Sheet normalSheet = writeDataInSheet(wb, normalLogger, essDelUseConflSheetName);
		Sheet modifiedSheet = writeDataInSheet(wb, modifiedLogger, MODIFIEDessDelUseConflSheetName);
		
		//TODO!
		Sheet overapproximationSheet = writeOverapproximationInSheet(wb, normalLogger.getFirstRules(), normalLogger.getSecondRules() , "overapproximation");		

		
		FileOutputStream out;
		try {
			out = new FileOutputStream(resultFile);
			wb.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			wb.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Sheet writeDataInSheet(Workbook wb, Logger2 logger, String sheetName) {
		Sheet createdSheet = wb.createSheet(sheetName);
		
//		DONE: Daten abrufen vom Logger
		List<Rule> secondRules = logger.getSecondRules();
		List<Rule> firstRules = logger.getFirstRules();
		
		String[][] analysisResults = logger.getAnalysisResults();
		
		//first column - rule Names
		Row secondRuleNameRow = createdSheet.createRow(0);
		Cell firstCell = secondRuleNameRow.createCell(0);
		firstCell.setCellValue("\\/firstRule / secondRule->");
		for(int i = 0; i < secondRules.size(); i++){
			Cell cell = secondRuleNameRow.createCell(i+1);
			cell.setCellValue(firstRules.get(i).getName());
		}
		
		for (int i = 0; i < (firstRules.size()); i++) {
			Row row = createdSheet.createRow(i+1);
			String[] arrayRow = analysisResults[i];
			Cell firstCellInRow = row.createCell(0);
			firstCellInRow.setCellValue(firstRules.get(i).getName());
			for (int j = 0; j < secondRules.size(); j++) {
				Cell cell = row.createCell(j+1);
				String value = arrayRow[j];
				cell.setCellValue(Integer.valueOf(value));
			}
		}
		
		// DONE: add Metrics here!:
		// SUM, TIMEOUTS, MAXIMUM, AVERAGE, MEDIAN, OVERALL RUNTIME
		
		lastRowOfFormula = String.valueOf(firstRules.size()+1);
		int lastColumnNumber = secondRules.size();
		lastColumnLetter = CellReference.convertNumToColString(lastColumnNumber);
		
		Row sumRow = createdSheet.createRow(firstRules.size()+1);
		sumRow.createCell(0).setCellValue("SUM:");
		String sumFormula = "SUM(B2:"+lastColumnLetter+lastRowOfFormula+")";
		Cell formulaCell = sumRow.createCell(1);
		formulaCell.setCellFormula(sumFormula);
		
		Row timeoutRow = createdSheet.createRow(firstRules.size()+2);
		timeoutRow.createCell(0).setCellValue("TIMEOUTS:");
		Cell timeoutCell = timeoutRow.createCell(1);
		String timeoutFormula = "COUNTIF(B2:"+lastColumnLetter+lastRowOfFormula+",C"+String.valueOf(firstRules.size()+3)+")";
		timeoutCell.setCellFormula(timeoutFormula);
		
		Cell toCell = timeoutRow.createCell(2);
		toCell.setCellValue("TO");

		Row maxRow = createdSheet.createRow(firstRules.size()+3);
		maxRow.createCell(0).setCellValue("MAXIMUM:");
		String maxFormula = "MAX(B2:"+lastColumnLetter+lastRowOfFormula+")";
		maxRow.createCell(1).setCellFormula(maxFormula);

		Row avgRow = createdSheet.createRow(firstRules.size()+4);
		avgRow.createCell(0).setCellValue("AVERAGE:");
		String avgFormula = "AVERAGE(B2:"+lastColumnLetter+lastRowOfFormula+")";
		avgRow.createCell(1).setCellFormula(avgFormula);
		
		Row totalRuntimeRow = createdSheet.createRow(firstRules.size()+5);
		totalRuntimeRow.createCell(0).setCellValue("TOTAL_RUNTIME:");
		totalRuntimeRow.createCell(1).setCellValue(String.valueOf(logger.getTotalRuntimeAmount())+" ms");
		if((logger.getTotalRuntimeAmount()/1000) > 1)
			totalRuntimeRow.createCell(1).setCellValue(String.valueOf(logger.getTotalRuntimeAmount()/1000)+" s");
		if(logger.getTotalRuntimeAmount()/(1000*60) > 1)
			totalRuntimeRow.createCell(1).setCellValue(String.valueOf(logger.getTotalRuntimeAmount()/(1000*60))+" min");
		
		
		
		return createdSheet;
	}

	//TODO: in wie weit macht es wirklich Sinn hier zwischen firstRules und secondRules zu unterscheiden???  
	private Sheet writeOverapproximationInSheet(Workbook wb, List<Rule> firstRules, List<Rule> secondRules,
			String sheetName) {
		Sheet createdSheet = wb.createSheet(sheetName);
		
		//first column - rule Names
		Row secondRuleNameRow = createdSheet.createRow(0);
		Cell firstCell = secondRuleNameRow.createCell(0);
		firstCell.setCellValue("\\/firstRule / secondRule->");
		for(int i = 0; i < secondRules.size(); i++){
			Cell cell = secondRuleNameRow.createCell(i+1);
			cell.setCellValue(secondRules.get(i).getName());
		}
		
		for (int i = 0; i < (firstRules.size()); i++) {
			Row row = createdSheet.createRow(i+1);
			Cell firstCellInRow = row.createCell(0);
			firstCellInRow.setCellValue(firstRules.get(i).getName());
			for (int j = 0; j < secondRules.size(); j++) {
				Cell cell = row.createCell(j+1);
				
				CellAddress address = cell.getAddress();
				String addressAsString = address.formatAsString();
//				String currentRowOfFormula = String.valueOf(i+1);
//				int lastColumnNumber = secondRules.size();
//				String currentColumnLetter = CellReference.convertNumToColString(j+1);
//				String currentAddress = currentColumnLetter+currentRowOfFormula;
				String formula = MODIFIEDessDelUseConflSheetName+"!"+addressAsString+" - "+essDelUseConflSheetName+"!"+addressAsString;
				cell.setCellFormula(formula);
			}
		}
		
		
		// DONE: add Metrics here!:
		Row overapproximationResultRow = createdSheet.createRow(firstRules.size()+1);
		Cell overapproximationDescriptionCell = overapproximationResultRow.createCell(0);
		overapproximationDescriptionCell.setCellValue("overAppr [%]");
		Cell overapproximationResultCell = overapproximationResultRow.createCell(1);
		String currentRowOfFormula = String.valueOf(firstRules.size()+2);
		// Berechnung der Überapproximation
		String overapproximationResultFormula = "(("+MODIFIEDessDelUseConflSheetName+"!B"+currentRowOfFormula+" / "+essDelUseConflSheetName+"!B"+currentRowOfFormula+")-1)*100";
		overapproximationResultCell.setCellFormula(overapproximationResultFormula);
		
		return createdSheet;
	}

}
