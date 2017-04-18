package org.eclipse.emf.henshin.cpa.atomic.compareLogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.henshin.model.Action;
import org.eclipse.emf.henshin.model.Edge;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;

public class Logger2 {
	
	public enum LogData {
		DELTE_USE_CONFLICTS,
		PRODUCE_USE_DEPENDENCY,
		ESSENTIAL_DELTE_USE_CONFLICTS,
		ESSENTIAL_PRODUCE_USE_DEPENDENCY,
		CONFLICT_CANDIDATE,
		DEPENDENCY_CANDIDATE,
		CONFLICT_ATOM,
		DEPENDENCY_ATOM,
		MINIMAL_CONFLICT_REASON,
		MINIMAL_DEPENDENCY_REASON,
		CONFLICT_REASON,
		DEPENDENCY_REASON
	}
	
	LogData logData;
	
	boolean dataAdded = false;
	
//	private static DateFormat dateFormat = new SimpleDateFormat("yy_MM_dd-HHmmss");
	
	/**
	 * @return the logData
	 */
	public LogData getLogData() {
		return logData;
	}

	// access: analysisDurations[row][column] - reihe - Spalte
	String[][] analysisDurations;
	String[][] analysisResults;
	
//	Map<Rule, Integer> rowPositionOfRule;
//	Map<Rule, Integer> columnPositionOfRule;
	
	int numberOfRules;
	
	List<Rule> rules;
	
	boolean addDetailsOnRuleName = false;  //TODO: sollte entfernt werden!
	
	//(14.04.2017) wird nicht verwendet. DEPRECATED?!
//	String totalRunTimes;
//	String totalResults;
	
	// das gehört nciht in die Logger Klasse!
//	CPAResult normalCpaResults;
//	CPAResult essentialCpaResults;
//	
//	List<MinimalConflict> minimalConflicts;
	
	public Logger2(LogData logData, List<Rule> rules) {
		this.logData = logData;
		this.rules = rules;
		this.numberOfRules = rules.size();
		analysisDurations = new String[numberOfRules][numberOfRules];
		analysisResults = new String[numberOfRules][numberOfRules]; 
	}

	// (14.04.2017) Scheint überflüssig = DEPRECATED zu sein!
//	public void init(List<Rule> rules){
//		
//		this.rules = rules;
//		
//		this.numberOfRules = rules.size();
//		
////		RuleSetMetricsCalculator ruleSetMetricsCalculator = new RuleSetMetricsCalculator(); 
//		
//		analysisDurations = new String[numberOfRules][numberOfRules];
//		
//		analysisResults = new String[numberOfRules][numberOfRules]; 
//		
////		rowPositionOfRule = new HashMap<Rule, Integer>(); //überflüssig. Ergibt sich aus der Position der Regel in der Regelliste.
////		columnPositionOfRule = new HashMap<Rule, Integer>();
//		
////		this.numberOfRules = numberOfRules+1;
//		
//		// das gehört nciht in die Logger Klasse!
////		normalCpaResults = new CPAResult();
////		essentialCpaResults = new CPAResult();
////		minimalConflicts = new LinkedList<MinimalConflict>();
//	}
	
	public void setAnalysisKinds(boolean normal, boolean essential, boolean atomic){
		String runtimeString = "";
		String deleteUseconflicts = "";
		StringBuffer sb = new StringBuffer("");
		if(normal)
			sb.append("normalCPs");
		if(sb.length()>1 && essential)
			sb.append("/");
		if(essential)
			sb.append("essentialCPs");
		if(sb.length()>1 && atomic)
			sb.append(" / ");
		if(atomic)
			sb.append("conflictAtoms");
			
		runtimeString = sb.toString();
		deleteUseconflicts = sb.toString();
		if(atomic)
			deleteUseconflicts = deleteUseconflicts.concat(" / Candidates / Minimal Reasons");
		
		
		analysisDurations[0][0] = "RUNTIME - "+runtimeString+"[ms]";
		analysisResults[0][0] = "D-U-CONFLICTS - "+deleteUseconflicts;			
	}
	
