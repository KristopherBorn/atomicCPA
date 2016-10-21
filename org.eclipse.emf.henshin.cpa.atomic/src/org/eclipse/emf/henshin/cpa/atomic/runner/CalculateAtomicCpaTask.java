package org.eclipse.emf.henshin.cpa.atomic.runner;

import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.emf.henshin.cpa.CPAOptions;
import org.eclipse.emf.henshin.cpa.CpaByAGG;
import org.eclipse.emf.henshin.cpa.ICriticalPairAnalysis;
import org.eclipse.emf.henshin.cpa.UnsupportedRuleException;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.ConflictAtom;
import org.eclipse.emf.henshin.cpa.result.CPAResult;
import org.eclipse.emf.henshin.model.Rule;

public class CalculateAtomicCpaTask implements Callable<List<ConflictAtom>> {

	Rule firstRule;
	Rule secondRule; 
	
	long normalRunTime;
	
	AtomicResultKeeper resultKeeper;
	
	public CalculateAtomicCpaTask(AtomicResultKeeper resultKeeper) {
		
		this.resultKeeper = resultKeeper;
		
		this.firstRule = resultKeeper.getFirstRule();
		this.secondRule = resultKeeper.getSecondRule();
		

		// normal CPA setup

				
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
	public List<ConflictAtom> call() throws Exception {
		System.out.println("CALLL!");

		AtomicCoreCPA atomicCoreCPA = new AtomicCoreCPA();

		long normalStartTime = System.currentTimeMillis();

		List<ConflictAtom> computeConflictAtoms = atomicCoreCPA.computeConflictAtoms(firstRule, secondRule);
		
		long normalEndTime = System.currentTimeMillis();
		normalRunTime = normalEndTime - normalStartTime;
		
		resultKeeper.addResult(computeConflictAtoms);
		resultKeeper.setCalculationTime(normalRunTime);
		resultKeeper.setCandidates(atomicCoreCPA.getCandidates());
		resultKeeper.setOverallReasons(atomicCoreCPA.getOverallReasons());
		
		return computeConflictAtoms;
	}

}
