package org.eclipse.emf.henshin.cpa.atomic.unitTest;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.PushoutResult;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.Span;
import org.eclipse.emf.henshin.model.Edge;
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

public class FindFixingEdgesTest {

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

	//involves "findDanglingEdges()" before. This method is already tested in another TestClass test.
	@Test
	public void findFixingEdges_2_13() {
		
		Node node2InLhsOfRule1 = decapsulateAttributeRule.getLhs().getNode("2");
		Node node13InLhsOfRule2 = pullUpEncapsulatedAttributeRule.getLhs().getNode("13");
		
		HenshinFactory henshinFactory = new HenshinFactoryImpl();
		Graph graphOfSpan = henshinFactory.createGraph();
		Node commonNodeOfSpan = henshinFactory.createNode(graphOfSpan, node2InLhsOfRule1.getType(), "2,13");
		
		Mapping node2InRule1Mapping = henshinFactory.createMapping(commonNodeOfSpan, node2InLhsOfRule1);
		Mapping node13InRule2Mapping = henshinFactory.createMapping(commonNodeOfSpan, node13InLhsOfRule2);
		
		AtomicCoreCPA atomicCoreCPA = new AtomicCoreCPA();
		Span span = atomicCoreCPA.newSpan(node2InRule1Mapping, graphOfSpan, node13InRule2Mapping);
		
		PushoutResult pushoutResult = atomicCoreCPA.newPushoutResult(decapsulateAttributeRule, span, pullUpEncapsulatedAttributeRule);
		Graph resultGraph = pushoutResult.getResultGraph();
		
		List<Edge> findDanglingEdges = atomicCoreCPA.findDanglingEdges(decapsulateAttributeRule, pushoutResult.getMappingsOfRule1());
		
		assertEquals(2, findDanglingEdges.size());
		
		// prüfen, dass es sich um eine Kante von "11:Class" nach "13:Method" handelt und um eine Kante von "13:Method" nach "16:Class".
		boolean methodsEdgeDetected = false;
		boolean typeEdgeDetected = false;
		
		for(Edge danglingEdge : findDanglingEdges){
			if(danglingEdge.getType().getName().equals("methods")){
				//Do check source and target!
				Node sourceNode = danglingEdge.getSource();
				String sourceNodeName = sourceNode.getName();
				if(sourceNodeName.equals("11"))
					methodsEdgeDetected = true;
			}
			if(danglingEdge.getType().getName().equals("type")){
				Node targetNode = danglingEdge.getTarget();
				String targetNodeName = targetNode.getName();
				if(targetNodeName.equals("16"))
					typeEdgeDetected = true;
			}
		}
		
		// already assured in another test 
		assertEquals(true, methodsEdgeDetected);
		assertEquals(true, typeEdgeDetected);
		
		for(Edge danglingEdge : findDanglingEdges){
			if(danglingEdge.getType().getName().equals("methods")){
				List<Edge> findFixingEdges = atomicCoreCPA.findFixingEdges(decapsulateAttributeRule, pullUpEncapsulatedAttributeRule, span, danglingEdge, pushoutResult.getMappingsOfRule1(), pushoutResult.getMappingsOfRule2());
//				TODO: was ist hier zu erwarten???
//						Dass die "methods"-Kante zwischen "1:Class" und "2:Method" der ersten Regel zurückgegeben wird.
				assertEquals(1, findFixingEdges.size());
				assertEquals("methods", findFixingEdges.get(0).getType().getName());
				assertEquals(decapsulateAttributeRule.getLhs(), findFixingEdges.get(0).getGraph());
			}
			if(danglingEdge.getType().getName().equals("type")){
				List<Edge> findFixingEdges = atomicCoreCPA.findFixingEdges(decapsulateAttributeRule, pullUpEncapsulatedAttributeRule, span, danglingEdge, pushoutResult.getMappingsOfRule1(), pushoutResult.getMappingsOfRule2());
//				TODO: was ist hier zu erwarten???
//					Dass die "type"-Kante zwischen "2:Method" und "6:Class" der ersten Regel zurückgegeben wird.
				assertEquals(1, findFixingEdges.size());
				assertEquals("type", findFixingEdges.get(0).getType().getName());
				assertEquals(decapsulateAttributeRule.getLhs(), findFixingEdges.get(0).getGraph());
			}
		}
		
	}

}
