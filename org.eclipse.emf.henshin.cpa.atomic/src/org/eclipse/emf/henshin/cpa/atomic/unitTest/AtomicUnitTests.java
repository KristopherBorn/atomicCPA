package org.eclipse.emf.henshin.cpa.atomic.unitTest;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.PushoutResult;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.Span;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.HenshinFactory;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.model.impl.HenshinFactoryImpl;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

import junit.framework.Assert;

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


	/**
	 * TODO: sollte für die Reihenfolge decapsulate -> encapsulate der Refactoring Regeln 
	 * 		die beiden conflict atom candidates der Knoten ":Method" und ":Parameter" ermitteln  
	 */
	@Test
	public void refactoringCandidatesDecapsulateEncapsulateTest() {
		

		final String PATH = "testData/refactoring/";
		final String henshinFileName = "refactorings.henshin";

		HenshinResourceSet resourceSet = new HenshinResourceSet(PATH);
		
		Module module = resourceSet.getModule(henshinFileName, false);
//		private ICriticalPairAnalysis cpaByAgg;
//		CPAOptions cpaOptions;
		
		// Create a resource set with a base directory:
		//
//		cpaByAgg = new CpaByAGG();
//		cpaOptions = new CPAOptions();
		
		Rule decapsulateAttributeRule = null;
		Rule pullUpEncapsulatedAttributeRule = null;
		
		for(Unit unit : module.getUnits()){
			if(unit.getName().equals("decapsulateAttribute"))
				decapsulateAttributeRule = (Rule) unit;
			if(unit.getName().equals("pullUpEncapsulatedAttribute"))
				pullUpEncapsulatedAttributeRule = (Rule) unit;
		}
		//TODO: add JUnit Assert or other check that rule1 & rule2 != NULL!
		
		AtomicCoreCPA atomicCoreCPA = new AtomicCoreCPA();
		
		List<AtomicCoreCPA.Span> conflictAtomCandidates = atomicCoreCPA.computeCandidates(decapsulateAttributeRule, pullUpEncapsulatedAttributeRule);
		
		System.out.println("HALT");
		
		assertThat(conflictAtomCandidates.size(), is(equalTo(5)));


		//TODO: Wieso überhaupt Spans? 
		
		//TODO: assertion, dass alle fünf Spans mit jeweils entsprechendem instance node und den beiden Mappings oder matches gefunden/erzeugt wurden.
		
		// jeweils zu jeder der beiden "Method" aus Regel1 eine Kombination mit der "Method" aus Regel (2*2 = 4) + einmal "Parameter" aus Regel1 mit "Parameter" aus Regel2
		// --> 5 insgesamt!
		
		//TODO: ggf. auch jeweils speichern ins Dateisystem und prüfen, dass das funktioniert und richtig instanziiert wurde.
		
	}
	
	
}
