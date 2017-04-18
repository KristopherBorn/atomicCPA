package org.eclipse.emf.henshin.cpa.atomic.runner;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.henshin.cpa.CPAOptions;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.ConflictAtom;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.Span;
import org.eclipse.emf.henshin.cpa.result.CPAResult;
import org.eclipse.emf.henshin.model.Rule;

public class AtomicResultKeeper {
	
	Rule firstRule;
	Rule secondRule; 



	List<ConflictAtom> atomicCoreCpaConflictAtoms;
	List<Span> atomicCoreCpaCandidates;
	Set<Span> atomicCoreCpaOverallReasons;
	
	
	long normalRunTime;

	public AtomicResultKeeper(Rule firstRule, Rule secondRule) {
		this.firstRule = firstRule;
		this.secondRule = secondRule;
		// TODO Auto-generated constructor stub
	}


	public Rule getFirstRule() {
		// TODO Auto-generated method stub
		return firstRule;
	}

	public Rule getSecondRule() {
		// TODO Auto-generated method stub
		return secondRule;
	}


	public void setCalculationTime(long normalRunTime) {
		this.normalRunTime = normalRunTime;
	}

	public long getNormalRunTime() {
		return normalRunTime;
	}


	public List<ConflictAtom> getConflictAtoms() {
		if(atomicCoreCpaConflictAtoms == null){
			return new LinkedList<ConflictAtom>();
		}else {			
			return atomicCoreCpaConflictAtoms;
		}
	}


	public List<Span> getCandidates() {
		return atomicCoreCpaCandidates;
	}
	

	public void setCandidates(List<Span> atomicCoreCpaCandidates) {
		this.atomicCoreCpaCandidates = atomicCoreCpaCandidates;;
	}


	public Set<Span> getOverallReasons() {
		return atomicCoreCpaOverallReasons;
	}


	public void addResult(List<ConflictAtom> atomicCoreCpaConflictAtoms) {
		this.atomicCoreCpaConflictAtoms = atomicCoreCpaConflictAtoms;
	}


	public void setOverallReasons(Set<Span> overallReasons) {
		this.atomicCoreCpaOverallReasons = overallReasons;
	}

}
