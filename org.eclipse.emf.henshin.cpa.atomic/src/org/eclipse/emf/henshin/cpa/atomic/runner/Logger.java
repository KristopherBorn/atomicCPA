package org.eclipse.emf.henshin.cpa.atomic.runner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.emf.henshin.model.Rule;

public class Logger {

	private static DateFormat dateFormat = new SimpleDateFormat("yy_MM_dd-HHmmss");
	
	// access: runtimeResults[row][column] - reihe - Spalte
	String[][] runtimeResults;
	String[][] numberOfDeleteUseconflicts;
	
	Map<Rule, Integer> rowPositionOfRule;
	Map<Rule, Integer> columnPositionOfRule;
	
	int numberOfRules;
	
	
	
	public void init(int numberOfRules){
		runtimeResults = new String[numberOfRules+1][numberOfRules+1];
		numberOfDeleteUseconflicts = new String[numberOfRules+1][numberOfRules+1];
		rowPositionOfRule = new HashMap<Rule, Integer>();
		columnPositionOfRule = new HashMap<Rule, Integer>();
		this.numberOfRules = numberOfRules;
	}
	
	public void setAnalysisKinds(boolean normal, boolean essential, boolean atomic){
		StringBuffer sb = new StringBuffer("");
		if(normal)
			sb.append("normal");
		if(sb.length()>1 && essential)
			sb.append("/");
		if(essential)
			sb.append("essential");
		if(sb.length()>1 && atomic)
			sb.append("/");
		if(atomic)
			sb.append("atomic");
		
		runtimeResults[0][0] = "RUNTIME-"+sb.toString();
		numberOfDeleteUseconflicts[0][0] = "D-U-CONFLICTS-"+sb.toString();			
	}
	
	public void addData(Rule rule1, Rule rule2, String runtime, String results){
		Integer rowPosition = rowPositionOfRule.get(rule1);
		if(rowPosition == null){
			rowPosition = rowPositionOfRule.size()+1;
			rowPositionOfRule.put(rule1, rowPosition);
			runtimeResults[rowPosition][0] = rule1.getName();
			numberOfDeleteUseconflicts[rowPosition][0] = rule1.getName();
		}
		Integer columnPosition = columnPositionOfRule.get(rule2);
		if(columnPosition == null){
			columnPosition = columnPositionOfRule.size()+1;
			columnPositionOfRule.put(rule2, columnPosition);
			runtimeResults[0][columnPosition] = rule1.getName();
			numberOfDeleteUseconflicts[0][columnPosition] = rule1.getName();
		}
		runtimeResults[rowPosition][columnPosition] = runtime;
		numberOfDeleteUseconflicts[rowPosition][columnPosition] = results;
	}
	
	
	public void exportStoredRuntimeToCSV(String targetFolder){
		try {
			
			Date start = new Date();
			String filename = targetFolder + File.separator + dateFormat.format(start) + "_runtime-results.csv";
			
			
			FileWriter fw = new FileWriter(filename);
			for(int row = 0; row<=numberOfRules; row++ ){
				for(int column = 0; column <=numberOfRules; column++ ){
					fw.append(runtimeResults[row][column]);
					fw.append(";");
				}
				fw.append("\n");
			}
			
			fw.flush();
			fw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void exportStoredConflictsToCSV(String targetFolder){
		try {
			
			Date start = new Date();
			String filename = targetFolder + File.separator + dateFormat.format(start) + "_conflicts-results.csv";
			
			
			FileWriter fw = new FileWriter(filename);
			for(int row = 0; row<=numberOfRules; row++ ){
				for(int column = 0; column <=numberOfRules; column++ ){
					fw.append(numberOfDeleteUseconflicts[row][column]);
					fw.append(";");
				}
				fw.append("\n");
			}
			
			fw.flush();
			fw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
