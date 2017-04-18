package org.eclipse.emf.henshin.cpa.atomic.runner;

import java.util.List;

import org.eclipse.emf.henshin.cpa.CPAOptions;
import org.eclipse.emf.henshin.cpa.result.CPAResult;
import org.eclipse.emf.henshin.model.Rule;

public class ResultKeeper {
	
	List<Rule> firstRuleList;
	List<Rule> secondRuleList; 
	CPAOptions normalOptions;
	
	CPAResult normalResult;
	
	long normalRunTime;

	public ResultKeeper(List<Rule> firstRuleList, List<Rule> secondRuleList, CPAOptions normalOptions) {
		this.firstRuleList = firstRuleList;
		this.secondRuleList = secondRuleList;
		this.normalOptions = normalOptions;
		// TODO Auto-generated constructor stub
	}

	public List<Rule> getFirstRuleList() {
		// TODO Auto-generated method stub
		return firstRuleList;
	}

	public List<Rule> getSecondRuleList() {
		// TODO Auto-generated method stub
		return secondRuleList;
	}

	public CPAOptions getNormalOptions() {
		// TODO Auto-generated method stub
		return normalOptions;
	}

	public void addResult(CPAResult normalResult) {
		this.normalResult = normalResult;
	}

	public CPAResult getResult() {
		return normalResult;
	}

	public void setCalculationTime(long normalRunTime) {
		this.normalRunTime = normalRunTime;
	}

	public long getNormalRunTime() {
		return normalRunTime;
	}

}
