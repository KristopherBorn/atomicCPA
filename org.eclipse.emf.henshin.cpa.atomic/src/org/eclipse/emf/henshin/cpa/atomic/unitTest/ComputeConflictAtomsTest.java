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
import org.junit.Before;
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

		for (Unit unit : module.getUnits()) {
			if (unit.getName().equals("decapsulateAttribute"))
				decapsulateAttributeRule = (Rule) unit;
			if (unit.getName().equals("pullUpEncapsulatedAttribute"))
				pullUpEncapsulatedAttributeRule = (Rule) unit;
		}
	}

	@Test
	public void computeConflictAtomsTest() {
		AtomicCoreCPA atomicCoreCPA = new AtomicCoreCPA();
		List<ConflictAtom> computedConflictAtoms = atomicCoreCPA.computeConflictAtoms(decapsulateAttributeRule,
				pullUpEncapsulatedAttributeRule);
		assertEquals(3, computedConflictAtoms.size());

		int numberOf_METHOD_atoms = 0;
		int numberOf_PARAMETER_atoms = 0;
		for (ConflictAtom conflictAtom : computedConflictAtoms) {
			Span span = conflictAtom.getSpan();
			Graph graph = span.getGraph();
			EList<Node> nodes = graph.getNodes();
			for (Node nodeInOverlapGraph : nodes) {
				if (nodeInOverlapGraph.getType().getName().equals("Method")) {
					numberOf_METHOD_atoms++;
				} else if (nodeInOverlapGraph.getType().getName().equals("Parameter")) {
					numberOf_PARAMETER_atoms++;
				} else {
					assertTrue("node of wrong type in overlap graph", false);
				}
			}
		}
		assertEquals(2, numberOf_METHOD_atoms);
		assertEquals(1, numberOf_PARAMETER_atoms);
	}

}
