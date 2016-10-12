package org.eclipse.emf.henshin.cpa.atomic.unitTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ComputeCandidatesTest {

	final String PATH = "testData/refactoring/";
	final String henshinFileName = "refactorings.henshin";

	Rule decapsulateAttributeRule;
	Rule pullUpEncapsulatedAttributeRule;
	
	
	@Before
	public void setUp() throws Exception {
		HenshinResourceSet resourceSet = new HenshinResourceSet(PATH);
		Module module = resourceSet.getModule(henshinFileName, false);
		
		for(Unit unit : module.getUnits()){
			if(unit.getName().equals("decapsulateAttribute"))
				decapsulateAttributeRule = (Rule) unit;
			if(unit.getName().equals("pullUpEncapsulatedAttribute"))
				pullUpEncapsulatedAttributeRule = (Rule) unit;
		}
	}

	/**
	 * TODO: sollte für die Reihenfolge decapsulate -> encapsulate der Refactoring Regeln 
	 * 		die beiden conflict atom candidates der Knoten ":Method" und ":Parameter" ermitteln  
	 */
	@Test
	public void refactoringCandidatesDecapsulateEncapsulateTest() {
		
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
