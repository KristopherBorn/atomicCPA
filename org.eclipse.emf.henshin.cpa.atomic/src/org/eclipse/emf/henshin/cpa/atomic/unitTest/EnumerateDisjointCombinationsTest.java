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

public class EnumerateDisjointCombinationsTest {

	final String PATH = "testData/refactoring/";
	final String henshinFileName = "refactorings.henshin";

	Rule decapsulateAttributeRule;
	Rule pullUpEncapsulatedAttributeRule;
	
	List<Edge> danglingEdges;
	AtomicCoreCPA atomicCoreCPA;
	Span span;
	PushoutResult pushoutResult;


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
		
		
		Node node2InLhsOfRule1 = decapsulateAttributeRule.getLhs().getNode("2");
		Node node13InLhsOfRule2 = pullUpEncapsulatedAttributeRule.getLhs().getNode("13");
		
		HenshinFactory henshinFactory = new HenshinFactoryImpl();
		Graph graphOfSpan = henshinFactory.createGraph();
		Node commonNodeOfSpan = henshinFactory.createNode(graphOfSpan, node2InLhsOfRule1.getType(), "2,13");
		
		Mapping node2InRule1Mapping = henshinFactory.createMapping(commonNodeOfSpan, node2InLhsOfRule1);
		Mapping node13InRule2Mapping = henshinFactory.createMapping(commonNodeOfSpan, node13InLhsOfRule2);
		
		atomicCoreCPA = new AtomicCoreCPA();
		span = atomicCoreCPA.newSpan(node2InRule1Mapping, graphOfSpan, node13InRule2Mapping);
		
		pushoutResult = atomicCoreCPA.newPushoutResult(decapsulateAttributeRule, span, pullUpEncapsulatedAttributeRule);
		Graph resultGraph = pushoutResult.getResultGraph(); //TODO: seems to be superfluous!
		
		danglingEdges = atomicCoreCPA.findDanglingEdges(decapsulateAttributeRule, pushoutResult.getMappingsOfRule1());
		
		assertEquals(2, danglingEdges.size());
		
		// prüfen, dass es sich um eine Kante von "11:Class" nach "13:Method" handelt und um eine Kante von "13:Method" nach "16:Class".
		boolean methodsEdgeDetected = false;
		boolean typeEdgeDetected = false;
		
		for(Edge danglingEdge : danglingEdges){
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
	}

	//involves "findDanglingEdges()" in the setUp method. This method is already tested in another TestClass test.
	@Test
	public void enumerateDisjointCombinations_11_13_AND_13_16_methodsEdge() {

		
		List<Edge> fixingEdgesOfMethodsEdge = null;
		List<Edge> fixingEdgesOfTypeEdges = null;
		
		for(Edge danglingEdge : danglingEdges){
			if(danglingEdge.getType().getName().equals("methods")){
				fixingEdgesOfMethodsEdge = atomicCoreCPA.findFixingEdges(decapsulateAttributeRule, pullUpEncapsulatedAttributeRule, span, danglingEdge, pushoutResult.getMappingsOfRule1(), pushoutResult.getMappingsOfRule2());
				//TODO: TODOs entfernen  + Verweis, dass bereits an anderer Stelle getestet.
//				TODO: was ist hier zu erwarten???
//						Dass die "methods"-Kante zwischen "1:Class" und "2:Method" der ersten Regel zurückgegeben wird.
				assertEquals(1, fixingEdgesOfMethodsEdge.size());
				assertEquals("methods", fixingEdgesOfMethodsEdge.get(0).getType().getName());
				assertEquals(decapsulateAttributeRule.getLhs(), fixingEdgesOfMethodsEdge.get(0).getGraph());
				
				List<Span> disjointCombinationsToFixDanglingMethodsEdge = atomicCoreCPA.enumerateDisjointCombinations(span, fixingEdgesOfMethodsEdge);
				assertEquals(1, disjointCombinationsToFixDanglingMethodsEdge.size());
				// erwartet: Der Span sollte beide Knoten haben, Class, Method und die methodsKante
				Span extendedMethodsEdgeSpan = disjointCombinationsToFixDanglingMethodsEdge.get(0);
				Graph graphOfExtendedMethodsEdgeSpan = extendedMethodsEdgeSpan.getGraph();
				assertEquals(1, graphOfExtendedMethodsEdgeSpan.getEdges().size());
				Edge methodsEdge = graphOfExtendedMethodsEdgeSpan.getEdges().get(0);
				assertEquals("methods", methodsEdge.getType().getName());
				assertEquals(2, graphOfExtendedMethodsEdgeSpan.getNodes().size());
				assertEquals("Class", methodsEdge.getSource().getType().getName());
				assertEquals("Method", methodsEdge.getTarget().getType().getName());
				Node classNodeInSpan = methodsEdge.getSource();
				Node methodNodeInSpan = methodsEdge.getTarget();
				// In Rule1: source: 1:Class, target: 2:Method
				assertEquals(2, extendedMethodsEdgeSpan.getMappingsInRule1().size());
				Mapping mappingClassInSpanToRule1 = extendedMethodsEdgeSpan.getMappingInRule1(classNodeInSpan);
				assertNotNull(mappingClassInSpanToRule1);
				Node classInRule1 = mappingClassInSpanToRule1.getImage();
				assertNotNull(classInRule1);
				assertEquals("Class", classInRule1.getType().getName());
				Mapping mappingMethodInSpanToRule1 = extendedMethodsEdgeSpan.getMappingInRule1(methodNodeInSpan);
				assertNotNull(mappingMethodInSpanToRule1);
				Node methodInRule1 = mappingMethodInSpanToRule1.getImage();
				assertNotNull(methodInRule1);
				assertEquals("Method", methodInRule1.getType().getName());
				// In Rule2: soure: 11:Class, target 13:Method
				assertEquals(2, extendedMethodsEdgeSpan.getMappingsInRule2().size());
				Mapping mappingClassInSpanToRule2 = extendedMethodsEdgeSpan.getMappingInRule2(classNodeInSpan);
				assertNotNull(mappingClassInSpanToRule2);
				Node classInRule2 = mappingClassInSpanToRule2.getImage();
				assertNotNull(classInRule2);
				assertEquals("Class", classInRule2.getType().getName());
				Mapping mappingMethodInSpanToRule2 = extendedMethodsEdgeSpan.getMappingInRule2(methodNodeInSpan);
				assertNotNull(mappingMethodInSpanToRule2);
				Node methodInRule2 = mappingMethodInSpanToRule2.getImage();
				assertNotNull(methodInRule2);
				assertEquals("Method", methodInRule2.getType().getName());
			}
		}
	}
	

