package org.eclipse.emf.henshin.cpa.atomic.tasks;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.ConflictAtom;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.ConflictReason;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.MinimalConflictReason;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.Span;
import org.eclipse.emf.henshin.model.Rule;

public class CalculateAtomicCpaTask implements Callable<List<ConflictAtom>> {

	Rule firstRule;
	Rule secondRule; 
	
	long conflictAtomRunTime;
	
	AtomicResultContainer resultKeeper;
	
	public CalculateAtomicCpaTask(AtomicResultContainer resultKeeper) {
		
		this.resultKeeper = resultKeeper;
		
		this.firstRule = resultKeeper.getFirstRule();
		this.secondRule = resultKeeper.getSecondRule();
	}

	@Override
	public List<ConflictAtom> call() throws Exception {
//		System.out.println("CALLL!");

		AtomicCoreCPA atomicCoreCPA = new AtomicCoreCPA();

		List<ConflictAtom> computeConflictAtoms = new LinkedList<AtomicCoreCPA.ConflictAtom>();

		long normalStartTime = System.currentTimeMillis();
		
		try {
			computeConflictAtoms = atomicCoreCPA.computeConflictAtoms(firstRule, secondRule);
		} catch (NullPointerException  e) {
			System.err.println("Timeout!");
		}
		
		long normalEndTime = System.currentTimeMillis();
		conflictAtomRunTime = normalEndTime - normalStartTime;
		
		resultKeeper.addResult(computeConflictAtoms);
		resultKeeper.setCalculationTime(conflictAtomRunTime);
		resultKeeper.setCandidates(atomicCoreCPA.getCandidates());
		resultKeeper.setMinimalConflictReasons(atomicCoreCPA.getMinimalConflictReasons());
		
		Set<MinimalConflictReason> minimalConflictReasons = new HashSet<MinimalConflictReason>();
		for(Span conflictReason : atomicCoreCPA.getMinimalConflictReasons()){
			minimalConflictReasons.add(atomicCoreCPA.new MinimalConflictReason(conflictReason));
		}
		long conflictReasonStartTime = System.currentTimeMillis();
		Set<ConflictReason> conflictReasons = atomicCoreCPA.computeConflictReason(minimalConflictReasons);
		long conflictReasonEndTime = System.currentTimeMillis();
		long conflictReasonAdditionalRunTime = conflictReasonEndTime - conflictReasonStartTime;
		long conflictReasonOverallRuneTime = conflictAtomRunTime + conflictReasonAdditionalRunTime;
		
		resultKeeper.setConflictReasonOverallTime(conflictReasonOverallRuneTime);
		resultKeeper.setConflictReasons(conflictReasons);
		
		return computeConflictAtoms;
	}

}
