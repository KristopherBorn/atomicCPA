package org.eclipse.emf.henshin.cpa.atomic;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.emf.henshin.cpa.CPAOptions;
import org.eclipse.emf.henshin.cpa.CpaByAGG;
import org.eclipse.emf.henshin.cpa.ICriticalPairAnalysis;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.Span;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AtomicUnitTests {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// TODO: sollte für die Reihenfolge decapsulate -> encpsulate der Refactoring Regeln die cnadidates
	/**
	 * TODO: sollte für die Reihenfolge decapsulate -> encapsulate der Refactoring Regeln 
	 * 		die beiden conflict atom candidates der Knoten ":Method" und ":Parameter" ermitteln  
	 */
	@Test
	public void refactoringCandidatesDecapsulateEncapsulateTest() {
		

		final String PATH = "testData/refactoring/";
		final String henshinFileName = "refactoring.henshin";

		Module module = resourceSet.getModule(henshinFileName, false);
//		private ICriticalPairAnalysis cpaByAgg;
//		CPAOptions cpaOptions;
		
		// Create a resource set with a base directory:
		HenshinResourceSet resourceSet = new HenshinResourceSet(PATH);
		//
//		cpaByAgg = new CpaByAGG();
//		cpaOptions = new CPAOptions();
		
		Rule decapsulateAttributeRule;
		Rule pullUpEncapsulatedAttributeRule;
		
		for(Unit unit : module.getUnits()){
			if(unit.getName().equals("decapsulateAttribute"))
				decapsulateAttributeRule = unit;
			if(unit.getName().equals("pullUpEncapsulatedAttribute"))
				pullUpEncapsulatedAttributeRule = unit;
		}
		//TODO: add JUnit Assert or other check that rule1 & rule2 != NULL!
		
		AtomicCoreCPA atomicCoreCPA = new AtomicUnitTests();
		
		List<Span> conflictAtomCandidates = atomicCoreCPA.computeCandidates(decapsulateAttributeRule, pullUpEncapsulatedAttributeRule);
		
		//TODO: assertion, dass beide Spans
		
		fail("Not yet implemented");
	}

}