	public void addData(Rule rule1, Rule rule2, String runtime, String results){
		
		dataAdded = true;
		
		int rowPosition = rules.indexOf(rule1);
		int columnPosition = rules.indexOf(rule2);
		
//		Integer rowPosition = rowPositionOfRule.get(rule1);

		// das war alles für das initiale hinzufügen der analysierten Regeln.
		// das ist schrecklich und das macht man so nciht!
//		if(rowPosition == null){
//			rowPosition = rowPositionOfRule.size()+2;
//			rowPositionOfRule.put(rule1, rowPosition);
////			String ruleDetails = analyseDetailsOfRule(rule1);
////			String ruleDetails = (addDetailsOnRuleName == true) ? analyseDetailsOfRule(rule1) : "";
//			analysisDurations[rowPosition][0] = rule1.getName();// + ruleDetails;  
//			analysisResults[rowPosition][0] = rule1.getName();// + ruleDetails;
//			analysisDurations[rowPosition][1] = runtime;// das sind hier die Metriken zur jeweiligen Regel und dies wird für jede Regel nur einmal zu Beginn eingetragen!  
//			analysisResults[rowPosition][1] = results;// das sind hier die Metriken zur jeweiligen Regel und dies wird für jede Regel nur einmal zu Beginn eingetragen!
//		}
		

		// das war alles für das initiale hinzufügen der analysierten Regeln.
		// das ist schrecklich und das macht man so nicht!
//		Integer columnPosition = null;
//		if(rule2 == null){
//			columnPosition = -2;
//		}else{
//			if(rule2 != null)
//				columnPosition = columnPositionOfRule.get(rule2);
//			if(columnPosition == null){
//				columnPosition = columnPositionOfRule.size()+2;
//				columnPositionOfRule.put(rule2, columnPosition);
////			String ruleDetails = analyseDetailsOfRule(rule2);
//				analysisDurations[0][columnPosition] = rule2.getName();// + ruleDetails;
//				analysisResults[0][columnPosition] = rule2.getName();// + ruleDetails;
//				analysisDurations[1][columnPosition] = runtime;// das sind hier die Metriken zur jeweiligen Regel und dies wird für jede Regel nur einmal zu Beginn eingetragen!  
//				analysisResults[1][columnPosition] = results;// das sind hier die Metriken zur jeweiligen Regel und dies wird für jede Regel nur einmal zu Beginn eingetragen!
//			}
//		}
			
			analysisDurations[rowPosition][columnPosition] = runtime;
			analysisResults[rowPosition][columnPosition] = results;
	}
	
	

	/**
	 * @param addDetailsOnRuleName the addDetailsOnRuleName to set
	 */
	public void setAddDetailsOnRuleName(boolean addDetailsOnRuleName) {
		this.addDetailsOnRuleName = addDetailsOnRuleName;
	}

	/*
	 *  todo: add details here: #LhsNode, #LhsEdges, #deleteNodes, #deleteEdges
	 */
	private String analyseDetailsOfRule(Rule rule) {
		// TODO Auto-generated method stub
		
		int numberOfLhsNodes = rule.getLhs().getNodes().size();
		int numberOfLhsEdges = rule.getLhs().getEdges().size();
		Action deleteAction = new Action(Action.Type.DELETE);
		EList<Node> deletionNodes = rule.getActionNodes(deleteAction);
		int numberOfDeletionNodes = deletionNodes.size();
		EList<Edge> deletionEdges = rule.getActionEdges(deleteAction);
		int numberOfDeletionEdges = deletionEdges.size();
		
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append("#LhsNode:");
		sb.append(numberOfLhsNodes);		
		sb.append(" ,#LhsEdges:");
		sb.append(numberOfLhsEdges);	
		sb.append(" ,#deleteNodes:");
		sb.append(numberOfDeletionNodes);		
		sb.append(" ,#deleteEdges:");
		sb.append(numberOfDeletionEdges);	
		sb.append("]");
		
		return sb.toString();
	}

