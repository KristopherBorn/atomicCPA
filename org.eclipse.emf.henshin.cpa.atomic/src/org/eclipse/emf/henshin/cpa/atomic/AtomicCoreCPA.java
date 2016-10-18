package org.eclipse.emf.henshin.cpa.atomic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.PushoutResult;
import org.eclipse.emf.henshin.cpa.atomic.AtomicCoreCPA.Span;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Match;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.model.Action;
import org.eclipse.emf.henshin.model.Edge;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.HenshinFactory;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.ModelElement;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.impl.EdgeImpl;
import org.eclipse.emf.henshin.model.impl.GraphImpl;
import org.eclipse.emf.henshin.model.impl.HenshinFactoryImpl;
import org.eclipse.emf.henshin.model.impl.MappingImpl;
import org.eclipse.emf.henshin.model.impl.NodeImpl;

public class AtomicCoreCPA {
	

	HenshinFactory henshinFactory = new HenshinFactoryImpl();

	public List<ConflictAtom> computeConflictAtoms(Rule rule1, Rule rule2){
		List<ConflictAtom> result = new LinkedList<ConflictAtom>();
		List<Span> candidates = computeCandidates(rule1, rule2);
		for(Span candidate : candidates){
			Set<Span> reasons = new HashSet<>();//
			computeMinReasons(rule1, rule2, candidate, reasons);
			if(!reasons.isEmpty()){
				result.add(new ConflictAtom(candidate, reasons));
				//TODO: wieso ein Atom die "reasons" benötigt ist mir noch unklar.
				// Bzw.was die Datenstruktur "Atom" überhaupt umfasst.
			}
		}
		return result;
	}
	
	
//	- elemente sammeln
//	- alle deletion-nodes
//	- alle deletion-edges mit zwei preserve-nodes
//		- vorkommen der elemente in der LHS von R2
//		- erstellen eines Graph (S1) und der Mappings in rule1 und rule2
//	Wie programmatisch instanzen des jeweiligen MM erstellen?
//	Henshins "MappingImpl" Klasse wirklich geeignet? Oder eher MatchImpl?
	public List<Span> computeCandidates(Rule rule1, Rule rule2) {
		List<Span> result = new LinkedList<Span>();
		Action deleteAction = new Action(Action.Type.DELETE);

		//TODO: extract to Method!
		List<ModelElement> atomicDeletionElements = new LinkedList<ModelElement>(rule1.getActionNodes(deleteAction));
		for(Edge deletionEdge : rule1.getActionEdges(deleteAction)){
			Action sourceNodeAction = deletionEdge.getSource().getAction();
			Action targetNodeAction = deletionEdge.getTarget().getAction();
			if(sourceNodeAction.getType().equals(Action.Type.PRESERVE) && targetNodeAction.getType().equals(Action.Type.PRESERVE)){
				//TODO: additional "deletion edge check" due to some unresolved Bug. Edges are loaded with different URIs for their type.
				Edge image = rule1.getMappings().getImage(deletionEdge, rule1.getRhs());
				Node sourceNodeLhs = deletionEdge.getSource();
				Node targetNodeLhs = deletionEdge.getTarget();
				Node sourceNodeRhs = rule1.getMappings().getImage(sourceNodeLhs, rule1.getRhs());
				Node targetNodeRhs = rule1.getMappings().getImage(targetNodeLhs, rule1.getRhs());
				EList<Edge> allOutgoing = sourceNodeRhs.getOutgoing();
				URI uriOfDeletionEdgeType = EcoreUtil.getURI(deletionEdge.getType());
				boolean isHoweverAPreserveEdge = false;
				for(Edge edge : allOutgoing){
					if(edge.getTarget() == targetNodeRhs){
						// check same name of URI:
						URI uriOfPotentialAssociatedEdgeType = EcoreUtil.getURI(edge.getType());
						if(uriOfDeletionEdgeType.toString().equals(uriOfPotentialAssociatedEdgeType.toString()))
							isHoweverAPreserveEdge = true;
					}
				}
//				allOutgoing.get(0).getType().eCrossReferences() getEGenericType().equals(allOutgoing.get(1).getType());
//				EList<Edge> outgoingWithType = sourceNodeRhs.getOutgoing(deletionEdge.getType());
//				URI outgoing0_uri = EcoreUtil.getURI(allOutgoing.get(0).getType());outgoing0_uri.toString()
//				URI outgoing1_uri = EcoreUtil.getURI(allOutgoing.get(1).getType());
//				URI deleteEdge_uri = EcoreUtil.getURI(deletionEdge.getType());
//				System.out.println("HALT");
				if(!isHoweverAPreserveEdge)
					atomicDeletionElements.add(deletionEdge);
			}
		}
		for(ModelElement el1 : atomicDeletionElements){
			List<ModelElement> atomicUsageElements = new LinkedList<ModelElement>();
			if(el1 instanceof Node){
				atomicUsageElements.addAll(rule2.getLhs().getNodes(((Node) el1).getType()));
//				EList<Node> nodes = rule2.getLhs().getNodes(((Node) el1).getType());
			}
			if(el1 instanceof Edge){
				atomicUsageElements.addAll(rule2.getLhs().getEdges(((Edge) el1).getType()));
			}
			for(ModelElement el2 : atomicUsageElements){
				if(el2 instanceof Node){
					Node el2Node = (Node) el2;
					
					
					//TODO: noch ist nicht ersichtlich, ob oder warum für S1wirklich ein Graph  notwendig ist, 
					//		oder ob nicht ein Mapping zwischen zwischen den Knoten der beiden LHS ausreicht.
					Graph S1 = henshinFactory.createGraph();
					Node commonNode = henshinFactory.createNode(S1, el2Node.getType(), ""); 
					
					//TODO: maybe add a type check for el1, but since the associated el2 is here a node, el1 should be also.
					Mapping nodeInRule1Mapping = henshinFactory.createMapping(commonNode, (Node)el1);
					//TODO: maybe add a type check for el2, but there had been allready one at the beginning by the if-statement
					Mapping nodeInRule2Mapping = henshinFactory.createMapping(commonNode, (Node) el2);
					
					Span S1span = new Span(nodeInRule1Mapping, S1, nodeInRule2Mapping);
					result.add(S1span);
					
//					EClass type = ((Node) el2).getType();
//					EPackage singleEPackageOfDomainModel = type.getEPackage();
//					EFactory eFactoryInstance = singleEPackageOfDomainModel.getEFactoryInstance();
//					
//					EObject create = eFactoryInstance.create(type);
//					el2.eResolveProxy
				}
				if(el2 instanceof Edge){
					Graph S1 = henshinFactory.createGraph();
					
					List<Mapping> rule1Mappings = new LinkedList<Mapping>();
					List<Mapping> rule2Mappings = new LinkedList<Mapping>();
					
					Node sourceNodeInRule1 = ((Edge) el1).getSource();
					Node sourceNodeInRule2 = ((Edge) el2).getSource();
					Node commonSourceNode = henshinFactory.createNode(S1, sourceNodeInRule1.getType(), ""); 
					Mapping sourceNodeInRule1Mapping = henshinFactory.createMapping(commonSourceNode, sourceNodeInRule1);
					rule1Mappings.add(sourceNodeInRule1Mapping);
					Mapping sourceNodeInRule2Mapping = henshinFactory.createMapping(commonSourceNode, sourceNodeInRule2);
					rule2Mappings.add(sourceNodeInRule2Mapping);

					Node targetNodeInRule1 = ((Edge) el1).getTarget();
					Node targetNodeInRule2 = ((Edge) el2).getTarget();
					Node commonTargetNode = henshinFactory.createNode(S1, targetNodeInRule1.getType(), ""); 
					Mapping targetNodeInRule1Mapping = henshinFactory.createMapping(commonSourceNode, targetNodeInRule1);
					rule1Mappings.add(targetNodeInRule1Mapping);
					Mapping targetNodeInRule2Mapping = henshinFactory.createMapping(commonSourceNode, targetNodeInRule2);
					rule2Mappings.add(targetNodeInRule2Mapping);
					
					henshinFactory.createEdge(commonSourceNode, commonTargetNode, ((Edge) el2).getType());
					
					Span S1span = new Span(rule1Mappings, S1, rule2Mappings);
					result.add(S1span);
				}
			}
		}
		return result;
	}
	
