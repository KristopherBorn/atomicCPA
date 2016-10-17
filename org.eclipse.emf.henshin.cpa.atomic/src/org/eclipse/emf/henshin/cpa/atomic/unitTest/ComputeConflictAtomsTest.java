package org.eclipse.emf.henshin.cpa.atomic.unitTest;

import static org.junit.Assert.*;

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

public class ComputeConflictAtomsTest {

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


	@Test
	public void computeConflictAtomsTest() {
		AtomicCoreCPA atomicCoreCPA = new AtomicCoreCPA();
		List<ConflictAtom> computedConflictAtoms = atomicCoreCPA.computeConflictAtoms(decapsulateAttributeRule, pullUpEncapsulatedAttributeRule);
//		assertEquals(3, computedConflictAtoms.size());
		
		Bisherige Erkenntnis: es werden bisher zwei ConflictAtoms erkannt mit jeweils "Method" im OverlapGraph.
		-> Wieso wird "Parameter" nicht als Conflict Atom erkannt?
				- erste Erkenntnis: Im Test "ComputeCandidates" werden Method(4x) und Parameter(1x) gefunden.
				- Die Ausgabe von "ComputeMinReasonsTest" legt nahe, dass die 
		-> Entsprechend prüfen, dass es sich bei den bei den Methods um 2:13 und 3:14 handelt.
		-> Wieso haben die Knoten im OverlapGraph keine Namen anstelle von "2_13" und "3_14"
		
		for(ConflictAtom conflictAtom : computedConflictAtoms){
			Span span = conflictAtom.getSpan();
			Graph graph = span.getGraph();
			EList<Node> nodes = graph.getNodes();
			System.out.println("number of nodes: "+nodes.size());
			for(Node nodeInOverlapGraph : nodes){
				System.out.println("Name of nodes: " +nodeInOverlapGraph.getName());
			}
		}
	}

}
