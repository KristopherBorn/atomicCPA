package org.eclipse.emf.henshin.cpa.atomic.runner;

import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.emf.henshin.cpa.CPAOptions;
import org.eclipse.emf.henshin.cpa.CpaByAGG;
import org.eclipse.emf.henshin.cpa.ICriticalPairAnalysis;
import org.eclipse.emf.henshin.cpa.UnsupportedRuleException;
import org.eclipse.emf.henshin.cpa.result.CPAResult;
import org.eclipse.emf.henshin.model.Rule;

public class CalculateCpaTask implements Callable<CPAResult> {

	List<Rule> firstRuleList;
	List<Rule> secondRuleList; 
	CPAOptions normalOptions;
	
	ICriticalPairAnalysis normalCpa;
	
	long normalRunTime;
	
	ResultKeeper detector;
	
	public CalculateCpaTask(ResultKeeper detector) {
		
		this.detector = detector;
		
		this.firstRuleList = detector.getFirstRuleList();
		this.secondRuleList = detector.getSecondRuleList();
		this.normalOptions = detector.getNormalOptions();	
		

		// normal CPA setup
		normalCpa = new CpaByAGG();
		CPAOptions normalOptions = new CPAOptions();
		CPAResult normalResult = null;
				
//		long normalStartTime = System.currentTimeMillis();
//		try {
//			normalCpa.init(firstRuleList, secondRuleList, normalOptions);
//			normalResult = normalCpa.runConflictAnalysis();
//		} catch (UnsupportedRuleException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//
//		long normalEndTime = System.currentTimeMillis();
//		long normalRunTime = normalEndTime - normalStartTime;
		
		// TODO Auto-generated constructor stub
	}

	@Override
	public CPAResult call() throws Exception {
		System.out.println("CALLL!");

		CPAResult normalResult = null;

		long normalStartTime = System.currentTimeMillis();
		try {
			normalCpa.init(firstRuleList, secondRuleList, normalOptions);
			normalResult = normalCpa.runConflictAnalysis();
		} catch (Exception /*UnsupportedRuleException*/ e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		long normalEndTime = System.currentTimeMillis();
		normalRunTime = normalEndTime - normalStartTime;
		
		detector.addResult(normalResult);
		detector.setCalculationTime(normalRunTime);
		
		return normalResult;
	}

}