	public void computeMinReasons(Rule rule1, Rule rule2, Span s1, Set<Span> reasons) {
		if(isMinReason(rule1, rule2, s1)){
			reasons.add(s1);
			return;
		}
		// is this part of the backtracking?
		Set<Span> extendedSpans = findExtensions(rule1, rule2, s1, reasons);
		for(Span extendedSpan : extendedSpans){
			computeMinReasons(rule1, rule2, extendedSpan, reasons);
		}		
	}


	private boolean isMinReason(Rule rule1, Rule rule2, Span s1) {
		// TODO: ist hier der Zusammenhang zwischen C_1 und L_1, also c_1 klar?
		PushoutResult pushoutResult = constructPushout(rule1, rule2, s1); 
		//TODO: wofür wird G benötigt? Vermutlich nur als Ziel der matches, oder?
		// 		Oder ist das nicht normalerweise das minimale Modell?
		boolean isMatchM1 = findDanglingEdges(rule1, pushoutResult.getMappingsOfRule1()).isEmpty(); //TODO: über den jeweiligen match sollte doch die Regel auch "erreichbar" sein. Regel als Parameter daher überflüssig.
		boolean isMatchM2 = findDanglingEdges(rule2, pushoutResult.getMappingsOfRule2()).isEmpty();
		return (isMatchM1 && isMatchM2);
	}

	// TODO: bisher nicht weiter spezifiziert!
	/**
	 * Idee: 
	 *  Im prinziep 
	 * @param rule1
	 * @param rule2
	 * @param s1
	 * @return
	 */
	private PushoutResult constructPushout(Rule rule1, Rule rule2, Span s1) {
		PushoutResult pushoutResult = new PushoutResult(rule1, s1, rule2);
		// TODO einfach nur ein Aufruf des PushoutResult Konstruktor? 
//		Da es mutmaßlich nur ein 
//			- kopieren der beiden linken Seiten
//				- dabei aufbauen der mappings
//			- entfernen der "doppelten" Knoten und Kanten aus L2
//			- zusammenfassen der Mengen 
//			ist, kann dies alles auch im Konstruktor ablaufen!
		return pushoutResult;
	}

	private Set<Span> findExtensions(Rule rule1, Rule rule2, Span s1, Set<Span> reasons) {
		// TODO: bei "isMinReason" wird ebensfalls der Pushout gebildet. 
		// Was ist hier der Unterschied, bzw. es sollte vermieden werden, 
		// dass zweimal die gleichen Pushouts gebildet werden/existieren.
		PushoutResult pushoutResult = constructPushout(rule1, rule2, s1); 
		List<Edge> danglingEdges = new LinkedList<>();
		//TODO: wo sollen hier die matches (match1 und match2) herkommen?
		danglingEdges.addAll(findDanglingEdges(rule1, pushoutResult.getMappingsOfRule1()));
		//TODO: anstelle einer Neuberechnung sollten hier die ge-cachten Ergebnisse der vorherigen Berechnung verwendet werden!
		danglingEdges.addAll(findDanglingEdges(rule2, pushoutResult.getMappingsOfRule2()));
		//TODO: anstelle einer Neuberechnung sollten hier die ge-cachten Ergebnisse der vorherigen Berechnung verwendet werden!
		
		
		List<Edge> fixingEdges = new LinkedList<>();
		for(Edge danglingEdge : danglingEdges){
			//Frage: wofür steht das "e"? Welche Bedeutung hat das?
			//antwort:	vermutlich für die Kante "e", die hier als danglingEdge behoben werden soll.
			//	Frage: handelt es sich bei den "e"s um die zwischen S'_1 und S_1 wie in Definition3?
			// 	antwort: vermutlich nicht, denn dort scheint ein komplexerer morphismus gemeint zu sein zwischen zwei Graphen.
			//			 hier ist vermutlich nur einfach nur eine der dangling edges gemeint. 
			//TODO: Ist hier nicht der overlapGraph "G" notwendig? - 
			// antwort: dieser ist leicht über die dangling edge emit "getGraph()" zu erhalten.
			//TODO: "fixingEdges" müssen ja irgendwie aus der Menge der Kanten der beiden Regeln stammen 
			// 		und vom Typ her zur danglingEdge passen (bzw. einer der SuperTypen sein)
			// TODO: Aber irgendwie wird doch noch der morphismus(/match) zwischen den Regeln und der dangling edge gebraucht, oder?
			// antwort: noch ungeklärt!!!!
			List<Edge> fixingEdges_e = findFixingEdges(rule1, rule2, s1, danglingEdge, pushoutResult.getMappingsOfRule1(), pushoutResult.getMappingsOfRule2());
			if(!fixingEdges_e.isEmpty()){
				fixingEdges.addAll(fixingEdges_e);
			}
			else{
				// Frage: wird keine Lösung für eine dangling edge gefunden, 
				//		so soll die Suche nach Erweiterungen abgebrochen werden?
				//		Überlegung: was ist überhaupt das Ziel? -> Wenn dangling-edges auftreten,
				//			dann ist es nur eine extension wenn es für diese auch Lösungen gibt.
				return new HashSet<Span>();// oder NULL?
			}
			// Frage: Was genau ist das, die disjointCombinations?
		}		
		Set<Span> disjointCombinations = enumerateDisjointCombinations(s1, fixingEdges);
		return disjointCombinations;
	}


