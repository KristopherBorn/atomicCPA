package org.eclipse.emf.henshin.cpa.atomic.unitTest;

import static org.junit.Assert.*;

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
import org.junit.Before;
import org.junit.Test;

public class PushoutTests {

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
	public void pushoutResultTest_2_13() {

		Node node2InLhsOfRule1 = decapsulateAttributeRule.getLhs().getNode("2");
		Node node13InLhsOfRule2 = pullUpEncapsulatedAttributeRule.getLhs().getNode("13");

		HenshinFactory henshinFactory = new HenshinFactoryImpl();
		Graph graphOfSpan = henshinFactory.createGraph();
		Node commonNodeOfSpan = henshinFactory.createNode(graphOfSpan, node2InLhsOfRule1.getType(), "2,13");
		// Mapping
		Mapping node2InRule1Mapping = henshinFactory.createMapping(commonNodeOfSpan, node2InLhsOfRule1);
		Mapping node13InRule2Mapping = henshinFactory.createMapping(commonNodeOfSpan, node13InLhsOfRule2);

		AtomicCoreCPA atomicCoreCPA = new AtomicCoreCPA();
		Span span = atomicCoreCPA.newSpan(node2InRule1Mapping, graphOfSpan, node13InRule2Mapping);

		PushoutResult pushoutResult = atomicCoreCPA.newPushoutResult(decapsulateAttributeRule, span,
				pullUpEncapsulatedAttributeRule);
		Graph resultGraph = pushoutResult.getResultGraph();

		assertEquals(13, resultGraph.getNodes().size());

		assertEquals(16, resultGraph.getEdges().size());
	}

	@Test
	public void pushoutResultTest_2_14() {

		Node node2InLhsOfRule1 = decapsulateAttributeRule.getLhs().getNode("2");
		Node node14InLhsOfRule2 = pullUpEncapsulatedAttributeRule.getLhs().getNode("14");

		HenshinFactory henshinFactory = new HenshinFactoryImpl();
		Graph graphOfSpan = henshinFactory.createGraph();
		Node commonNodeOfSpan = henshinFactory.createNode(graphOfSpan, node2InLhsOfRule1.getType(), "2,14");

		Mapping node2InRule1Mapping = henshinFactory.createMapping(commonNodeOfSpan, node2InLhsOfRule1);
		Mapping node13InRule2Mapping = henshinFactory.createMapping(commonNodeOfSpan, node14InLhsOfRule2);

		AtomicCoreCPA atomicCoreCPA = new AtomicCoreCPA();
		Span span = atomicCoreCPA.newSpan(node2InRule1Mapping, graphOfSpan, node13InRule2Mapping);

		PushoutResult pushoutResult = atomicCoreCPA.newPushoutResult(decapsulateAttributeRule, span,
				pullUpEncapsulatedAttributeRule);
		Graph resultGraph = pushoutResult.getResultGraph();

		assertEquals(13, resultGraph.getNodes().size());

		assertEquals(16, resultGraph.getEdges().size());
	}

	@Test
	public void pushoutResultTest_5_15() {

		Node node5InLhsOfRule1 = decapsulateAttributeRule.getLhs().getNode("5");
		Node node15InLhsOfRule2 = pullUpEncapsulatedAttributeRule.getLhs().getNode("15");

		HenshinFactory henshinFactory = new HenshinFactoryImpl();
		Graph graphOfSpan = henshinFactory.createGraph();
		Node commonNodeOfSpan = henshinFactory.createNode(graphOfSpan, node5InLhsOfRule1.getType(), "5,15");

		Mapping node2InRule1Mapping = henshinFactory.createMapping(commonNodeOfSpan, node5InLhsOfRule1);
		Mapping node13InRule2Mapping = henshinFactory.createMapping(commonNodeOfSpan, node15InLhsOfRule2);

		AtomicCoreCPA atomicCoreCPA = new AtomicCoreCPA();
		Span span = atomicCoreCPA.newSpan(node2InRule1Mapping, graphOfSpan, node13InRule2Mapping);

		PushoutResult pushoutResult = atomicCoreCPA.newPushoutResult(decapsulateAttributeRule, span,
				pullUpEncapsulatedAttributeRule);
		Graph resultGraph = pushoutResult.getResultGraph();

		assertEquals(13, resultGraph.getNodes().size());

		assertEquals(16, resultGraph.getEdges().size());
	}
}