	//TODO: beide Export Methoden zusammenfassen!
	public void exportStoredRuntimeToCSV(String targetFolder){
		
		if(dataAdded){
			try {
//			Date start = new Date();
				String filename = targetFolder + File.separator + /* dateFormat.format(start) + "_" + */ logData.toString() +"_runtime.csv";
				
				FileWriter fw = new FileWriter(filename);
				
				//DONE: in der ersten Zeile(row) steht zuerst die Definition(\/firstRule/>secondRule und dann die Regelnamen
				fw.append("\\/firstRule / secondRule->;");
				for(Rule rule : rules){
					fw.append(rule.getName().toString());
					fw.append(";");
				}
				fw.append("\n");
				
				for(int row = 0; row < numberOfRules; row++ ){
					
					//DONE: in der ersten Spalteimmer die Regelnamen
					fw.append(rules.get(row).getName().toString());
					fw.append(";");
					
					//DONE: nur reine Ergebnisse durchlaufen!!! (keine Regelnamen und keine Metriken zu den Regeln!)
					for(int column = 0; column < numberOfRules; column++ ){
						fw.append(analysisDurations[row][column]);
						fw.append(";");
					}
					fw.append("\n");
				}
				
				// (17.04.2017) hinzufügen der Berechnung von SUMME, TIMEOUTS, MAXIMUM und AVERAGE
				addStatisticsToCSV(fw);
				
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void exportStoredResultsToCSV(String targetFolder){

		if(dataAdded){
			try {
				
//			Date start = new Date();
				String filename = targetFolder + File.separator + /* dateFormat.format(start) + "_" + */ logData.toString() + "_results.csv";
				
				FileWriter fw = new FileWriter(filename);
				
				//DONE: in der ersten Zeile(row) steht zuerst die Definition(\/firstRule/>secondRule und dann die Regelnamen
				fw.append("\\/firstRule / secondRule->;");
				for(Rule rule : rules){
					fw.append(rule.getName().toString());
					fw.append(";");
				}
				fw.append("\n");
				
				for(int row = 0; row < numberOfRules; row++ ){
					
					//DONE: in der ersten Spalte immer die Regelnamen
					fw.append(rules.get(row).getName().toString());
					fw.append(";");
					
					//DONE: nur reine Ergebnisse durchlaufen!!! (keine Regelnamen und keine Metriken zu den Regeln!)
					for(int column = 0; column < numberOfRules; column++ ){
						fw.append(analysisResults[row][column]);
						fw.append(";");
					}
					fw.append("\n");
				}

				// (17.04.2017) hinzufügen der Berechnung von SUMME, TIMEOUTS, MAXIMUM und AVERAGE
				addStatisticsToCSV(fw);
				
				fw.flush();
				fw.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void addStatisticsToCSV(FileWriter fw) throws IOException {
		int lastRow = numberOfRules+1;
		
		fw.append("SUM:");
		fw.append(";");
		String sum = "=SUMME(B2:CCC"+lastRow+")";
		fw.append(sum);
		fw.append(";");
		fw.append("\n");

		fw.append("TIMEOUTS:");
		fw.append(";");
		String to = "=ZÄHLENWENN(B2:CCC"+lastRow+".\"TO\")";
		fw.append(to);
		fw.append(";");
		fw.append("\n");

		fw.append("MAXIMUM:");
		fw.append(";");
		String max = "=MAX(B2:CCC"+lastRow+")";
		fw.append(max);
		fw.append(";");
		fw.append("\n");

		fw.append("AVERAGE:");
		fw.append(";");
		String avg = "=MITTELWERT(B2:CCC"+lastRow+")";
		fw.append(avg);
		fw.append(";");
		fw.append("\n");
	}

	public int getTotalResultAmount() {
		return sumUpAllNumbers(analysisResults);
	}
	
	public int getTotalRuntimeAmount() {
		return sumUpAllNumbers(analysisDurations);
	}
	
	private int sumUpAllNumbers(String[][] analysisResults2) {
		int sum = 0;
	    for(int i=0; i<analysisResults.length; i++) {
	        for(int j=0; j<analysisResults[i].length; j++) {
	        	try {
	        		int number = Integer.parseInt(analysisResults[i][j]); 
	        		sum += number;
	        	}
	        	catch (NumberFormatException nfe){
	        	}
	        }
	    }
		return sum;
	}



	// (14.04.2017) Scheint DEPRECATED zu sein. Wird nicht verwendet
//	public void addRunTimes(long totalNormalRuntime, long totalEssentialRuntime, long totalAtomicRuntime) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("Total run times [ms]- normal CPA: ");
//		sb.append(totalNormalRuntime);
//		sb.append(" essential CPA: ");
//		sb.append(totalEssentialRuntime);
//		sb.append(" atomic CPA: ");
//		sb.append(totalAtomicRuntime);
//		totalRunTimes = sb.toString();
//	}

	// (14.04.2017) Scheint DEPRECATED zu sein. Wird nicht verwendet
//	public void addTotalResults(int totalNumberOfNormalCPs, int totalNumberOfEssentialCPs, int totalNumberOfAtomicCPs,
//			int totalNumberOfConflictAtomCandidates, int totalNumberOfMinimalConflictReasons) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("Total amount of rsults -");
//		sb.append(" normal CPs: ");
//		sb.append(totalNumberOfNormalCPs);
//		sb.append(" essential CPs: ");
//		sb.append(totalNumberOfEssentialCPs);
//		sb.append(" conflict atoms: ");
//		sb.append(totalNumberOfAtomicCPs);
//		sb.append(" conflictAtomCandidates: ");
//		sb.append(totalNumberOfConflictAtomCandidates);
//		sb.append(" minimalConflictReasons: ");
//		sb.append(totalNumberOfMinimalConflictReasons);
//		totalResults = sb.toString();
//	}

	
	// das gehört nicht in die Logger Klasse!
//	public void addNormalCpaResult(CPAResult cpaResult) {
//		for(CriticalPair cp : cpaResult.getCriticalPairs()){
//			normalCpaResults.addResult(cp); //TODO: introduce a "addAll" method for CriticalPairs in the CPAResult class. 
//		}		
//	}
//
//	public void addEssentialCpaResult(CPAResult essentialResult) {
//		for(CriticalPair cp : essentialResult.getCriticalPairs()){
//			essentialCpaResults.addResult(cp); //TODO: introduce a "addAll" method for CriticalPairs in the CPAResult class. 
//		}		
//	}
//
//	public void addCoreOfConflictsResult(Rule firstRule, Rule originalRuleOfRule2, List<ConflictAtom> conflictAtoms,
//			List<Span> conflictAtomCandidates, Set<Span> minimalConflictReasons) {
//		for(Span minimalConflictReason : minimalConflictReasons){
//			//TODO(11.04.2017): es sollten nur die relevanten 'conflictAtoms' und 'conflictAtomCandidates' zum 'minimalConflictReason' übergeben werden. Wie lassen sich diese abrufen/identifizieren???  
//			MinimalConflict minimalConflict = new MinimalConflict(firstRule, originalRuleOfRule2, minimalConflictReason, conflictAtoms, conflictAtomCandidates);			
//			minimalConflicts.add(minimalConflict);
//		}
//	}
	
}