	/* Grundidee: die dangling edge hat als einen ihrer beiden Knoten definitiv einen Knoten aus dem Graph S1 des Spans!
	 * Aus dem Papier: "A naïve implementation of this function may enumerate all adjacent edges 
	 * 					in L1 OHNE S1 of e's context node in S1"
	 * 	Optimierung aus dem Papier: nur "löschende Kanten" von L_1 berücksichtigen, da der Knoten auch bereits löschend war. 
	 * 								(andernfalls wäre es ja nicht zur dangling edge gekommen.)
	 * Sollte es keine fixing edges geben wird eine leere Menge zurückgegeben.
	 * Ansonsten kann es antürlich mehrere fixing edges geben. Diese werden ALLE zurückgegeben!
	 * 
	 */
	//TODO: höchstwahrscheinlich werden noch als zusätzliche Übergabeparameter die Mappings m_1 und m_2 benötigt
		// --> Algo anpassen! (ansonsten kann der zugehörige knoten zur dangling edge nicht eindeutig in S1 bestimmt werden.)
	//TODO: die zweite Optimierung haeb ich noch nciht verstanden!
	//TODO: mwenn (mit Daniel?) geklärt ist, dass die matches notwendig sind, dann könnte man überlegen die beiden Listen von mapping edges 
	//		und zusätzlich auch noch den Span s1 durch das "pushoutResult" zu ersetzen, da dieses alle drei kennen könnte(sollte?)
	//		Alternativ wird das hinfällig wenn eine(/mehrere) zentrale Instanz(en) die MAppings verwaltet.(Stichwort "MappingHandler") 
	public List<Edge> findFixingEdges(Rule rule1, Rule rule2, Span s1, Edge danglingEdge, List<Mapping> mappingOfRule1InOverlapG, List<Mapping> mappingOfRule2InOverlapG) {
		List<Edge> fixingEdges = new LinkedList<Edge>();
		//TODO: identifizieren des Knotens der dangling edge, der bereits in s1 enthalten ist.
//		Dies ist der Knoten, für den es ein Mapping in beide REgeln gibt. (Für den anderen Knoten der KAnte wird dies nciht der falls ein. Gäbe es für beide Knoten ein Mapping in beide Regeln )
//		Node
		HashMap<Node, Mapping> mappingsOfImageNodesFromRule1InOverlapG = new HashMap<Node, Mapping>();	
		for(Mapping mapping : mappingOfRule1InOverlapG){
			Node imageNodeOfMapping = mapping.getImage();
			mappingsOfImageNodesFromRule1InOverlapG.put(imageNodeOfMapping, mapping);
		}
		// ERkenntnis/Idee: source und target Knoten eines Mappings sind immer festgelegt auf eine rolle. 
		// Ein source Knoten kann nie innerhalb dieserMappingbeziehung zum target Knoten werden und umgekehrt
		// -> Idee: Es werden nicht zwie HashMaps benötigt für eine Menge von Mappings, sondern nur eine in der jedes Mapping als value zweimal vorkommt.
		//			Einmal mit sienem origin als key und einmal mit seinem image als key.
		// WICHTIG: da noch nicht ersichtlich ist wie der Hash der Klasse Node berechnet wird sollte beim Hinzufügen jedes Knoten in die HashMap im Rahmen ihrer Erstellung geprüft werden, 
		//			dass noch kein Eintrag mit diesem Schlüssel vorhanden ist. 
		//			(Falls doch, so ist zu überlegen, ob die hashCode() Funktion von Node für gen Einsatz in einer HashMap geeignet ist.
		//TODO: ggf. zentralen "MappingHandler" anlegen.
		// 
		HashMap<Node, Mapping> mappingsOfImageNodesFromRule2InOverlapG = new HashMap<Node, Mapping>();	
		for(Mapping mapping : mappingOfRule2InOverlapG){
			Node imageNodeOfMapping = mapping.getImage();
			mappingsOfImageNodesFromRule2InOverlapG.put(imageNodeOfMapping, mapping);
		}
		Node sourceNodeOfDanglingEdgeInOverlapG = danglingEdge.getSource();
		Node targetNodeOfDanglingEdgeInOverlapG = danglingEdge.getTarget();
		
		//VORSICHT! vermutlich NPE!!!
		//Lösung: zweistufiges vorgehen: erst Mapping holen und nur wenn dieses != null ist darauf zugreifen!
		Node sourceNodeInRule1 = null;
		Mapping mappingOfSourceNodeFromRule1ToOverlapG = mappingsOfImageNodesFromRule1InOverlapG.get(sourceNodeOfDanglingEdgeInOverlapG);
		if(mappingOfSourceNodeFromRule1ToOverlapG != null){
			sourceNodeInRule1 = mappingOfSourceNodeFromRule1ToOverlapG.getOrigin();
		}
		Node sourceNodeInRule2 = null;
		Mapping mappingOfSourceNodeFromRule2ToOverlapG = mappingsOfImageNodesFromRule2InOverlapG.get(sourceNodeOfDanglingEdgeInOverlapG);
		if(mappingOfSourceNodeFromRule2ToOverlapG != null){
			sourceNodeInRule2 = mappingOfSourceNodeFromRule2ToOverlapG.getOrigin();
		}
		Node targetNodeInRule1 = null;
		Mapping mappingOfTargetNodeFromRule1ToOverlapG = mappingsOfImageNodesFromRule1InOverlapG.get(targetNodeOfDanglingEdgeInOverlapG);
		if(mappingOfTargetNodeFromRule1ToOverlapG != null){
			targetNodeInRule1 = mappingOfTargetNodeFromRule1ToOverlapG.getOrigin();
		}
		Node targetNodeInRule2 = null;
		Mapping mappingOfTargetNodeFromRule2ToOverlapG = mappingsOfImageNodesFromRule2InOverlapG.get(targetNodeOfDanglingEdgeInOverlapG);
		if(mappingOfTargetNodeFromRule2ToOverlapG != null){
			targetNodeInRule2 = mappingOfTargetNodeFromRule2ToOverlapG.getOrigin();
		}
		// WICHTIG: meistens sind nur drei der vier Knoten vorhanden.
		
		//Information zur Verknüpfung der Elemente im Graph des Span und der Regelknoten sind in den Mappings di eTeil des Spans sind!
		Node sourceNodeInGraphOfSpanByRule1 = null;
		if(sourceNodeInRule1 != null){
			Mapping mappingOfSourceNodeFromGraphToRule1 = s1.getMappingFromGraphToRule1(sourceNodeInRule1); //TODO: kann das ergebnis null sein? insert NPE check?
			sourceNodeInGraphOfSpanByRule1 = (mappingOfSourceNodeFromGraphToRule1 != null) ? mappingOfSourceNodeFromGraphToRule1.getOrigin() : null;
		}
		Node targetNodeInGraphOfSpanByRule1 = null;
		if(targetNodeInRule1 != null){			
			Mapping mappingOfTargetNodeFromGraphToRule1 = s1.getMappingFromGraphToRule1(targetNodeInRule1);
			targetNodeInGraphOfSpanByRule1 = (mappingOfTargetNodeFromGraphToRule1 != null) ? mappingOfTargetNodeFromGraphToRule1.getOrigin() : null;
		}
		
		Node sourceNodeInGraphOfSpanByRule2 = null;
		if(sourceNodeInRule2 != null){			
			Mapping mappingOfSourceNodeFromGraphToRule2 = s1.getMappingFromGraphToRule2(sourceNodeInRule2);
			sourceNodeInGraphOfSpanByRule2 = (mappingOfSourceNodeFromGraphToRule2 != null) ? mappingOfSourceNodeFromGraphToRule2.getOrigin() : null;
		}
		Node targetNodeInGraphOfSpanByRule2 = null;
		if(targetNodeInRule2 != null){
			Mapping mappingOfTargetNodeFromGraphToRule2 = s1.getMappingFromGraphToRule2(targetNodeInRule2);
			targetNodeInGraphOfSpanByRule2 = (mappingOfTargetNodeFromGraphToRule2 != null) ? mappingOfTargetNodeFromGraphToRule2.getOrigin() : null;			
		}
		
		// generell sollten die beiden sourceNodeInGraphOfSpan (und die beiden targetNodeInGraphOfSpan jeweils) die selben sein.
		
		// In der Regel wird nur eins der beiden Paare erreicht und beim anderen Paar gibt es keins der beiden Mappings.
		// Ist dennoch eines der beiden Mappings vorhanden liegt vermutlich ein Fehler vor
		// Sind beide Paare vollständig vorhanden handelt es sich um einen Sonderfall in dem aber unabhängig voneinander die 
		// potentiellen fixingEdges ermittelt werden können.
		
		//TODO: in Methode auslagern, da zweimal nahezu identischer code??? Oder doch zu viele unterschiede und zu verwirrend?
		if(sourceNodeInGraphOfSpanByRule1 != null && sourceNodeInGraphOfSpanByRule2 != null){
			Node sourceNodeToFixOutgoingEdge = sourceNodeInGraphOfSpanByRule1;
			// - über alle ausgehenden Kanten in der Regel1 drübergehen und prüfen ob diese vom korrekten Typ sind und löschend sind
			// if(yes): zu fixing edges hinzufügen
			EList<Edge> outgoingEdgesOfSourceNodeInRule1 = sourceNodeInRule1.getOutgoing(danglingEdge.getType());
			//TODO: check here, that we really have a node of the LHS of rule1 - otherwise there had been an error!
			for(Edge outgoingEdgeOfSourceNodeInRule1 : outgoingEdgesOfSourceNodeInRule1){
				if(outgoingEdgeOfSourceNodeInRule1.getAction().getType().equals(Action.Type.DELETE))
					fixingEdges.add(outgoingEdgeOfSourceNodeInRule1);
			}
		}
		
		if(targetNodeInGraphOfSpanByRule1 != null && targetNodeInGraphOfSpanByRule2 != null){
			Node targetNodeToFixOutgoingEdge = targetNodeInGraphOfSpanByRule1;
			// - über alle eingehenden Kanten in der Regel1 drübergehen und prüfen ob diese vom korrekten Typ sind und löschend sind
			// if(yes): zu fixing edges hinzufügen
			EList<Edge> incomingEdgesOfTargetNodeInRule1 = targetNodeInRule1.getIncoming(danglingEdge.getType());
			//TODO: check here, that we really have a node of the LHS of rule1 - otherwise there had been an error!
			for(Edge incomingEdgeOfTargetNodeInRule1 : incomingEdgesOfTargetNodeInRule1){
				if(incomingEdgeOfTargetNodeInRule1.getAction().getType().equals(Action.Type.DELETE))
					fixingEdges.add(incomingEdgeOfTargetNodeInRule1);
			}
		}
		
		return fixingEdges;
	}


