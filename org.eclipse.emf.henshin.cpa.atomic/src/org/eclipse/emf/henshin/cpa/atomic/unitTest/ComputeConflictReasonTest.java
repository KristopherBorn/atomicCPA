package org.eclipse.emf.henshin.cpa.atomic.unitTest;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
//import org.eclipse.emf.henshin.cpa.atomic.main.AtomicCoreCPA;
import org.eclipse.emf.henshin.cpa.atomic.DependencyAtom;
import org.eclipse.emf.henshin.cpa.MinimalConflict;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.ConflictAtom;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.ConflictReason;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.MinimalConflictReason;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.Span;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.eclipse.swt.internal.win32.MINMAXINFO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ComputeConflictReasonTest {

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
		public void compute_decapsulate_pullUp_ConflictReasonTest() {
			AtomicCoreCPA atomicCoreCPA = new AtomicCoreCPA();
			List<ConflictAtom> computedConflictAtoms = atomicCoreCPA.computeConflictAtoms(decapsulateAttributeRule,
					pullUpEncapsulatedAttributeRule);
			assertEquals(3, computedConflictAtoms.size());
			
			Set<ConflictAtom> methodCAs = new HashSet<ConflictAtom>();
			
			ConflictAtom conflictAtom_Method_2_13 = null;
			ConflictAtom conflictAtom_Method_3_14 = null;
			ConflictAtom conflictAtom_Parameter_5_15 = null;
	
			int numberOf_METHOD_atoms = 0;
			int numberOf_PARAMETER_atoms = 0;
			for (ConflictAtom conflictAtom : computedConflictAtoms) {
				Span span = conflictAtom.getSpan();
				Graph graph = span.getGraph();
				EList<Node> nodes = graph.getNodes();
				for (Node nodeInOverlapGraph : nodes) {
					if (nodeInOverlapGraph.getType().getName().equals("Method")) {
						numberOf_METHOD_atoms++;
						if(nodeInOverlapGraph.getName().contains("13"))
							conflictAtom_Method_2_13 = conflictAtom;
						if(nodeInOverlapGraph.getName().contains("14"))
							conflictAtom_Method_3_14 = conflictAtom;
					} else if (nodeInOverlapGraph.getType().getName().equals("Parameter")) {
						numberOf_PARAMETER_atoms++;
						if(nodeInOverlapGraph.getName().contains("15"))
							conflictAtom_Parameter_5_15 = conflictAtom;
					} else {
						assertTrue("node of wrong type in overlap graph", false);
					}
				}
			}
			assertEquals(2, numberOf_METHOD_atoms);
			assertEquals(1, numberOf_PARAMETER_atoms);
			
			Set<Span> allMinimalConflictReasons = new HashSet<Span>();
			//TODO: check that the two Reasons had been found AND that the three ConflictAtoms only have two (minimal)conflict reasons!
			for(ConflictAtom conflictAtom : computedConflictAtoms){
				Set<Span> reasons = conflictAtom.getReasons();
				Assert.assertEquals(1, reasons.size());
				allMinimalConflictReasons.addAll(reasons);
			}
			Assert.assertEquals(2, allMinimalConflictReasons.size());
			
			Span conflictReasonOfMethod_3_14_Atom = conflictAtom_Method_3_14.getReasons().iterator().next();
			Span conflictReasonOfParameter_5_15_Atom = conflictAtom_Parameter_5_15.getReasons().iterator().next();
	//		System.out.println(conflictReasonOfMethod_3_14_Atom);
	//		System.out.println(conflictReasonOfParameter_5_15_Atom);
			Assert.assertTrue(conflictReasonOfMethod_3_14_Atom.equals(conflictReasonOfParameter_5_15_Atom));
			
			Set<MinimalConflictReason> minimalConflictReasons = new HashSet<MinimalConflictReason>();
			for(Span conflictReason : allMinimalConflictReasons){
				minimalConflictReasons.add(atomicCoreCPA.new MinimalConflictReason(conflictReason));
			}
			
			Set<ConflictReason> computeConflictReason = atomicCoreCPA.computeConflictReason(minimalConflictReasons);
			Assert.assertEquals(3, computeConflictReason.size());
			
			//TODO: pr�fen, dass es 5 CofnlictREason gibt, die sich aus einem zusammensetzen und 3 die sich asu zweien zusammen setzen und einen, der sich aus dreien zusammensetzt!
			
			
	//		TODO: Pr�fen!
	//		Es sollten die beiden bekannten minimal conflict reason enthalten sein (Siehe auch �bersichtsgrafik. Dort ist notiert, dass jedes MCR auch ein CR ist)
	//		Au�erdem sollte die zus�tzliche Kombination entstehen!
	//		F�r zus�tzliche Kombination pr�fen, dass der Graph f�nf Knoten hat. zweimal Class, zweimal Method und einmal PArameter
	//		Au�erdem die jeweiligen Mappings pr�fen!
			
		}

	@Test
	public void compute_pullUp_decapsulate_ConflictReasonTest() {
		AtomicCoreCPA atomicCoreCPA = new AtomicCoreCPA();
		List<ConflictAtom> computedConflictAtoms = atomicCoreCPA.computeConflictAtoms(pullUpEncapsulatedAttributeRule, decapsulateAttributeRule);
		assertEquals(5, computedConflictAtoms.size());
		
		Set<ConflictAtom> methodCAs = new HashSet<ConflictAtom>();

		// alles alter TestCode durchs kopieren der im Papier betrachteten Regelreihenfolge!
//		ConflictAtom conflictAtom_Method_2_13 = null;
//		ConflictAtom conflictAtom_Method_3_14 = null;
//		ConflictAtom conflictAtom_Parameter_5_15 = null;
//
//		int numberOf_METHOD_atoms = 0;
//		int numberOf_PARAMETER_atoms = 0;
//		for (ConflictAtom conflictAtom : computedConflictAtoms) {
//			Span span = conflictAtom.getSpan();
//			Graph graph = span.getGraph();
//			EList<Node> nodes = graph.getNodes();
//			for (Node nodeInOverlapGraph : nodes) {
//				if (nodeInOverlapGraph.getType().getName().equals("Method")) {
//					numberOf_METHOD_atoms++;
//					if(nodeInOverlapGraph.getName().contains("13"))
//						conflictAtom_Method_2_13 = conflictAtom;
//					if(nodeInOverlapGraph.getName().contains("14"))
//						conflictAtom_Method_3_14 = conflictAtom;
//				} else if (nodeInOverlapGraph.getType().getName().equals("Parameter")) {
//					numberOf_PARAMETER_atoms++;
//					if(nodeInOverlapGraph.getName().contains("15"))
//						conflictAtom_Parameter_5_15 = conflictAtom;
//				} else {
//					assertTrue("node of wrong type in overlap graph", false);
//				}
//			}
//		}
//		assertEquals(2, numberOf_METHOD_atoms);
//		assertEquals(1, numberOf_PARAMETER_atoms);
		
		Set<Span> allMinimalConflictReasons = new HashSet<Span>();
		//TODO: check that the two Reasons had been found AND that the three ConflictAtoms only have two (minimal)conflict reasons!
		for(ConflictAtom conflictAtom : computedConflictAtoms){
			Set<Span> reasons = conflictAtom.getReasons();
			Assert.assertEquals(1, reasons.size());
			allMinimalConflictReasons.addAll(reasons);
		}
		Assert.assertEquals(5, allMinimalConflictReasons.size());
		
		// alles alter TestCode durchs kopieren der im Papier betrachteten Regelreihenfolge!
//		Span conflictReasonOfMethod_3_14_Atom = conflictAtom_Method_3_14.getReasons().iterator().next();
//		Span conflictReasonOfParameter_5_15_Atom = conflictAtom_Parameter_5_15.getReasons().iterator().next();
////		System.out.println(conflictReasonOfMethod_3_14_Atom);
////		System.out.println(conflictReasonOfParameter_5_15_Atom);
//		Assert.assertTrue(conflictReasonOfMethod_3_14_Atom.equals(conflictReasonOfParameter_5_15_Atom));
		
		Set<MinimalConflictReason> minimalConflictReasons = new HashSet<MinimalConflictReason>();
		for(Span conflictReason : allMinimalConflictReasons){
			minimalConflictReasons.add(atomicCoreCPA.new MinimalConflictReason(conflictReason));
		}
		
		Set<ConflictReason> computeConflictReason = atomicCoreCPA.computeConflictReason(minimalConflictReasons);
		Assert.assertEquals(17, computeConflictReason.size());
		
		//von diesen 17 potentiellen CRs verletzen 10 die dangling condition. 
		// Das sind jeweils diejenigen, die die beiden folgenden Conflict Atoms enhalten:
		// 11_1 --> 13_3
		// 11_1 --> 14_2
		// dies k�nnte sich ggf. �ber ein "isApplicable" der zweiten Regel (hier 'decapsulate...') identifizieren
		// ABER: dazu m�sste zuerst aus den ConflictREasons jeweils ein vollst�ndiger Overlap als Instanz erzeugt werden.
		// SEHR AUFWENDIG!!!
		
	}

}
