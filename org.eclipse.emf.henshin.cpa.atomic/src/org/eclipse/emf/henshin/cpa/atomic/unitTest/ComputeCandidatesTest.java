package org.eclipse.emf.henshin.cpa.atomic.unitTest;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.junit.Before;
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

	@Test
	public void refactoringCandidatesDecapsulateEncapsulateTest() {
		
		AtomicCoreCPA atomicCoreCPA = new AtomicCoreCPA();
		
		List<AtomicCoreCPA.Span> conflictAtomCandidates = atomicCoreCPA.computeCandidates(decapsulateAttributeRule, pullUpEncapsulatedAttributeRule);
		
		System.out.println("HALT");
		
		assertEquals(5, conflictAtomCandidates.size());
		
		for(AtomicCoreCPA.Span candidate : conflictAtomCandidates){
			EList<Node> nodesOfCandidate = candidate.getGraph().getNodes();
			assertEquals(1, nodesOfCandidate.size());
		}
	}
}