	// TODO: bisher nicht weiter spezifiziert!
	public Set<Span> enumerateDisjointCombinations(Span s1, List<Edge> fixingEdges) {
		Set<Span> disjointCombinations = new HashSet<Span>(); //LinkedList<Span>();
//		Für jede Kante in fixingEdges wird ein neuer Span erzeugt und dieser um die jeweilige Kante vergrößert.
		for(Edge fixingEdge : fixingEdges){
//			Dabei müssen auch entsprechend neue Mappings erzeugt werden!
//			TODO: die Kopie für dne neuen Span muss zuerst erstellt werden und die neuen Knotne und KAnten in der Kopie erstellt werden, 
//					sowie die neuen Mappings der Kopie hinzugefügt werden!					
			Span newSpan = new Span(s1);
			//TODO: prüfen, dass die Art des erstellens einer Kopie korrekt ist. 
//			ToDo: (/Fehler!) zur Erweiterung des Span um eine Kante der Regel 1 kann es mehrere passende Kanten der Regel 2 geben. 
//						-> eine weitere Schleife ist notwendig!
			Node newNodeInGraph = null;
			Node alreadyExistingNodeInGraph = null;
			Node sourceNodeInGraph = null;
			Node targetNodeInGraph = null;			
			Node sourceNodeOfFixingEdgeInRule1 = fixingEdge.getSource();
			Mapping mappingOfsourceNodeOfFixingEdgeInRule1 = newSpan.getMappingFromGraphToRule1(sourceNodeOfFixingEdgeInRule1);
			// wenn NULL - erstellen von Knoten und Kante in graph, und mapping
			if(mappingOfsourceNodeOfFixingEdgeInRule1 == null){
				// Knoten in graph von Span erstellen
				newNodeInGraph = henshinFactory.createNode(newSpan.getGraph(), sourceNodeOfFixingEdgeInRule1.getType(), sourceNodeOfFixingEdgeInRule1.getName()+"_");
				sourceNodeInGraph = newNodeInGraph;
				//TODO: Mapping in den Span hinzufügen!
				Mapping newSourceNodeMapping = henshinFactory.createMapping(newNodeInGraph, sourceNodeOfFixingEdgeInRule1);
				newSpan.mappingsInRule1.add(newSourceNodeMapping);
			}
			else{
				sourceNodeInGraph = mappingOfsourceNodeOfFixingEdgeInRule1.getOrigin();
				alreadyExistingNodeInGraph = sourceNodeInGraph;
			}
			Node targetNodeOfFixingEdgeInRule1 = fixingEdge.getTarget();
			Mapping mappingOftargetNodeOfFixingEdgeInRule2 = newSpan.getMappingFromGraphToRule1(targetNodeOfFixingEdgeInRule1);
			// wenn NULL - erstellen von Knoten und Kante in graph, und mapping
			if(mappingOftargetNodeOfFixingEdgeInRule2 == null){
				// Knoten in graph von Span erstellen
				newNodeInGraph = henshinFactory.createNode(newSpan.getGraph(), targetNodeOfFixingEdgeInRule1.getType(), targetNodeOfFixingEdgeInRule1.getName()+"_");
				targetNodeInGraph = newNodeInGraph;
				//TODO: Mapping in den Span hinzufügen!
				Mapping newSourceNodeMapping = henshinFactory.createMapping(newNodeInGraph, targetNodeOfFixingEdgeInRule1);
				newSpan.mappingsInRule1.add(newSourceNodeMapping);
			}
			else{
				targetNodeInGraph = mappingOftargetNodeOfFixingEdgeInRule2.getOrigin();
				alreadyExistingNodeInGraph = targetNodeInGraph;
			}
			// create corresponding edge of fixingEdge in graph of span.
			Edge fixingEdgeInGraphOfSpan = henshinFactory.createEdge(sourceNodeInGraph, targetNodeInGraph, fixingEdge.getType());
			
			//find all potential "use" edges in Rule2
				// find sourceNode or TargetNode in Rule2
//			List<Edge> potentialUsageEdgesOfFixingEdgeInRule2 = null; // TODO: seems to be superfluous right now!
			List<Mapping> mappingsOfUsageEdgesInRule2 = new LinkedList<Mapping>();
			Mapping alreadyExistingMappingInRule2 = newSpan.getMappingInRule2(alreadyExistingNodeInGraph);
			if(alreadyExistingMappingInRule2.getOrigin().getOutgoing().contains(fixingEdgeInGraphOfSpan)){
//				dann werden zum Knoten in Regel2 die ausgehenden Kanten nach potentiellen "use" Kanten durchsucht.
//				dabei sollte immer mindestens eine zu finden sein, sonst wäre es gar nicht zur dangling edge gekommen.
				EList<Edge> potentialUsageEdgesOfFixingEdgeInRule2EList = alreadyExistingMappingInRule2.getImage().getOutgoing(fixingEdgeInGraphOfSpan.getType());
//				potentialUsageEdgesOfFixingEdgeInRule2.addAll(potentialUsageEdgesOfFixingEdgeInRule2EList); // TODO: seems to be superfluous right now!
				//anstelle nur der usageEdges zu ermitteln am besten auch gleich Knoten und letztlich mapping ermitteln/erzeugen
					// dann aber auch gleich ggf den neuen Span erzeugen!
				
//				Im Fall nur einer potentiell nutzenden Kante reicht es aus den "newSpan", der bereits ein Kopie des ursprünglichen ist, zu erweitern.
//				andernfalls müssen weitere Kopien erzeugt werden.
				if(potentialUsageEdgesOfFixingEdgeInRule2EList.size() == 0){
					//irgendwas ist schief gelaufen. Es wurde festgestellt, dass eine fixing edge benötigt wird, aber keine passende fixing edge gefunden.
				}
				else if(potentialUsageEdgesOfFixingEdgeInRule2EList.size() ==1){
					//es gibt nur eine fixing edge, "newSpan" kann dafür verwendet werden
					//build new mapping for fixing edge
					// herausfinden welcher der Knoten in Rule2 bereits durch ein mapping abgedeckt ist
						// an dieser Stelle im code immer der source Knoten
					Edge fixingEdgeInRule2 = potentialUsageEdgesOfFixingEdgeInRule2EList.get(0);
//					if(fixingEdge.)
						// origin Knoten des mappings sollte schon durch die Verarbeitung von Regel 1 erzeugt worden sein.
					Mapping newMappingForFixingEdgeinRule2 = henshinFactory.createMapping(newNodeInGraph, fixingEdgeInRule2.getTarget());
					newSpan.mappingsInRule2.add(newMappingForFixingEdgeinRule2);
//						if(disjointCombinations.contains(newSpan)) //TODO: "Messung" bezüglich equals(..)-Methode wieder entfernen.
//							System.err.println("newSpan is equal to allready existing one");
						int sizeBefore = disjointCombinations.size();
					disjointCombinations.add(newSpan);
//						System.err.println("number of disjoint spans - before: "+sizeBefore +" after: "+disjointCombinations.size()); //TODO: "Messung" bezüglich equals(..)-Methode wieder entfernen.
				}
				// mehr als eine fixing edge vorhanden. Für jede muss ein neuer Span angelegt werden.				
				else{
					for(Edge potentialUsageEdgeOfFixingEdgeInRule2 : potentialUsageEdgesOfFixingEdgeInRule2EList){
						Span newSpanPerUsageEdge = new Span(newSpan);
						Mapping targetNodeOfFixingEdgeMapping = newSpanPerUsageEdge.getMappingFromGraphToRule1(targetNodeOfFixingEdgeInRule1);
						// "newNodeInGraph"  muss nach dem Kopieren von newSpan ermittelt werden!
						Node newNodeDueToTargetNodeInGraph = targetNodeOfFixingEdgeMapping.getOrigin();
						Edge fixingEdgeInRule2 = potentialUsageEdgeOfFixingEdgeInRule2;
						Mapping newMappingForFixingEdgeinRule2 = henshinFactory.createMapping(newNodeDueToTargetNodeInGraph, fixingEdgeInRule2.getTarget());
						newSpanPerUsageEdge.mappingsInRule2.add(newMappingForFixingEdgeinRule2);
//							if(disjointCombinations.contains(newSpan)) //TODO: "Messung" bezüglich equals(..)-Methode wieder entfernen.
//								System.err.println("newSpan is equal to allready existing one");
							int sizeBefore = disjointCombinations.size();
						disjointCombinations.add(newSpanPerUsageEdge);
//							System.err.println("number of disjoint spans - before: "+sizeBefore +" after: "+disjointCombinations.size()); //TODO: "Messung" bezüglich equals(..)-Methode wieder entfernen.
					}
				}
			}
			if(alreadyExistingMappingInRule2.getOrigin().getIncoming().contains(fixingEdgeInGraphOfSpan)){
//				dann werden zum Knoten in Regel2 die eingehenden Kanten nach potentiellen "use" Kanten durchsucht.
//				dabei sollte immer mindestens eine zu finden sein, sonst wäre es gar nicht zur dangling edge gekommen.
				EList<Edge> potentialUsageEdgesOfFixingEdgeInRule2EList = alreadyExistingMappingInRule2.getImage().getIncoming(fixingEdgeInGraphOfSpan.getType());
//				potentialUsageEdgesOfFixingEdgeInRule2.addAll(potentialUsageEdgesOfFixingEdgeInRule2);
				//anstelle nur der usageEdges zu ermitteln am besten auch gleich Knoten und letztlich mapping ermitteln/erzeugen
					//dann aber auch gleich ggf. den neuen Span erzeugen

//				Im Fall nur einer potentiell nutzenden Kante reicht es aus den "newSpan", der bereits ein Kopie des ursprünglichen ist, zu erweitern.
//				andernfalls müssen weitere Kopien erzeugt werden.
				if(potentialUsageEdgesOfFixingEdgeInRule2EList.size() == 0){
					//irgendwas ist schief gelaufen. Es wurde festgestellt, dass eine fixing edge benötigt wird, aber keine passende fixing edge gefunden.
				}
				else if(potentialUsageEdgesOfFixingEdgeInRule2EList.size() ==1){
					//es gibt nur eine fixing edge, "newSpan" kann dafür verwendet werden
					//build new mapping for fixing edge
					// herausfinden welcher der Knoten in Rule2 bereits durch ein mapping abgedeckt ist
						// an dieser Stelle im code immer der source Knoten
					Edge fixingEdgeInRule2 = potentialUsageEdgesOfFixingEdgeInRule2EList.get(0);
//					if(fixingEdge.)
						// origin Knoten des mappings sollte schon durch die Verarbeitung von Regel 1 erzeugt worden sein.
					Mapping newMappingForFixingEdgeinRule2 = henshinFactory.createMapping(newNodeInGraph, fixingEdgeInRule2.getSource());
					newSpan.mappingsInRule2.add(newMappingForFixingEdgeinRule2);
						if(disjointCombinations.contains(newSpan)) //TODO: "Messung" bezüglich equals(..)-Methode wieder entfernen.
							System.err.println("newSpan is equal to allready existing one");
						int sizeBefore = disjointCombinations.size();
					disjointCombinations.add(newSpan);
						System.err.println("number of disjoint spans - before: "+sizeBefore +" after: "+disjointCombinations.size()); //TODO: "Messung" bezüglich equals(..)-Methode wieder entfernen.
				}
				// mehr als eine fixing edge vorhanden. Für jede muss ein neuer Span angelegt werden.				
				else{
					for(Edge potentialUsageEdgeOfFixingEdgeInRule2 : potentialUsageEdgesOfFixingEdgeInRule2EList){
						Span newSpanPerUsageEdge = new Span(newSpan);
						Mapping sourceNodeOfFixingEdgeMapping = newSpanPerUsageEdge.getMappingFromGraphToRule1(sourceNodeOfFixingEdgeInRule1);
						// "newNodeInGraph"  muss nach dem Kopieren von newSpan ermittelt werden!
						Node newNodeDueToSourceNodeInGraph = sourceNodeOfFixingEdgeMapping.getOrigin();
						Edge fixingEdgeInRule2 = potentialUsageEdgeOfFixingEdgeInRule2;
						Mapping newMappingForFixingEdgeinRule2 = henshinFactory.createMapping(newNodeDueToSourceNodeInGraph, fixingEdgeInRule2.getSource());
						newSpanPerUsageEdge.mappingsInRule2.add(newMappingForFixingEdgeinRule2);
							if(disjointCombinations.contains(newSpan)) //TODO: "Messung" bezüglich equals(..)-Methode wieder entfernen.
								System.err.println("newSpan is equal to allready existing one");
							int sizeBefore = disjointCombinations.size();
						disjointCombinations.add(newSpanPerUsageEdge);
							System.err.println("number of disjoint spans - before: "+sizeBefore +" after: "+disjointCombinations.size()); //TODO: "Messung" bezüglich equals(..)-Methode wieder entfernen.
					}
				}
			}
			
			// das scheinen alles noch Überreste der Entwicklung zu sein.
//			//TODO: create mappingS of potential "use"-Edges
//			
//			
////				Im Fall nur einer potentiell nutzenden Kante reicht es aus den "newSpan", der bereits ein Kopie des ursprünglichen ist, zu erweitern.
////				andernfalls müssen weitere Kopien erzeugt werden.
//			if(potentialUsageEdgesOfFixingEdgeInRule2.size() == 0){
//				//irgendwas ist schief gelaufen. Es wurde festgestellt, dass eine fixing edge benötigt wird, aber keine passende fixing edge gefunden.
//			}
//			else if(potentialUsageEdgesOfFixingEdgeInRule2.size() ==1){
//				//es gibt nur eine fixing edge, "newSpan" kann dafür verwendet werden
//				//build new mapping for fixing edge
//				// herausfinden welcher der Knoten in Rule2 bereits durch ein mapping abgedeckt ist und zu welchem Knoten noch ein mapping erstellt werden muss
//				Edge fixingEdgeInRule2 = potentialUsageEdgesOfFixingEdgeInRule2.get(0);
//				if(fixingEdge.)
//				
//				alreadyExistingMappingInRule2.getImage()
//				newSpan.expand(fixingEdge);
//				disjointCombinations.add(newSpan);
//			}
//			else{
//				// mehr als eine fixing edge vorhanden. Für jede muss ein neuer Span angelegt werden.
//				TODO: an dieser Stelle muss neben der Kante der Regel1 (fixingEdge) auch die Kante der Regel2 und der Knoten übergeben werden!
//				Oder gleich die fertigen mappings!
//				for(Edge fixingEdgeInRule2 : potentialUsageEdgesOfFixingEdgeInRule2){		
//				}					
//			}
		}
		return disjointCombinations;
	}