	//involves "findDanglingEdges()" in the setUp method. This method is already tested in another TestClass test.
	@Test
	public void enumerateDisjointCombinations_11_13_AND_13_16_typeEdge() {

		
		List<Edge> fixingEdgesOfMethodsEdge = null;
		List<Edge> fixingEdgesOfTypeEdges = null;
		
		for(Edge danglingEdge : danglingEdges){
			if(danglingEdge.getType().getName().equals("type")){
				fixingEdgesOfTypeEdges = atomicCoreCPA.findFixingEdges(decapsulateAttributeRule, pullUpEncapsulatedAttributeRule, span, danglingEdge, pushoutResult.getMappingsOfRule1(), pushoutResult.getMappingsOfRule2());
//				TODO: was ist hier zu erwarten???
//					Dass die "type"-Kante zwischen "2:Method" und "6:Class" der ersten Regel zurückgegeben wird.
				assertEquals(1, fixingEdgesOfTypeEdges.size());
				assertEquals("type", fixingEdgesOfTypeEdges.get(0).getType().getName());
				assertEquals(decapsulateAttributeRule.getLhs(), fixingEdgesOfTypeEdges.get(0).getGraph());

				List<Span> disjointCombinationsToFixDanglingTypeEdge = atomicCoreCPA.enumerateDisjointCombinations(span, fixingEdgesOfTypeEdges);
				assertEquals(1, disjointCombinationsToFixDanglingTypeEdge.size());
				// erwartet: Der Span sollte beide Knoten haben, Method, Class und die typeKante
				Span extendedSpanTypeEdge = disjointCombinationsToFixDanglingTypeEdge.get(0);
				Graph graphOfExtendedTypeEdgeSpan = extendedSpanTypeEdge.getGraph();
				assertEquals(1, graphOfExtendedTypeEdgeSpan.getEdges().size());
				Edge typeEdge = graphOfExtendedTypeEdgeSpan.getEdges().get(0);
				assertEquals("type", typeEdge.getType().getName());
				assertEquals(2, graphOfExtendedTypeEdgeSpan.getNodes().size());
				assertEquals("Method", typeEdge.getSource().getType().getName());
				assertEquals("Class", typeEdge.getTarget().getType().getName());
				Node methodNodeInSpan = typeEdge.getSource();
				Node classNodeInSpan = typeEdge.getTarget();
				// In Rule1: source: 1:Class, target: 2:Method
				assertEquals(2, extendedSpanTypeEdge.getMappingsInRule1().size());
				Mapping mappingMethodInSpanToRule1 = extendedSpanTypeEdge.getMappingInRule1(methodNodeInSpan);
				assertNotNull(mappingMethodInSpanToRule1);
				Node methodInRule1 = mappingMethodInSpanToRule1.getImage();
				assertNotNull(methodInRule1);
				assertEquals("Method", methodInRule1.getType().getName());
				Mapping classMappingInSpanToRule1 = extendedSpanTypeEdge.getMappingInRule1(classNodeInSpan);
				assertNotNull(classMappingInSpanToRule1);
				Node classInRule1 = classMappingInSpanToRule1.getImage();
				assertNotNull(classInRule1);
				assertEquals("Class", classInRule1.getType().getName());
				
				// In Rule2: soure: 11:Class, target 13:Method
				assertEquals(2, extendedSpanTypeEdge.getMappingsInRule2().size());
				Mapping mappingMethod_InSpanToRule2 = extendedSpanTypeEdge.getMappingInRule2(methodNodeInSpan);
				assertNotNull(mappingMethod_InSpanToRule2);
				Node methodInRule2 = mappingMethod_InSpanToRule2.getImage();
				assertNotNull(methodInRule2);
				assertEquals("Method", methodInRule2.getType().getName());
				Mapping mappingClassInSpanToRule2 = extendedSpanTypeEdge.getMappingInRule2(classNodeInSpan);
				assertNotNull(mappingClassInSpanToRule2);
				Node classInRule2 = mappingClassInSpanToRule2.getImage();
				assertNotNull(classInRule2);
				assertEquals("Class", classInRule2.getType().getName());
			}
		}
	}
}
