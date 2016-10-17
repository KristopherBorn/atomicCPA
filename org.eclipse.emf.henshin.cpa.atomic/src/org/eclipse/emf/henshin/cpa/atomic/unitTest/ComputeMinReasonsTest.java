package org.eclipse.emf.henshin.cpa.atomic.unitTest;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.ConflictAtom;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.Span;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ComputeMinReasonsTest {

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
		
//		assertThat(conflictAtomCandidates.size(), is(equalTo(5)));
		assertEquals(5, conflictAtomCandidates.size());
		
		for(AtomicCoreCPA.Span candidate : conflictAtomCandidates){
			EList<Node> nodesOfCandidate = candidate.getGraph().getNodes();
			assertEquals(1, nodesOfCandidate.size());
			Node nodeOfCandidate = nodesOfCandidate.get(0); 
//			System.out.println(nodeOfCandidate.getName() +":" +nodeOfCandidate.getType().getName());
		}
		
		List<Span> reasons = new LinkedList<>();//
		for(Span candidate : conflictAtomCandidates){
			atomicCoreCPA.computeMinReasons(decapsulateAttributeRule, pullUpEncapsulatedAttributeRule, candidate, reasons);
		}
		for(Span minReason : reasons){
			// wieso sind es 8 minReason? Das erscheint mir nicht korrekt. 
			Graph graphOfMinReason = minReason.getGraph();
			EList<Node> nodesOfMinReason = graphOfMinReason.getNodes();
			System.out.println("NumberOfnodesInMinReason: " +nodesOfMinReason.size());
			System.out.println(nodesOfMinReason.toString());
		}
//			if(reasons.isEmpty()){
//				result.add(new ConflictAtom(candidate, reasons));
//				//TODO: wieso ein Atom die "reasons" benötigt ist mir noch unklar.
//				// Bzw.was die Datenstruktur "Atom" überhaupt umfasst.
//			}

		//TODO: Wieso überhaupt Spans? 
		
		//TODO: assertion, dass alle fünf Spans mit jeweils entsprechendem instance node und den beiden Mappings oder matches gefunden/erzeugt wurden.
		
		// jeweils zu jeder der beiden "Method" aus Regel1 eine Kombination mit der "Method" aus Regel (2*2 = 4) + einmal "Parameter" aus Regel1 mit "Parameter" aus Regel2
		// --> 5 insgesamt!
		
		//TODO: ggf. auch jeweils speichern ins Dateisystem und prüfen, dass das funktioniert und richtig instanziiert wurde.
		
	}
}