	// TODO: bisher nicht weiter spezifiziert!
	//		Funktionalität gibt es sehr wahrscheinlich schon in Henshin. (NEIN! - hier wird mit mappings anstelle von matches gearbeitet!)
	// Spezifikation der MEthode: Gibt die Menge der Kanten aus der Regel zurück, die beim anwenden auf den overlapGraph zu einer dangling edge führen würden!
	public List<Edge> findDanglingEdges(Rule rule, List<Mapping> mappings) {
		List<Edge> danglingEdges = new LinkedList<Edge>();
		
		//TODO: build an initial bidirektional hashmap of the nodes in the rule and in the overlapgraph based on the mappings!
			// teilweise schon durch "mappingsOfOriginNodes" und "mappingsOfImageNodes" geschehen!
		
		
		//TODO: einfach prüfen, ob es Kanten zu den gelöschten Knoten gibt, die durch die Regel nicht mit abgedeckt sind. 
		EList<Node> deletionNodesOfRule = rule.getActionNodes(new Action(Action.Type.DELETE));
		EList<Edge> deletionEdgesOfRule = rule.getActionEdges(new Action(Action.Type.DELETE));
		// Annahme: es gibt ein mapping für jeden Knoten aus der Regel.
		HashMap<Node, Mapping> mappingsOfOriginNodes = new HashMap<Node, Mapping>();	
		for(Mapping mapping : mappings){
			Node originNodeOfMapping = mapping.getOrigin();
			mappingsOfOriginNodes.put(originNodeOfMapping, mapping);
		}
		
		HashMap<Node, Mapping> mappingsOfImageNodes = new HashMap<Node, Mapping>();	
		for(Mapping mapping : mappings){
			Node imageNodeOfMapping = mapping.getImage();
			mappingsOfImageNodes.put(imageNodeOfMapping, mapping);
		}
		
		// für jeden gelöschten Knoten prüfen, dass auch all seine Kanten gelöscht werden. 
		for(Node deleteNode : deletionNodesOfRule){
			Mapping mappingOfDeleteNode = mappingsOfOriginNodes.get(deleteNode);
			Node nodeToBeDeletedInOverlapGraph = mappingOfDeleteNode.getImage();
			EList<Edge> allDeletionEdgesOfDeletionNodeInRule = deleteNode.getAllEdges();
			//TODO: all ascending nodes of deleteNode
			
			EList<Edge> allEdgesToBeCheckedForDeletion = nodeToBeDeletedInOverlapGraph.getAllEdges();
			for(Edge edgeInOverlapToBeCheckedForDeletion : allEdgesToBeCheckedForDeletion){
				EReference typeOfEdgeInOverlapGraph = edgeInOverlapToBeCheckedForDeletion.getType();
				Node sourceOfEdgeInOverlapGraph = edgeInOverlapToBeCheckedForDeletion.getSource();
				Mapping mappingOfSourceOfEdge = mappingsOfImageNodes.get(sourceOfEdgeInOverlapGraph);
				// kann "null" zurückgeben, dann Kante den dangling edges hinzufügen und "break" der for-schleife
				if(mappingOfSourceOfEdge == null){
					danglingEdges.add(edgeInOverlapToBeCheckedForDeletion);
					continue;
				}
				// kann "null" zurückgeben, dann Kante den dangling edges hinzufügen und "break" der for-schleife
				Node sourceOfEdgeInRule = mappingOfSourceOfEdge.getOrigin();
				
				
				Node targetOfEdgeInOverlapGraph = edgeInOverlapToBeCheckedForDeletion.getTarget();
				Mapping mappingOfTargetOfEdge = mappingsOfImageNodes.get(targetOfEdgeInOverlapGraph);
				// kann "null" zurückgeben, dann Kante den dangling edges hinzufügen und "break" der for-schleife
				if(mappingOfTargetOfEdge == null){
					danglingEdges.add(edgeInOverlapToBeCheckedForDeletion);
					continue;
				}
				// kann "null" zurückgeben, dann Kante den dangling edges hinzufügen und "break" der for-schleife
				Node targetOfEdgeInRule = mappingOfTargetOfEdge.getOrigin();
				
				
				if(sourceOfEdgeInRule != null && targetOfEdgeInRule !=null){
					
					//TODO: identify second node of Edge
//				Node secondInvolvedNodeOfDeletionEdge = null;
//				if(edgeInOverlapToBeCheckedForDeletion.getSource() == nodeToBeDeletedInOverlapGraph)
//					secondInvolvedNodeOfDeletionEdge = edgeInOverlapToBeCheckedForDeletion.getTarget();
//				if(edgeInOverlapToBeCheckedForDeletion.getTarget() == nodeToBeDeletedInOverlapGraph)
//					secondInvolvedNodeOfDeletionEdge = edgeInOverlapToBeCheckedForDeletion.getSource();
					//TODO: finden des zugehörgen Knotens in der Regel
//				Mapping mapping = mappingsOfImageNodes.get(secondInvolvedNodeOfDeletionEdge);
//				Node originNodeOfEdgeToBeDeleted = mapping.getOrigin();	
					// sollte 
					
					//TODO: prüfen, dass die Kante zwischen den beiden Knoten in der Regel als zu löschende Kante definiert ist.
					// TODO: alle Kanten zwischen beiden Knoten (deleteNode & originNodeOfEdgeToBeDeleted) in der Regel ermitteln
					EList<Edge> outgoingEdgesOfNodeInRule = sourceOfEdgeInRule.getOutgoing();	
					// TODO: Ziel abgleichen
					List<Edge> potentialEdgesInRule = new LinkedList<Edge>();
					for(Edge edge : outgoingEdgesOfNodeInRule){
						if(edge.getTarget() == targetOfEdgeInRule)
							potentialEdgesInRule.add(edge);
					}
					// TODO: type abgleichen
					Edge edgeInRule = new EdgeImpl();//only temporary to prevent NPE
					for(Edge edge : potentialEdgesInRule){
						if(edge.getType().equals(edgeInOverlapToBeCheckedForDeletion.getType()))
							edgeInRule = edge;
					}				
					// TODO: prüfen, dass löschende Kante!
					if(!(edgeInRule.getAction().getType().equals(Action.Type.DELETE))){
						danglingEdges.add(edgeInOverlapToBeCheckedForDeletion);
					}
				}
				else{
					danglingEdges.add(edgeInOverlapToBeCheckedForDeletion);
				}
			}
		}
		
		return danglingEdges;
	}



	// TODO: noch ist unklar ob eine solche Datenstruktur notwendig ist, 
	//		oder es sich um Instanzen einer bereits bekannten Datenstruktur handelt.
	// 		Je nach Ergebnis löschen oder in eigenständiges class-file auslagern.
	public class ConflictAtom{
		Span span;
		/**
		 * @return the span
		 */
		public Span getSpan() {
			return span;
		}

		/**
		 * @return the reasons
		 */
		public Set<Span> getReasons() {
			return reasons;
		}

		Set<Span> reasons;

		// in Algo Zeile 6 wird ein Atom mit den Parametern candidate und reasons initilisiert.
		// dennoch ist die Datenstruktur noch nicht klar!
		public ConflictAtom(Span candidate, Set<Span> reasons) {
			this.span = candidate;
			this.reasons = reasons;
		}
		
	}
	
	
	public Span newSpan(Mapping nodeInRule1Mapping, Graph s1, Mapping nodeInRule2Mapping){
		return new Span(nodeInRule1Mapping, s1, nodeInRule2Mapping);
	}
	
	public Span newSpan(List<Mapping> rule1Mappings, Graph s1, List<Mapping> rule2Mappings){
		return new Span(rule1Mappings, s1, rule2Mappings);
	}
	
	// TODO: noch ist unklar ob eine solche Datenstruktur notwendig ist, 
	//		oder es sich um Instanzen einer bereits bekannten Datenstruktur handelt.
	// 		Je nach Ergebnis löschen oder in eigenständiges class-file auslagern.
	
	//Generell: Laut Definition 3 ist ein Span z.B. C1<-incl-S1-match->L2
	//		d.h. drei Graphen verbunden über eine Inklusion und einen Match
	
	public class Span{
		
		List<Mapping> mappingsInRule1;
		List<Mapping> mappingsInRule2;
		
		
		// Scheint derzeit ncoh überflüssig zu sein!
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
//			result = prime * result + getOuterType().hashCode();
//			result = prime * result + ((graph == null) ? 0 : graph.hashCode());
//			result = prime * result + ((mappingsInRule1 == null) ? 0 : mappingsInRule1.hashCode()); // no application due to missing knwoledge on the hashCode of two lists with equal content but different order 
//			result = prime * result + ((mappingsInRule2 == null) ? 0 : mappingsInRule2.hashCode());
			return result;
		}


		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Span other = (Span) obj;
//			if (!getOuterType().equals(other.getOuterType())) //specific for inner class - might be irrelevant by extraction to own class file!
//				return false;
			if (graph == null) { //should never happen!
				if (other.graph != null) //should never happen!
					return false;
			} else{
				// !graph.equals(other.graph)
				/*
				 * TODO:
				 * 1. vergleichen, dass die Anzahl der Elemente im Graph gleich sind 
				 * 2. für jedes Element im Graph das jeweils passende im anderen Graph finden!
				 * 3. je Element prüfen, dass die Mappings das selbe Ziel haben.
				 * 		Vorteil hierbei ist, dass die Regeln die selben sind und osmit die ziele mit "==" verglichen werden können.  
				 */
				//compare "size" of Graphs
					EList<Node> ownNodes = graph.getNodes();
					EList<Node> otherNodes = other.graph.getNodes();
					if(ownNodes.size() != otherNodes.size()){
						return false;
					}
					EList<Edge> ownEdges = graph.getEdges();
					EList<Edge> otherEdges = other.getGraph().getEdges();
					if(ownEdges.size() != otherEdges.size()){
						return false;
					}					
					
				// build allocation between own graph elements and others graph elements.
					// all mappings in the first rule should be the same!
					for(Node nodeInOwnGraph : ownNodes){
						// get mapping and node in rule1
						Mapping mappingOfNodeInRule1 = getMappingInRule1(nodeInOwnGraph);
						Node associatedNodeInRule1 = mappingOfNodeInRule1.getImage();
						// get mapping in otherGraph
						Mapping mappingOfOtherNodeInRule1 = other.getMappingFromGraphToRule1(associatedNodeInRule1);
						if(mappingOfOtherNodeInRule1 == null)
							return false;
						Node nodeInOtherGraph = mappingOfOtherNodeInRule1.getOrigin();
						// check that both mappings in rule 2 have the same target
						Mapping mappingOfNodeInRule2 = getMappingInRule2(nodeInOwnGraph);
						Node associatedNodeInRule2 = mappingOfNodeInRule2.getImage();
						Mapping mappingOfOtherNodeInRule2 = other.getMappingInRule2(nodeInOtherGraph);
						Node associatedNodeOfOtherGraphInRule2 = mappingOfOtherNodeInRule2.getImage();
						if(associatedNodeOfOtherGraphInRule2 == associatedNodeInRule2)
							return true;
					}
					
				//
					
					
//				return false;
			}
//			if (mappingsInRule1 == null) {
//				if (other.mappingsInRule1 != null)
//					return false;
//			} else if (!mappingsInRule1.equals(other.mappingsInRule1))
//				return false;
//			if (mappingsInRule2 == null) {
//				if (other.mappingsInRule2 != null)
//					return false;
//			} else if (!mappingsInRule2.equals(other.mappingsInRule2))
//				return false;
			return false;
		}


		/**
		 * @return the mappingsInRule1
		 */
		public List<Mapping> getMappingsInRule1() {
			return mappingsInRule1;
		}
		

		/**
		 * @return the mappingsInRule2
		 */
		public List<Mapping> getMappingsInRule2() {
			return mappingsInRule2;
		}

		Graph graph;

		public Span(Mapping nodeInRule1Mapping, Graph s1, Mapping nodeInRule2Mapping) {
			this.graph = s1;
			mappingsInRule1 = new LinkedList<Mapping>();
			mappingsInRule1.add(nodeInRule1Mapping);
			mappingsInRule2 = new LinkedList<Mapping>();
			mappingsInRule2.add(nodeInRule2Mapping);
		}

		public Mapping getMappingFromGraphToRule2(Node imageNode) {
			for(Mapping mappingInRule2 : mappingsInRule2){
				if(mappingInRule2.getImage() == imageNode)
					return mappingInRule2;
			}
			return null;
		}

		public Mapping getMappingFromGraphToRule1(Node imageNode) {
			for(Mapping mappingInRule1 : mappingsInRule1){
				if(mappingInRule1.getImage() == imageNode)
					return mappingInRule1;
			}
			return null;
		}

		public Span(List<Mapping> rule1Mappings, Graph s1, List<Mapping> rule2Mappings) {
			this.mappingsInRule1 = rule1Mappings;
			this.mappingsInRule2 = rule2Mappings;
			this.graph = s1;
		}

		public Span(Span s1) {
//			copy Graph and mappings!
			// Copier
			Copier copierForSpanAndMappings= new Copier();
			// copy of graph
			Graph copiedGraph = (Graph) copierForSpanAndMappings.copy(s1.getGraph());
			copierForSpanAndMappings.copyReferences();
			this.graph = copiedGraph;
			
			//TODO: extract to method
			List<Mapping> mappingsInRule1 = new LinkedList<Mapping>();
			for(Mapping mapping : s1.getMappingsInRule1()){
				Mapping copiedMapping = (Mapping) copierForSpanAndMappings.copy(mapping);
				copierForSpanAndMappings.copyReferences();
				mappingsInRule1.add(copiedMapping);				
			}
			this.mappingsInRule1 = mappingsInRule1;
			
			List<Mapping> mappingsInRule2 = new LinkedList<Mapping>();
			for(Mapping mapping : s1.getMappingsInRule2()){
				Mapping copiedMapping = (Mapping) copierForSpanAndMappings.copy(mapping);
				copierForSpanAndMappings.copyReferences();
				mappingsInRule2.add(copiedMapping);				
			}
			this.mappingsInRule2 = mappingsInRule2;
		}

		public Graph getGraph() {
			return graph;			
		}

		//TODO use "getMappingWithImage(...)" method
		public Mapping getMappingInRule1(Node originNode) {
			for(Mapping mapping : mappingsInRule1){
				if(mapping.getOrigin() == originNode)
					return mapping;
			}
			return null;
		}

		//TODO use "getMappingWithImage(...)" method
		public Mapping getMappingInRule2(Node originNode) {
			for(Mapping mapping : mappingsInRule2){
				if(mapping.getOrigin() == originNode)
					return mapping;
			}
			return null;
		}


		private AtomicCoreCPA getOuterType() {
			return AtomicCoreCPA.this;
		}
		
	}

	
	
	public PushoutResult newPushoutResult(Rule rule1, Span span, Rule rule2) {
		return new PushoutResult(rule1, span, rule2);
	}
	
	// TODO: noch ist unklar ob eine solche Datenstruktur notwendig ist, 
	//		oder es sich um Instanzen einer bereits bekannten Datenstruktur handelt.
	// 		Je nach Ergebnis löschen oder in eigenständiges class-file auslagern.
	
	// Generell: muss die matches m1 und m2 aus L1 und L2 enthalten und somit auch G.
	// daher kennt es oder referenziert es auch (indirekt?) die beiden Regeln
	public class PushoutResult{
		
		/**
		 * @return the mappingsOfRule1
		 */
		public List<Mapping> getMappingsOfRule1() {
			return mappingsOfRule1;
		}

		/**
		 * @return the mappingsOfRule2
		 */
		public List<Mapping> getMappingsOfRule2() {
			return mappingsOfRule2;
		}
		
		/**
		 * @return the resultGraph
		 */
		public Graph getResultGraph() {
			return resultGraph;
		}

		Graph resultGraph;
		
		private List<Mapping> mappingsOfRule1;
		private List<Mapping> mappingsOfRule2;
		
		//überflüssig!
//		private Mapping mapping1;
//		private Mapping mapping2;

		public PushoutResult(Rule rule1, Span s1, Rule rule2) {

//			Da es mutmaßlich nur ein 
//			- kopieren der beiden linken Seiten
//				- dabei aufbauen der mappings
//			- entfernen der "doppelten" Knoten und Kanten aus L2
//			- zusammenfassen der Mengen 
//			ist, kann dies alles auch im Konstruktor ablaufen!
			
			// EGraph.copy(...) ist ungeeignet, da es nicht ermöglicht die cross references abzurufen.
			Graph lhsOfRule1 = rule1.getLhs();
//			Mapping mapping1 = henshinFactory.createM
			mappingsOfRule1 = new LinkedList<Mapping>();
			
//			if (copies==null) {
			//TODO: extract to Method
				Copier copierForRule1 = new Copier();
				Graph copyOfLhsOfRule1 = (Graph) copierForRule1.copy(lhsOfRule1);
//				copier.copyAll(getRoots());
				copierForRule1.copyReferences();
//				copies = copier;
				for(Node node : lhsOfRule1.getNodes()){
					Node copyResultNode = (Node) copierForRule1.get(node);
					Mapping createdMapping = henshinFactory.createMapping(node, copyResultNode);
					mappingsOfRule1.add(createdMapping);					
				}
				
				Graph lhsOfRule2 = rule2.getLhs();
				mappingsOfRule2 = new LinkedList<Mapping>();
				Copier copierForRule2 = new Copier();
				Graph copyOfLhsOfRule2 = (Graph) copierForRule2.copy(lhsOfRule2);
//				copier.copyAll(getRoots());
				copierForRule2.copyReferences();
//				copies = copier;
				for(Node node : lhsOfRule2.getNodes()){
					Node copyResultNode = (Node) copierForRule2.get(node);
					Mapping createdMapping = henshinFactory.createMapping(node, copyResultNode);
					mappingsOfRule2.add(createdMapping);
				}
				
				/* TODO:
				 * 
				 *  
				 */
				Graph s1Graph = s1.getGraph();
				// replace common nodes in copyOfRule2 with the one in copyOfRule1
				for(Node node : s1Graph.getNodes()){
					// retarget associated edges
						// get associated node and mapping in both copies
					Mapping mappingInRule1 = s1.getMappingInRule1(node); //TODO: deal with NPE!!!
					Mapping mappingInRule2 = s1.getMappingInRule2(node); //TODO: deal with NPE!!!
					if(mappingInRule1 == null || mappingInRule2 == null){
						System.out.println("which node is it?");
					}
					if(mappingInRule1 != null && mappingInRule2 != null){
						//identify node in copy of rule1
						Node nodeInL1 = mappingInRule1.getImage();//TODO: deal with NPE!!!
						Mapping mappingFromL1ToOverlapGraph = getMappingOfOrigin(mappingsOfRule1, nodeInL1);
						Node nodeOfL1PartOfOverlap = mappingFromL1ToOverlapGraph.getImage();
						//identify node in copy of rule2
						Node nodeInL2 = mappingInRule2.getImage();//TODO: deal with NPE!!!
						Mapping mappingFromL2ToOverlapGraph = getMappingOfOrigin(mappingsOfRule2, nodeInL2);
						Node nodeOfL2PartOfOverlap = mappingFromL2ToOverlapGraph.getImage();
						// alle Kanten von nodeInL2 auf nodeInL1 umhängen
						//TODO: replace by improved version based on:
						// http://stackoverflow.com/questions/18448671/how-to-avoid-concurrentmodificationexception-while-removing-elements-from-arr
						List<Edge> incomingEdgesInL2 = new LinkedList<Edge>();
						for(Edge incomingEdge : nodeOfL2PartOfOverlap.getIncoming()){
							incomingEdgesInL2.add(incomingEdge);
						}
						for(Edge incomingEdge : incomingEdgesInL2){
							incomingEdge.setTarget(nodeOfL1PartOfOverlap);
						}
	
						List<Edge> outgoingEdgesOfL2 = new LinkedList<Edge>();
						for(Edge outgoingEdge : nodeOfL2PartOfOverlap.getOutgoing()){
							outgoingEdgesOfL2.add(outgoingEdge);
						}
						for(Edge outgoingEdge : outgoingEdgesOfL2){
							outgoingEdge.setSource(nodeOfL1PartOfOverlap);
						}					
						//mappingFromOverlapGraphToL2 anpassen - nodeInL1 als neues "image" setzen.
						mappingFromL2ToOverlapGraph.setImage(nodeOfL1PartOfOverlap);
						//prüfen, dass Knoten "nodeInL2" keine ein- oder ausgehenden KAnten mehr hat.
						if(nodeOfL2PartOfOverlap.getAllEdges().size() > 0){
							System.err.println("All Edges of should have been removed, but still "+nodeInL2.getAllEdges().size()+" are remaining!");
						}
						//löschen des Knoten "nodeInL2y"
						Graph graphOfNodeL2 = nodeOfL2PartOfOverlap.getGraph();
						boolean equalGraph = (copyOfLhsOfRule2 == graphOfNodeL2);
						boolean removedNode = graphOfNodeL2.removeNode(nodeOfL2PartOfOverlap);
						System.out.println("removedNode: "+removedNode);
					}
				}
				// move all edges and nodes from copyOfLhsOfRule2 to copyOfLhsOfRule1
//				Iterator<Node> iter = copyOfLhsOfRule2.getNodes().iterator();
//				while (iter.hasNext()) {
//				    Node nodeInCopyOfLhsOfRule2 = iter.next();
//				    nodeInCopyOfLhsOfRule2.setGraph(copyOfLhsOfRule1);
//				}
				List<Node> nodesInCopyOfLhsOfRule2 = new LinkedList<Node>();
				System.out.println("copyOfLhsOfRule2.getNodes(): "+copyOfLhsOfRule2.getNodes().size());
				for(Node nodeInCopyOfLhsOfRule2 : copyOfLhsOfRule2.getNodes()){
					nodesInCopyOfLhsOfRule2.add(nodeInCopyOfLhsOfRule2);
				}
				for(Node nodeInCopyOfLhsOfRule2 : nodesInCopyOfLhsOfRule2){
					nodeInCopyOfLhsOfRule2.setGraph(copyOfLhsOfRule1);
				}
				
				List<Edge> edgesInCopyOfLhsOfRule2 = new LinkedList<Edge>();
				for(Edge edgeInCopyOfLhsOfRule2 : copyOfLhsOfRule2.getEdges()){
					edgesInCopyOfLhsOfRule2.add(edgeInCopyOfLhsOfRule2);
				}
				for(Edge edgeInCopyOfLhsOfRule2 : edgesInCopyOfLhsOfRule2){
					edgeInCopyOfLhsOfRule2.setGraph(copyOfLhsOfRule1);
				}
				// check that NO edges and nodes are remaining in copyOfLhsOfRule2
				if(copyOfLhsOfRule2.getEdges().size() > 0){
					System.err.println(copyOfLhsOfRule2.getEdges().size()+" edges reaming in "+copyOfLhsOfRule2+", but should be 0");
				}
				if(copyOfLhsOfRule2.getNodes().size() > 0){
					System.err.println(copyOfLhsOfRule2.getNodes().size()+" nodes reaming in "+copyOfLhsOfRule2+", but should be 0");
				}
				// check that the number of edges and nodes in edgeInCopyOfLhsOfRule1 is correct
				int numberOfExpectedNodes = (lhsOfRule1.getNodes().size() + lhsOfRule2.getNodes().size() - s1Graph.getNodes().size());
				if(copyOfLhsOfRule1.getNodes().size() != numberOfExpectedNodes){
					System.err.println("Amount of nodes in created result graph of pushout not as expected. Difference: "+ (copyOfLhsOfRule1.getNodes().size()-numberOfExpectedNodes));
				}
				// set copyOfLhsOfRule1 as resultGraph
				resultGraph = copyOfLhsOfRule1;
			
		}

		private Mapping getMappingOfOrigin(List<Mapping> mappingsOfRules, Node origin) {
			for(Mapping mapping : mappingsOfRules){
				if(mapping.getOrigin() == origin)
					return mapping;
			}
			return null;
		}

		
	}
	

}
